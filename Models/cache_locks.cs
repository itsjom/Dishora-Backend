using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class cache_locks
{
    public string key { get; set; } = null!;

    public string owner { get; set; } = null!;

    public int expiration { get; set; }
}
