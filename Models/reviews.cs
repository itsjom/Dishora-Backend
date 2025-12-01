using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class reviews
{
    public long review_id { get; set; }

    public long customer_id { get; set; }

    public long business_id { get; set; }

    // public long order_id { get; set; }

    public int rating { get; set; }

    public string? comment { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual business_details business { get; set; } = null!;

    public virtual customers customer { get; set; }

    // public virtual orders order { get; set; } = null!;
}
