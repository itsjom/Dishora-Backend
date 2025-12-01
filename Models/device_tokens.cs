using System;
using System.Collections.Generic;
using System.Data;

namespace Dishora.Models
{
    public class device_tokens
    {
        public long device_token_id { get; set; }

        public long user_id { get; set; }

        public string provider { get; set; } = null!;

        public string token { get; set; } = null!;

        public string? sns_endpoint_arn { get; set; }

        public string? platform { get; set; }

        public DateTime? last_seen { get; set; }

        public bool is_active { get; set; }

        public DateTime? created_at { get; set; }

        public virtual users User { get; set; } = null!;
    }
}
