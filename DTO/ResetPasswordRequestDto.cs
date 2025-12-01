using System.ComponentModel.DataAnnotations;

namespace Dishora.DTO
{
    public class ResetPasswordRequestDto
    {
        [Required]
        public string Token { get; set; }

        [Required]
        [MinLength(8)]
        public string Password { get; set; }

        [Required]
        [Compare("Password", ErrorMessage = "Passwords do not match")]
        public string ConfirmPassword { get; set; }
    }
}
