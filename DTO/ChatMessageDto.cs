namespace Dishora.DTO
{
    public class ChatMessageDto
    {
        public long message_id { get; set; }
        public long sender_id { get; set; }
        public string? sender_role { get; set; }
        public string? sender_name { get; set; } // "Jomar" or "Lola's Lutong Bahay"
        public string? message_text { get; set; }
        public string? image_url { get; set; } // <--- ADD THIS
        public DateTime sent_at { get; set; }
    }
}
