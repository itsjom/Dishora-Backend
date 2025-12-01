using System;
using System.Collections.Generic;
using System.Data;

namespace Dishora.Models
{
    public class notification_preferences
    {
        public long preference_id { get; set; }

        public long user_id { get; set; }

        public string event_type { get; set; } = null!;

        public string channel { get; set; } = null!;

        public bool enabled { get; set; }

        public DateTime? updated_at { get; set; }

        public virtual users User { get; set; } = null!;
    }
}
