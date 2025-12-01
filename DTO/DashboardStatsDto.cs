using System.Text.Json.Serialization;

namespace Dishora.DTO
{
    public class DashboardStatsDto
    {
        [JsonPropertyName("totalRevenue")]
        public double TotalRevenue { get; set; }

        [JsonPropertyName("newOrders")]
        public int NewOrders { get; set; }

        [JsonPropertyName("activeProducts")]
        public int ActiveProducts { get; set; }

        [JsonPropertyName("averageRating")]
        public double AverageRating { get; set; }
    }
}
