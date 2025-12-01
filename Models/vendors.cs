using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class vendors
{
    public long vendor_id { get; set; }

    public long user_id { get; set; }

    public string fullname { get; set; } = null!;

    public string? phone_number { get; set; }

    public string registration_status { get; set; } = null!;

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual ICollection<business_details> BusinessDetails { get; set; } = new List<business_details>();

    public virtual users User { get; set; } = null!;
}
