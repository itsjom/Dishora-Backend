using Microsoft.Build.Framework;

namespace Dishora.DTO
{
    public class SendMessageDto
    {
        [Required]
        public string? message_text { get; set; }
    }
}
