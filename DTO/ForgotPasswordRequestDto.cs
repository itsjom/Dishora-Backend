using System.ComponentModel.DataAnnotations;

namespace Dishora.DTO
{
    public class ForgotPasswordRequestDto
    {
        [Required]
        [EmailAddress]
        public string Email { get; set; }
    }
}
