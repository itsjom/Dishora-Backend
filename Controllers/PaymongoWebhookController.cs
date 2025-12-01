using Dishora.Data;
using Dishora.DTO;
using Dishora.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;
using System;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace Dishora.Controllers
{

    [ApiController]
    [Route("api/paymongo-webhook")]
    [AllowAnonymous]
    public class PayMongoWebhookController : ControllerBase
    {
        private readonly IOrderService _orderService;
        private readonly DishoraDbContext _context;
        private readonly ILogger<PayMongoWebhookController> _logger;

        public PayMongoWebhookController(IOrderService orderService, DishoraDbContext context, ILogger<PayMongoWebhookController> logger)
        {
            _orderService = orderService;
            _context = context;
            _logger = logger;
        }

        [HttpPost]
        // [ServiceFilter(typeof(PayMongoSignatureFilter))]
        public async Task<IActionResult> HandleWebhook()
        {
            string json;
            using (var reader = new StreamReader(Request.Body)) { json = await reader.ReadToEndAsync(); }
            _logger.LogInformation("--- Received PayMongo Webhook ---\n{WebhookJson}", json);

            try
            {
                var webhookEvent = JsonConvert.DeserializeObject<PayMongoWebhookEvent>(json);
                if (webhookEvent?.Data?.Attributes?.Type == "checkout_session.payment.paid")
                {
                    // var checkoutId = webhookEvent.Data.Id;
                    var checkoutId = webhookEvent.Data.Attributes.Data.Id;
                    if (string.IsNullOrEmpty(checkoutId))
                    {
                        _logger.LogWarning("Webhook ignored: checkout_session.payment.paid event received but checkout session ID was null or empty.");
                        return Ok();
                    }

                    // Find the unprocessed draft matching the transaction ID
                    var draft = await _context.checkout_drafts.FirstOrDefaultAsync(d => d.transaction_id == checkoutId && d.processed_at == null);
                    if (draft == null)
                    {
                        _logger.LogWarning("Webhook ignored: No unprocessed draft found for TransactionId {TransactionId}.", checkoutId);
                        return Ok();
                    }

                    // ✅ Step 1: Deserialize the stored JSON back into your DTOs
                    var cartItems = JsonConvert.DeserializeObject<List<ItemDTO>>(draft.cart);
                    var deliveryInfo = JsonConvert.DeserializeObject<DeliveryInfoDto>(draft.delivery);

                    // ✅ Step 2: Reconstruct the final OrderRequest object
                    var orderRequest = new OrderRequest
                    {
                        UserId = draft.user_id,
                        BusinessId = deliveryInfo.BusinessId, // Get BusinessId from the delivery info
                        PaymentMethodId = draft.payment_method_id,
                        Total = draft.total,
                        DeliveryDate = deliveryInfo.DeliveryDate,
                        DeliveryTime = deliveryInfo.DeliveryTime,
                        Address = new AddressDTO { FullAddress = deliveryInfo.FullAddress, PhoneNumber = deliveryInfo.PhoneNumber },
                        Items = cartItems
                    };

                    // ✅ Step 3: Create the final order
                    await _orderService.CreateOrderAsync(orderRequest);

                    // ✅ Step 4: Mark the draft as processed to prevent duplicates
                    draft.processed_at = DateTime.UtcNow;
                    await _context.SaveChangesAsync();

                    _logger.LogInformation("Successfully created order from draft #{DraftId} for TransactionId {TransactionId}", draft.checkout_draft_id, checkoutId);
                }
                else
                {
                    // Log other event types if needed, but return Ok()
                    _logger.LogInformation("Webhook received for event type {EventType}, ignoring.", webhookEvent?.Data?.Attributes?.Type ?? "Unknown");
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "ERROR processing webhook. Raw JSON: {WebhookJson}", json);
            }

            return Ok();
        }
    }

    // Simplified helper classes for webhook deserialization
    // Simplified helper classes for webhook deserialization
    public class PayMongoWebhookEvent
    {
        [JsonProperty("data")]
        public WebhookData Data { get; set; }
    }

    public class WebhookData
    {
        [JsonProperty("id")]
        public string Id { get; set; }

        [JsonProperty("attributes")]
        public WebhookAttributes Attributes { get; set; }
    }

    // This is the new class you need to add
    public class WebhookAttributesData
    {
        [JsonProperty("id")]
        public string Id { get; set; }
    }

    public class WebhookAttributes
    {
        [JsonProperty("type")]
        public string Type { get; set; }

        // This property is updated to use the new class
        [JsonProperty("data")]
        public WebhookAttributesData Data { get; set; }
    }
}