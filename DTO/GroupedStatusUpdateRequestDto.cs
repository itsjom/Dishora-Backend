namespace Dishora.DTO
{
    public class GroupStatusUpdateRequest
    {
        public long OrderId { get; set; }
        public string NewStatus { get; set; }
    }
}
