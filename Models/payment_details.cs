using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class payment_details
{
    public long payment_detail_id { get; set; }

    public long payment_method_id { get; set; }

    public long order_id { get; set; }

    public string? transaction_id { get; set; }

    public decimal? amount_paid { get; set; }

    public string payment_status { get; set; } = null!;

    public string? payment_reference { get; set; }

    public DateTime? paid_at { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual orders order { get; set; } = null!;

    public virtual payment_methods payment_method { get; set; } = null!;
}
