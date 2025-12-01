using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class delivery_addresses
{
    public long delivery_address_id { get; set; }

    public long order_id { get; set; }

    public long user_id { get; set; }

    public string? phone_number { get; set; }

    public string? region { get; set; }

    public string? province { get; set; }

    public string? city { get; set; }

    public string? barangay { get; set; }

    public string? postal_code { get; set; }

    public string? street_name { get; set; }

    public string? full_address { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual orders Order { get; set; } = null!;
    public virtual users User { get; set; } = null!;
}
