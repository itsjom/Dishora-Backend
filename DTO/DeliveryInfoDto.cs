namespace Dishora.DTO
{
    public class DeliveryInfoDto
    {
        public long BusinessId { get; set; }
        public string FullAddress { get; set; } = string.Empty;
        public string PhoneNumber { get; set; } = string.Empty;
        public string DeliveryDate { get; set; } = string.Empty;
        public string DeliveryTime { get; set; } = string.Empty;
    }
}
