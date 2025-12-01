namespace Dishora.DTO
{
    public class VendorStatusDto
    {
        public bool IsVendor { get; set; }
        public string VendorStatus { get; set; } = "Not Registered"; // "Pending", "Approved", etc.
        public long? VendorId { get; set; }
    }
}