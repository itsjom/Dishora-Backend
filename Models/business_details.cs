using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class business_details
{
    public long business_id { get; set; }

    public string? business_image { get; set; }

    public long vendor_id { get; set; }

    public string business_name { get; set; } = null!;

    public string business_description { get; set; } = null!;

    public string business_type { get; set; } = null!;

    public string? business_location { get; set; }

    public string? valid_id_type { get; set; }

    public string? valid_id_no { get; set; }

    public string? business_permit_no { get; set; }

    public string? bir_reg_no { get; set; }

    public string? business_permit_file { get; set; }

    public string? valid_id_file { get; set; }

    public string? bir_reg_file { get; set; }

    public string? mayor_permit_file { get; set; }

    public string? business_duration { get; set; }

    public double? latitude { get; set; }

    public double? longitude { get; set; }

    public string verification_status { get; set; } = null!;

    public int preorder_lead_time_hours { get; set; }

    public string? remarks { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual ICollection<business_opening_hours> opening_hours { get; set; } = new List<business_opening_hours>();

    public virtual ICollection<business_payment_methods> payment_methods { get; set; } = new List<business_payment_methods>();

    public virtual ICollection<orders> orders { get; set; } = new List<orders>();

    public virtual ICollection<preorder_schedule> preorder_schedule { get; set; } = new List<preorder_schedule>();

    public virtual ICollection<products> products { get; set; } = new List<products>();

    public virtual ICollection<reviews> reviews { get; set; } = new List<reviews>();

    public virtual vendors vendor { get; set; } = null!;
}
