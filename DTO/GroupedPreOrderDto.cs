namespace Dishora.DTO
{
    public class GroupedPreOrderDto
    {
        public string GroupId { get; set; }
        public string CustomerName { get; set; }
        public double TotalAmount { get; set; }
        public string Status { get; set; }
        public List<PreOrderItemDto> Items { get; set; }

        // --- ADD THESE NEW FIELDS ---
        public string OrderDate { get; set; }       // The date it was pre-ordered
        public string DeliveryDate { get; set; }      // The requested delivery date/time
        public string ContactNumber { get; set; }
        public string DeliveryAddress { get; set; }
        public double AdvancePaid { get; set; }
        public double BalanceDue { get; set; }
    }
}
