namespace Dishora.DTO
{
    public class RegistrationRequest
    {
        // Existing structured data as JSON strings passed via multipart fields
        public string VendorJson { get; set; }   // serialized VendorDto
        public string BusinessJson { get; set; } // serialized BusinessDto
        public string OpeningHoursJson { get; set; } // serialized OpeningHourDto list

        // File uploads
        public IFormFile BusinessImage { get; set; }
        public IFormFile BirRegFile { get; set; }
        public IFormFile BusinessPermitFile { get; set; }
        public IFormFile ValidIdFile { get; set; }
        public IFormFile? MayorPermitFile { get; set; }
    }
}
