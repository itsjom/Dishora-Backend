using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class payment_methods
{
    public long payment_method_id { get; set; }

    public string method_name { get; set; } = null!;

    public string description { get; set; } = null!;

    public string status { get; set; } = null!;

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual ICollection<business_payment_methods> BusinessPaymentMethods { get; set; } = new List<business_payment_methods>();

    public virtual ICollection<orders> orders { get; set; } = new List<orders>();

    public virtual ICollection<payment_details> payment_details { get; set; } = new List<payment_details>();

    // public virtual ICollection<pre_orders> preorders { get; set; } = new List<pre_orders>();
}
