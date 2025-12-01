using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class sessions
{
    public string id { get; set; } = null!;

    public long? user_id { get; set; }

    public string? ip_address { get; set; }

    public string? user_agent { get; set; }

    public string payload { get; set; } = null!;

    public int last_activity { get; set; }
}
