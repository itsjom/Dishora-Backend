using System;
using System.Collections.Generic;
using System.Data;

namespace Dishora.Models
{
    public class notification_deliveries
    {
        public long delivery_id { get; set; }

        public long notification_id { get; set; }

        public string? provider { get; set; }

        public string? provider_response { get; set; }

        public bool? success { get; set; }

        public DateTime? attempted_at { get; set; }

        public virtual notifications notification { get; set; } = null!;
    }
}
