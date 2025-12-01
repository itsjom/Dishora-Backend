namespace Dishora.Models
{
    public class business_pm_details
    {
        public long business_pm_details_id { get; set; }

        public long business_payment_method_id { get; set; }

        public string account_number { get; set; } = null!;

        public string account_name { get; set; } = null!;

        public bool? is_active { get; set; }

        public DateTime? created_at { get; set; }

        public DateTime? updated_at { get; set; }

        public virtual business_payment_methods business_payment_method { get; set; } = null!;
    }
}
