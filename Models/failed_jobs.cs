using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class failed_jobs
{
    public long id { get; set; }

    public string uu_id { get; set; } = null!;

    public string connection { get; set; } = null!;

    public string queue { get; set; } = null!;

    public string payload { get; set; } = null!;

    public string exception { get; set; } = null!;

    public DateTime failed_at { get; set; }
}
