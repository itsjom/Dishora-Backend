using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Dishora.Services; // <-- ADD THIS
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;
using System.Net.Http.Json;
using System.Text.Json; // <-- Keep this

namespace Dishora.Controllers
{

    [ApiController]
    [Route("api/[controller]")]
    public class PaymentController : ControllerBase
    {
        private readonly DishoraDbContext _context;
        private readonly HttpClient _httpClient;
        private readonly string _payMongoSecret;
        private readonly string _publicGatewayUrl;
        private readonly ILogger<PaymentController> _logger; // <-- 2. Add logger field

        // --- 1. ADD THE NEW SERVICE ---
        private readonly IPaymentProcessingService _paymentProcessor;

        private static readonly Dictionary<string, string> PayMongoMap = new(StringComparer.OrdinalIgnoreCase) {
            { "GCash", "gcash" }, { "Maya", "paymaya" }, { "Credit/Debit Card", "card"}, { "Cash on Delivery", "cod" }
        };

        // --- 2. INJECT THE NEW SERVICE ---
        public PaymentController(
            DishoraDbContext context, 
            IHttpClientFactory httpClientFactory, 
            IConfiguration configuration, 
            ILogger<PaymentController> logger, 
            IPaymentProcessingService paymentProcessor)
        {
            _context = context;
            _httpClient = httpClientFactory.CreateClient();
            _logger = logger; // <-- 4. Assign the logger

            // --- 3. INJECT THE SERVICE ---
            _paymentProcessor = paymentProcessor;

            _payMongoSecret = configuration["PayMongo:SecretKey"] ?? throw new ArgumentNullException("PayMongo:SecretKey is missing!");
            _publicGatewayUrl = configuration["AppSettings:PublicGatewayUrl"] ?? throw new ArgumentNullException("AppSettings:PublicGatewayUrl is missing!");
            var encoded = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes($"{_payMongoSecret}:"));
            _httpClient.DefaultRequestHeaders.Add("Authorization", $"Basic {encoded}");
        }

        [HttpPost("create-checkout")]
        public async Task<IActionResult> CreateCheckout([FromBody] CheckoutRequestDto request)
        {
            // (This entire method is unchanged)
            #region Validation
            var enabledMethodsRaw = await _context.business_payment_methods
                .Where(bpm => bpm.business_id == request.VendorId)
                .Join(_context.payment_methods, bpm => bpm.payment_method_id, pm => pm.payment_method_id, (bpm, pm) => pm.method_name)
                .ToListAsync();

            if (!enabledMethodsRaw.Any())
            {
                return BadRequest("This vendor has no enabled payment methods.");
            }

            var enabledMethods = enabledMethodsRaw
                .Where(m => PayMongoMap
                .ContainsKey(m))
                .Select(m => PayMongoMap[m])
                .ToList();

            if (!enabledMethods.Contains(request.PaymentMethodType.ToLower()))
            {
                return BadRequest("This payment method is not enabled for this vendor.");
            }

            var methodName = PayMongoMap
                .FirstOrDefault(x => x.Value
                .Equals(request.PaymentMethodType, StringComparison.OrdinalIgnoreCase)).Key;

            var paymentMethod = await _context.payment_methods
                .FirstOrDefaultAsync(pm => pm.method_name == methodName);

            if (paymentMethod == null)
            {
                return BadRequest($"Payment method '{request.PaymentMethodType}' is not configured.");
            }

            if (string.IsNullOrEmpty(request.OrderDetailsMetadata))
            {
                return BadRequest("OrderDetailsMetadata is required.");

            }

            #endregion
            OrderRequest orderDetails;
            try { 
                orderDetails = JsonConvert.DeserializeObject<OrderRequest>(request.OrderDetailsMetadata); 
            }
            catch (Newtonsoft.Json.JsonException ex) 
            { 
                return BadRequest($"Invalid JSON format in OrderDetailsMetadata: {ex.Message}"); 
            
            }
            if (orderDetails == null)
            {
                return BadRequest("Failed to parse order details from metadata.");
            }

            var cartJson = JsonConvert.SerializeObject(orderDetails.Items);
            var deliveryJson = JsonConvert.SerializeObject(new
            {
                BusinessId = request.VendorId,
                FullAddress = orderDetails.Address.FullAddress,
                PhoneNumber = orderDetails.Address.PhoneNumber,
                DeliveryDate = orderDetails.DeliveryDate,
                DeliveryTime = orderDetails.DeliveryTime
            });

            var draft = new checkout_drafts
            {
                user_id = orderDetails.UserId,
                payment_method_id = paymentMethod.payment_method_id,
                total = orderDetails.Total,
                cart = cartJson,
                delivery = deliveryJson,
                is_cod = false,
                created_at = DateTime.UtcNow,
                updated_at = DateTime.UtcNow
            };
            _context.checkout_drafts.Add(draft);
            await _context.SaveChangesAsync();
            var payload = new
            {
                data = new
                {
                    attributes = new
                    {
                        line_items = new[] 
                        { 
                            new 
                            { 
                                currency = "PHP", amount = request.Amount, name = request.ItemName, quantity = request.Quantity 
                            }  
                        },
                        payment_method_types = new[] 
                        { 
                            request.PaymentMethodType 
                        },
                        success_url = $"{_publicGatewayUrl}payment/success?draftId={draft.checkout_draft_id}",
                        cancel_url = $"{_publicGatewayUrl}payment/cancel",
                        metadata = new Dictionary<string, object> { { "checkoutDraftId", draft.checkout_draft_id } }

                        /*success_url = $"dishora://payment/success?draftId={draft.checkout_draft_id}",
                        cancel_url = $"dishora://payment/cancel",
                        metadata = new Dictionary<string, object> { { "checkoutDraftId", draft.checkout_draft_id } }*/
                    }
                }
            };
            var response = await _httpClient.PostAsJsonAsync("https://api.paymongo.com/v1/checkout_sessions", payload);
            var respString = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
            {
                _logger.LogError("PayMongo API Error creating checkout session ({StatusCode}): {ErrorBody}", (int)response.StatusCode, respString);
                _context.checkout_drafts.Remove(draft); await _context.SaveChangesAsync();
                return StatusCode((int)response.StatusCode, $"PayMongo API Error: {respString}");
            }
            using var doc = System.Text.Json.JsonDocument.Parse(respString);
            var dataElement = doc.RootElement.GetProperty("data");
            var checkoutUrl = dataElement.GetProperty("attributes").GetProperty("checkout_url").GetString();
            var checkoutId = dataElement.GetProperty("id").GetString();
            draft.transaction_id = checkoutId;
            await _context.SaveChangesAsync();
            return Ok(new CheckoutResponseDto
            {
                CheckoutUrl = checkoutUrl ?? string.Empty,
                CheckoutId = checkoutId ?? string.Empty,
                DraftId = draft.checkout_draft_id
            });
        }

        // --- 4. REPLACE THIS ENTIRE METHOD ---
        /* [HttpGet("status-by-draft/{draftId}")]
        public async Task<IActionResult> GetOrderStatusByDraftId(long draftId)
        {
            // All the logic is now in the service. We just call it.
            var result = await _paymentProcessor.CheckAndProcessPayment(draftId);
            return Ok(result);
        } */



        // --- ADD SUCCESS REDIRECT ENDPOINT ---
        [HttpGet("success")]
        [AllowAnonymous] // Allow access without JWT token from browser redirect
        public IActionResult HandleSuccessRedirect([FromQuery] long draftId)
        {
            string deepLinkUrl = $"dishora://payment/success?draftId={draftId}";
            _logger.LogInformation("Redirecting to success deeplink: {DeepLinkUrl}", deepLinkUrl);
            // Returns an HTTP 302 Found response to the browser
            return Redirect(deepLinkUrl);
        }
        // --- END SUCCESS REDIRECT ---



        // --- ADD CANCEL REDIRECT ENDPOINT ---
        [HttpGet("cancel")]
        [AllowAnonymous] // Allow access without JWT token from browser redirect
        public IActionResult HandleCancelRedirect([FromQuery] long draftId) // Accept draftId here
        {
            string deepLinkUrl = $"dishora://payment/cancel?draftId={draftId}";
            _logger.LogInformation("Redirecting to cancel deeplink: {DeepLinkUrl}", deepLinkUrl);
            // Returns an HTTP 302 Found response to the browser
            return Redirect(deepLinkUrl);
        }
        // --- END CANCEL REDIRECT ---


        [HttpGet("draft-details/{draftId}")]
        public async Task<IActionResult> GetDraftDetails(long draftId)
        {
            var draft = await _context.checkout_drafts
                                    .AsNoTracking() // Read-only is efficient
                                    .FirstOrDefaultAsync(d => d.checkout_draft_id == draftId);

            if (draft == null)
            {
                _logger.LogWarning("GetDraftDetails requested for non-existent draftId {DraftId}", draftId);
                return NotFound();
            }

            // Deserialize only the delivery info part needed (BusinessId)
            try
            {
                // Make sure DeliveryInfoDto matches the structure saved in draft.delivery JSON
                var deliveryInfo = JsonConvert.DeserializeObject<DeliveryInfoDto>(draft.delivery);
                if (deliveryInfo == null)
                {
                    _logger.LogError("Failed to deserialize delivery info for draft {DraftId}. JSON: {DeliveryJson}", draftId, draft.delivery);
                    return StatusCode(500, "Failed to parse delivery info in draft.");
                }

                // Return just the businessId in an anonymous object
                return Ok(new { businessId = deliveryInfo.BusinessId });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to deserialize delivery info for draft {DraftId}. JSON: {DeliveryJson}", draftId, draft.delivery);
                return StatusCode(500, "Error reading draft details.");
            }
        }


        [HttpGet("status-by-draft/{draftId}")]
        public async Task<IActionResult> GetOrderStatusByDraftId(long draftId)
        {
            var draft = await _context.checkout_drafts
                                      .AsNoTracking() // Read-only query
                                      .FirstOrDefaultAsync(d => d.checkout_draft_id == draftId);

            if (draft == null)
            {
                // Should not normally happen if called right after create-checkout
                return NotFound(new OrderStatusResponseDto { Status = "success" });
            }

            // The ONLY thing we care about: Has the webhook processed it yet?
            if (draft.processed_at != null)
            {
                return Ok(new OrderStatusResponseDto { Status = "processed" });
            }
            else
            {
                // If not processed yet, it's still pending from the app's perspective
                return Ok(new OrderStatusResponseDto { Status = "pending" });
            }
        }
    }
}