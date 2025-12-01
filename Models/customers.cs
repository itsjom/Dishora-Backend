using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class customers
{
    public long customer_id { get; set; }

    public long user_id { get; set; }

    public string? user_image { get; set; }

    public string? user_address { get; set; }

    public double? latitude { get; set; }

    public double? longitude { get; set; }

    public string? contact_number { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual users user { get; set; } = null!;

    public virtual ICollection<reviews> reviews { get; set; } = new List<reviews>();
}
