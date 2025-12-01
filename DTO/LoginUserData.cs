namespace Dishora.DTO
{
    public class LoginUserData
    {
        public string Token { get; set; } = string.Empty;
        public long User_Id { get; set; }
        public string Username { get; set; } = string.Empty;
        public string FullName { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;

        // Vendor info
        public bool IsVendor { get; set; }
        public string VendorStatus { get; set; } = "Not Registered";
        public long? VendorId { get; set; }

        // ✅ New: Business info
        public long? Business_Id { get; set; }
    }
}
