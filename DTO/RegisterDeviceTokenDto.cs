using System.Text.Json.Serialization;

namespace Dishora.DTO
{
    public class RegisterDeviceTokenDto
    {
        [JsonPropertyName("token")]
        public string Token { get; set; }
    }
}
