namespace Dishora.DTO
{
    public class OpeningHourDto
    {
        public string dayOfWeek { get; set; }
        public string opensAt { get; set; }
        public string closesAt { get; set; }
        public bool isClosed { get; set; }
    }
}
