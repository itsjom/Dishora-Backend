namespace Dishora.DTO
{
    public class OrderStatusResponseDto
    {
        // Status can be: "pending", "processed", "not_found"
        public string Status { get; set; }
        public long? OrderId { get; set; } // The final order ID, if you want to send it
    }
}
