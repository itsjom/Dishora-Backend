namespace Dishora.DTO
{

    // Used for sending product data to Android
    public class ProductDto
    {
        public long product_id { get; set; }
        public string item_name { get; set; } = string.Empty;
        public decimal price { get; set; }
        public decimal advance_amount { get; set; }
        public bool is_available { get; set; }
        public bool is_pre_order { get; set; }
        public string? description { get; set; }
        public string? image_url { get; set; }
        public long vendor_id { get; set; }
        public List<long>? dietary_specification_ids { get; set; }
        // For the customer-side menu chips
        public List<string>? tags { get; set; }
        public long? product_category_id { get; set; } // For the category dropdown
        public int? cutoff_minutes { get; set; }
    }
}