using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class job_batches
{
    public string id { get; set; } = null!;

    public string name { get; set; } = null!;

    public int total_jobs { get; set; }

    public int pending_jobs { get; set; }

    public int failed_jobs { get; set; }

    public string failed_job_ids { get; set; } = null!;

    public string? options { get; set; }

    public int? cancelled_at { get; set; }

    public int created_at { get; set; }

    public int? finished_at { get; set; }
}
