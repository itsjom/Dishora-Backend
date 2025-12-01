using System.Text.Json.Serialization;

namespace Dishora.DTO
{
    public class OrderDto
    {
        [JsonPropertyName("id")]
        public long Id { get; set; }

        [JsonPropertyName("placedDate")]
        public string PlacedDate { get; set; }

        [JsonPropertyName("vendorName")]
        public string VendorName { get; set; }

        [JsonPropertyName("total")]
        public decimal Total { get; set; }

        [JsonPropertyName("status")]
        public string Status { get; set; }

        [JsonPropertyName("isPreOrder")]
        public bool IsPreOrder { get; set; }

        [JsonPropertyName("orderType")]
        public string OrderType { get; set; }

    }
}
