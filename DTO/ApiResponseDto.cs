namespace Dishora.DTO
{
    public class ApiResponseDto
    {
        public bool success { get; set; }
        public string message { get; set; }
        public object? data { get; set; } // Optional for return payloads (nullable)
    }
}
