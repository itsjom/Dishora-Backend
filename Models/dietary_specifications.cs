using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class dietary_specifications
{
    public long dietary_specification_id { get; set; }

    public string dietary_spec_name { get; set; } = null!;

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }
    public virtual ICollection<product_dietary_specifications> product_dietary_specifications { get; set; } = new List<product_dietary_specifications>();
}
