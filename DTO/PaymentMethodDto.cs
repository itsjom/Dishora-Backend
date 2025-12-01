namespace Dishora.DTO
{
    public class PaymentMethodDto
    {
        public long payment_method_id { get; set; }
        public long master_method_id { get; set; }
        public string method_name { get; set; }
        public string description { get; set; }
        public bool enabled { get; set; }
        public string? account_name { get; set; }
        public string? account_number { get; set; }
    }
}
