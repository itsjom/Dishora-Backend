using System;
using System.Collections.Generic;
using System.Data;

namespace Dishora.Models
{
    public class notifications
    {
        public long notification_id { get; set; }

        public long user_id { get; set; }

        public long? actor_user_id { get; set; }

        public string event_type { get; set; } = null!;

        public string? reference_table { get; set; }

        public long? reference_id { get; set; }

        public string? payload { get; set; }

        public bool is_read { get; set; }

        public string channel { get; set; } = null!;

        public DateTime? created_at { get; set; }

        public DateTime? expires_at { get; set; }

        public long? business_id { get; set; }

        public string? recipient_role { get; set; }

        public bool is_global { get; set; }

        public virtual ICollection<notification_deliveries> notificationdeliveries { get; set; } = new List<notification_deliveries>();

        public virtual users User { get; set; } = null!;
    }
}
