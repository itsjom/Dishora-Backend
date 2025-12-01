namespace Dishora.Models
{
    public class product_dietary_specifications
    {
        public long product_id { get; set; }
        public long dietary_specification_id { get; set; }
        public DateTime? created_at { get; set; }
        public DateTime? updated_at { get; set; }
        public virtual dietary_specifications dietary_specification { get; set; } = null!;
        public virtual products product { get; set; } = null!;
    }
}
