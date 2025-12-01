using System.Text.Json.Serialization;

namespace Dishora.DTO
{
    public class ScheduleItem
    {
        [JsonPropertyName("scheduleId")]
        public long ScheduleId { get; set; }

        [JsonPropertyName("availableDate")]
        public string AvailableDate { get; set; } // "yyyy-MM-dd"

        [JsonPropertyName("maxOrders")]
        public int MaxOrders { get; set; }

        [JsonPropertyName("currentOrderCount")]
        public int CurrentOrderCount { get; set; } // Your Java class uses 'currentOrders'

        [JsonPropertyName("isActive")]
        public bool IsActive { get; set; }

        [JsonPropertyName("businessId")]
        public long BusinessId { get; set; }
    }
}
