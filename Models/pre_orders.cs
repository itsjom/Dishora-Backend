using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class pre_orders
{
    public long pre_order_id { get; set; }

    public long order_id { get; set; }

    public decimal total_advance_required { get; set; }

    public decimal advance_paid_amount { get; set; }

    public decimal amount_due { get; set; }

    public string? payment_transaction_id { get; set; }

    public string? payment_option { get; set; }

    public string preorder_status { get; set; } = null!;

    public string? receipt_url { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual orders Order { get; set; } = null!;
}
