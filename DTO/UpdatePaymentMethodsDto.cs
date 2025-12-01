using System.Text.Json.Serialization;

namespace Dishora.DTO
{
    public class UpdatePaymentMethodsDto
    {
        public List<long> Payment_Methods { get; set; } = new();
    }
}
