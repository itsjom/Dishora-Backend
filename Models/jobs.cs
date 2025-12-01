using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class jobs
{
    public long id { get; set; }

    public string queue { get; set; } = null!;

    public string payload { get; set; } = null!;

    public byte attempts { get; set; }

    public int? reserved_at { get; set; }

    public int available_at { get; set; }

    public int created_at { get; set; }
}
