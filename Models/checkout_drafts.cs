using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class checkout_drafts
{
    public long checkout_draft_id { get; set; }

    public long user_id { get; set; }

    public long payment_method_id { get; set; }

    public string? transaction_id { get; set; }

    public decimal total { get; set; }

    public string cart { get; set; } = null!;

    public string delivery { get; set; } = null!;

    public string? item_notes { get; set; }

    public bool? is_cod { get; set; }

    public DateTime? processed_at { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }
}
