namespace Dishora.DTO
{
    public class CheckoutRequestDto
    {
        public int Amount { get; set; }   // sent in centavos by Android
        public string ItemName { get; set; }
        public int Quantity { get; set; }
        public int VendorId { get; set; }             // which vendor
        public string PaymentMethodType { get; set; } // chosen by user (card/gcash/paymaya)
        public string OrderDetailsMetadata { get; set; }
    }
}
