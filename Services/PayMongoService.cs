using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Microsoft.Extensions.Configuration;

namespace Dishora.Services
{
    public class PayMongoService
    {
        private readonly HttpClient _httpClient;

        public PayMongoService(HttpClient httpClient, IConfiguration configuration)
        {
            _httpClient = httpClient;

            // ✅ Load secret key from appsettings.json or environment variable
            var secretKey = configuration["PayMongo:SecretKey"]
                            ?? throw new ArgumentNullException("PayMongo:SecretKey is not configured!");

            // ✅ Base64 encode key for Basic Auth → "sk_test_xxx:"
            var base64Key = Convert.ToBase64String(Encoding.UTF8.GetBytes($"{secretKey}:"));

            _httpClient.BaseAddress = new Uri("https://api.paymongo.com/v1/");
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Basic", base64Key);
            _httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        }

        public async Task<string> CreatePaymentMethod(
            string type, object details, string billingName, string billingEmail, string billingPhone)
        {
            var requestData = new
            {
                data = new
                {
                    attributes = new
                    {
                        type = type, // "card" or "gcash" or "paymaya"
                        details = details, // card details OR empty object for eWallets
                        billing = new
                        {
                            name = billingName,
                            email = billingEmail,
                            phone = billingPhone
                        }
                    }
                }
            };

            var content = new StringContent(JsonSerializer.Serialize(requestData), Encoding.UTF8, "application/json");
            var response = await _httpClient.PostAsync("payment_methods", content);
            return await response.Content.ReadAsStringAsync();
        }

        public async Task<string> CreatePaymentIntent(decimal amount, string currency = "PHP")
        {
            var requestData = new
            {
                data = new
                {
                    attributes = new
                    {
                        amount = (int)(amount * 100), // PayMongo expects centavos
                        payment_method_allowed = new[] { "card" },
                        currency = currency
                    }
                }
            };

            var content = new StringContent(JsonSerializer.Serialize(requestData), Encoding.UTF8, "application/json");
            var response = await _httpClient.PostAsync("payment_intents", content);
            return await response.Content.ReadAsStringAsync();
        }

        public async Task<string> AttachPaymentMethod(string intentId, string paymentMethodId, string returnUrl)
        {
            var requestData = new
            {
                data = new
                {
                    attributes = new
                    {
                        payment_method = paymentMethodId,
                        return_url = returnUrl
                    }
                }
            };

            var content = new StringContent(JsonSerializer.Serialize(requestData), Encoding.UTF8, "application/json");
            var response = await _httpClient.PostAsync($"payment_intents/{intentId}/attach", content);
            return await response.Content.ReadAsStringAsync();
        }

        public async Task<string> GetPaymentIntent(string intentId)
        {
            var response = await _httpClient.GetAsync($"payment_intents/{intentId}");
            return await response.Content.ReadAsStringAsync();
        }
    }
}