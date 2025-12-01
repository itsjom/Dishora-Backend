using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class cart_sessions
{
    public long cart_session_id { get; set; }

    public long? user_id { get; set; }

    public string session_id { get; set; } = null!;

    public string cart { get; set; } = null!;

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }
}
