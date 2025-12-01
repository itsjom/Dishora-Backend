using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class business_payment_methods
{
    public long business_payment_method_id { get; set; }

    public long business_id { get; set; }

    public long payment_method_id { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual business_details business { get; set; } = null!;

    public virtual payment_methods payment_method { get; set; } = null!;

    public virtual business_pm_details business_pm_detail { get; set; } = null!;

}
