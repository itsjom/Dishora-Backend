namespace Dishora.Models
{
    public class preorder_schedule
    {
        public long schedule_id { get; set; }
        public long business_id { get; set; }
        public DateOnly available_date { get; set; }
        public int max_orders { get; set; }
        public int current_order_count { get; set; }
        public bool? is_active { get; set; }
        public DateTime? created_at { get; set; }
        public DateTime? updated_at { get; set; }
        public virtual business_details business { get; set; } = null!;
    }
}
