namespace Dishora.DTO
{
    public class VendorListDto
    {
        public long VendorId { get; set; }
        public long UserId { get; set; }
        public long BusinessId { get; set; }
        public string BusinessName { get; set; }
        public string BusinessAddress { get; set; }
        public string BusinessDescription { get; set; }
        public string BusinessImage { get; set; }
        public string VendorStatus { get; set; }
        public double Rating { get; set; }  // optional for now
        public string OpeningHours { get; set; }
    }
}