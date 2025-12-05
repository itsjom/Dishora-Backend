using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class orders
{
    public long order_id { get; set; }

    public long user_id { get; set; }

    public long business_id { get; set; }

    public decimal total { get; set; }

    public DateOnly delivery_date { get; set; }

    public String delivery_time { get; set; }

    public long payment_method_id { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public string? proof_of_delivery { get; set; }

    public string? cancellation_reason { get; set; }

    public virtual business_details business { get; set; } = null!;

    public virtual ICollection<delivery_addresses> delivery_address { get; set; } = new List<delivery_addresses>();

    public virtual ICollection<order_items> order_item { get; set; } = new List<order_items>();

    public virtual ICollection<payment_details> payment_detail { get; set; } = new List<payment_details>();

    public virtual payment_methods payment_method { get; set; } = null!;

    public virtual pre_orders? preorder { get; set; }

    public virtual users User { get; set; } = null!;
}
