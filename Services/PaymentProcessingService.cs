using Dishora.Controllers;
using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Dishora.Services;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;
using System;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;

namespace Dishora.Services
{
    // First, create the interface
    public interface IPaymentProcessingService
    {
        Task<OrderStatusResponseDto> CheckAndProcessPayment(long draftId);
    }

    // Next, create the implementation
    public class PaymentProcessingService : IPaymentProcessingService
    {
        private readonly DishoraDbContext _context;
        private readonly IOrderService _orderService;
        private readonly HttpClient _httpClient;

        public PaymentProcessingService(DishoraDbContext context, IOrderService orderService, IHttpClientFactory httpClientFactory, IConfiguration configuration)
        {
            _context = context;
            _orderService = orderService;
            _httpClient = httpClientFactory.CreateClient();

            // Configure the HttpClient to talk to PayMongo
            var _payMongoSecret = configuration["PayMongo:SecretKey"] ?? throw new ArgumentNullException("PayMongo:SecretKey is missing!");
            var encoded = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes($"{_payMongoSecret}:"));
            _httpClient.DefaultRequestHeaders.Add("Authorization", $"Basic {encoded}");
        }

        public async Task<OrderStatusResponseDto> CheckAndProcessPayment(long draftId)
        {
            // Find the draft. We need to track changes.
            var draft = await _context.checkout_drafts
                                      .FirstOrDefaultAsync(d => d.checkout_draft_id == draftId);

            if (draft == null)
            {
                return new OrderStatusResponseDto { Status = "not_found" };
            }

            // If it's already processed, just return success.
            if (draft.processed_at != null)
            {
                return new OrderStatusResponseDto { Status = "processed" };
            }

            if (string.IsNullOrEmpty(draft.transaction_id))
            {
                return new OrderStatusResponseDto { Status = "pending" };
            }

            try
            {
                // Call PayMongo's API to get the session status
                var response = await _httpClient.GetAsync($"https://api.paymongo.com/v1/checkout_sessions/{draft.transaction_id}");
                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Error checking PayMongo status for {draft.transaction_id}: {response.StatusCode}");
                    return new OrderStatusResponseDto { Status = "pending" };
                }

                var jsonString = await response.Content.ReadAsStringAsync();
                using var doc = System.Text.Json.JsonDocument.Parse(jsonString);
                var attributes = doc.RootElement.GetProperty("data").GetProperty("attributes");

                var status = attributes.GetProperty("status").GetString();

                if (status == "paid")
                {
                    // --- SUCCESS! ---
                    // 1. Deserialize the draft data
                    var cartItems = JsonConvert.DeserializeObject<List<ItemDTO>>(draft.cart);
                    var deliveryInfo = JsonConvert.DeserializeObject<DeliveryInfoDto>(draft.delivery);

                    // 2. Reconstruct the final OrderRequest object
                    var orderRequest = new OrderRequest
                    {
                        UserId = draft.user_id,
                        BusinessId = deliveryInfo.BusinessId,
                        PaymentMethodId = draft.payment_method_id,
                        Total = draft.total,
                        DeliveryDate = deliveryInfo.DeliveryDate,
                        DeliveryTime = deliveryInfo.DeliveryTime,
                        Address = new AddressDTO { FullAddress = deliveryInfo.FullAddress, PhoneNumber = deliveryInfo.PhoneNumber },
                        Items = cartItems
                    };

                    // 3. Create the final order
                    await _orderService.CreateOrderAsync(orderRequest);

                    // 4. Mark the draft as processed
                    draft.processed_at = DateTime.UtcNow;
                    await _context.SaveChangesAsync();

                    // 5. Tell the app it's processed!
                    return new OrderStatusResponseDto { Status = "processed" };
                }
                else
                {
                    // Status is 'pending', 'expired', etc.
                    return new OrderStatusResponseDto { Status = "pending" };
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"ERROR in CheckAndProcessPayment: {ex.ToString()}");
                return new OrderStatusResponseDto { Status = "pending" };
            }
        }
    }
}