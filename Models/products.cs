using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class products
{
    public long product_id { get; set; }

    public long business_id { get; set; }

    public long? product_category_id { get; set; }

    public string item_name { get; set; } = null!;

    public decimal price { get; set; }

    public int? cutoff_minutes { get; set; }

    public bool? is_available { get; set; }

    public bool? is_pre_order { get; set; }

    public decimal advance_amount { get; set; }

    public string? image_url { get; set; }

    public string? description { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual ICollection<order_items> order_items { get; } = new List<order_items>();

    // public virtual ICollection<pre_orders> pre_orders { get; } = new List<pre_orders>();

    public virtual ICollection<product_dietary_specifications> product_dietary_specifications { get; } = new List<product_dietary_specifications>();

    public virtual business_details business { get; set; } = null!;

    public virtual product_categories? product_category { get; set; }
}
