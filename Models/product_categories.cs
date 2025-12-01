using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class product_categories
{
    public long product_category_id { get; set; }

    public string category_name { get; set; } = null!;

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual ICollection<products> products { get; set; } = new List<products>();
}
