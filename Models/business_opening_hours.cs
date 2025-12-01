using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class business_opening_hours
{
    public long business_opening_hours_id { get; set; }

    public long business_id { get; set; }

    public string day_of_week { get; set; } = null!;

    public TimeOnly? opens_at { get; set; }

    public TimeOnly? closes_at { get; set; }

    public bool? is_closed { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual business_details business { get; set; } = null!;
}
