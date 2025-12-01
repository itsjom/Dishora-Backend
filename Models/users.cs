using System;
using System.Collections.Generic;
using System.Data;

namespace Dishora.Models;

public partial class users
{
    public long user_id { get; set; }

    public string? fullname { get; set; }

    public string username { get; set; } = null!;

    public string email { get; set; } = null!;

    public DateTime? email_verified_at { get; set; }
    public string password { get; set; } = null!;

    public bool is_verified { get; set; } = false;

    public string? verification_token { get; set; }

    public string? remember_token { get; set; }

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }

    public virtual ICollection<customers> customers { get; set; } = new List<customers>();

    public virtual ICollection<delivery_addresses> deliveryaddresses { get; set; } = new List<delivery_addresses>();

    public virtual ICollection<device_tokens> devicetokens { get; set; } = new List<device_tokens>();

    public virtual ICollection<notification_preferences> notificationpreferences { get; set; } = new List<notification_preferences>();

    public virtual ICollection<notifications> notifications { get; set; } = new List<notifications>();

    public virtual ICollection<orders> orders { get; set; } = new List<orders>();

    // public virtual ICollection<reviews> reviews { get; set; } = new List<reviews>();

    // public virtual ICollection<pre_orders> pre_orders { get; set; } = new List<pre_orders>();

    public virtual vendors? vendor { get; set; }

}
