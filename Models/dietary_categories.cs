using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class dietary_categories
{
    public long dietary_category_id { get; set; }

    public string dietary_category_name { get; set; } = null!;

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }
}
