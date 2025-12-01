namespace Dishora.DTO
{
    public class OrderRequest
    {
        public long UserId { get; set; }
        public long BusinessId { get; set; }
        public long PaymentMethodId { get; set; }
        public decimal Total { get; set; }
        public String DeliveryDate { get; set; }
        public string DeliveryTime { get; set; } = string.Empty;
        public AddressDTO? Address { get; set; }
        public List<ItemDTO> Items { get; set; }
    }

    public class AddressDTO
    {
        public string? PhoneNumber { get; set; }
        public string? Region { get; set; }
        public string? Province { get; set; }
        public string? City { get; set; }
        public string? Barangay { get; set; }
        public string? PostalCode { get; set; }
        public string? StreetName { get; set; }
        public string? FullAddress { get; set; }
    }

    public class ItemDTO
    {
        public long ProductId { get; set; }
        public string ProductName { get; set; } = string.Empty;
        public string? ProductDescription { get; set; }
        public int Quantity { get; set; }
        public decimal PriceAtOrderTime { get; set; }
        public string? OrderItemNote { get; set; }
        public bool IsPreOrder { get; set; }
    }
}