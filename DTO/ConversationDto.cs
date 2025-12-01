namespace Dishora.DTO
{
    public class CustomerConversationDto
    {
        // DTO for the customer's conversation list
        public long BusinessId { get; set; }
        public string? BusinessName { get; set; }
        public string? BusinessImageUrl { get; set; }
        public string? LastMessage { get; set; }
        public DateTime LatestMessageTime { get; set; }
        public int UnreadCount { get; set; }
    }

    // DTO for the vendor's conversation list
    public class VendorConversationDto
    {
        public long CustomerId { get; set; }
        public string? CustomerName { get; set; }
        public string? LastMessage { get; set; }
        public DateTime LatestMessageTime { get; set; }
        public int UnreadCount { get; set; }
    }
}
