using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class messages
{
    public long message_id { get; set; }

    public long sender_id { get; set; }

    public string? sender_role { get; set; }

    public long receiver_id { get; set; }

    public string? receiver_role { get; set; }

    public string? image_url { get; set; }

    public string? message_text { get; set; }

    public DateTime sent_at { get; set; }

    public bool? is_read { get; set; }
}
