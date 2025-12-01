namespace Dishora.DTO
{
    public class PreOrderScheduleResponseDto
    {
        public long ScheduleId { get; set; }
        public string AvailableDate { get; set; } // Send as string for easy Android consumption
        public int MaxOrders { get; set; }
        public int CurrentOrderCount { get; set; }
        public bool IsActive { get; set; }
        public long BusinessId { get; set; }

        public int RemainingCapacity => MaxOrders - CurrentOrderCount;
    }
}
