namespace Dishora.DTO
{
    // This class defines the exact data structure we will send TO the vendor's app.
    // It is a "Data Transfer Object" for displaying information.
    public class VendorOrderItemDto
    {
        public long OrderItemId { get; set; }
        public long OrderId { get; set; }
        public string ProductName { get; set; }
        public int Quantity { get; set; }
        public decimal PriceAtOrderTime { get; set; }
        public string OrderItemStatus { get; set; }
        public DateTime? CreatedAt { get; set; }
      

        // --- New fields specifically added for better UI display ---
        public string PaymentMethodName { get; set; }
        public string CustomerFullName { get; set; }
        public string? ContactNumber { get; set; }
        public string? DeliveryAddress { get; set; }
        public DateTime? DeliveryDate { get; set; }
        public string? DeliveryTime { get; set; }

        public string? ProofOfDelivery { get; set; }
        // public int PrepTime { get; set; }
    }
}
