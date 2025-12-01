using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class order_items
{
    public long order_item_id { get; set; }

    public long order_id { get; set; }

    public long product_id { get; set; }

    public string product_name { get; set; } = null!;

    public string? product_description { get; set; }

    public int quantity { get; set; }

    public decimal price_at_order_time { get; set; }

    public string order_item_status { get; set; } = null!;

    public string? order_item_note { get; set; }

    public bool? is_pre_order { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual orders order { get; set; } = null!;

    public virtual products product { get; set; } = null!;
}
