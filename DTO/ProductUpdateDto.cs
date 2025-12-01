    namespace Dishora.DTO
    {
        // Used only for editing product details
        public class ProductUpdateDto
        {
            public string? item_name { get; set; }
            public decimal? price { get; set; }
            // public int? cutoff_minutes { get; set; }
            public decimal? advance_amount { get; set; }
            public bool? is_available { get; set; }
            public bool? is_pre_order { get; set; }
            public string? description { get; set; }
            public long? product_category_id { get; set; }
        }
    }
