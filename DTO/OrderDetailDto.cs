namespace Dishora.DTO
{
    public class OrderDetailDto
    {
        public long Id { get; set; }
        public string PlacedDate { get; set; }
        public string VendorName { get; set; }
        public decimal Total { get; set; }
        public string Status { get; set; }

        // --- New fields for the details screen ---
        public string DeliveryDate { get; set; }
        public bool IsPaid { get; set; } // Based on your 'payment_details' or 'payment_method'
        public string? DeliveryAddress { get; set; } // Make it nullable (?)
        public string? ContactNumber { get; set; }
        public bool IsPreOrder { get; set; }
        public List<OrderItemDto> Items { get; set; }
    }
}
