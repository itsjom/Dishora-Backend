namespace Dishora.DTO
{
    public class LoginResponseDto
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public LoginUserData Data { get; set; } = new();
    }
}
