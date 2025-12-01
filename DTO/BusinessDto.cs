namespace Dishora.DTO
{
    public class BusinessDto
    {
        public string businessName { get; set; }
        public string description { get; set; }
        public string type { get; set; }
        public string location { get; set; }
        public double latitude { get; set; }
        public double longitude { get; set; }

        public string? businessImage { get; set; }
        public string? businessDuration { get; set; }

        public string? birRegNo { get; set; }
        public string? birRegFile { get; set; }
        public string? businessPermitNo { get; set; }
        public string? businessPermitFile { get; set; }
        public string? validIdType { get; set; }
        public string? validIdNo { get; set; }
        public string? validIdFile { get; set; }
        public string? mayorPermitFile { get; set; }

        public List<OpeningHourDto> OpeningHours { get; set; }
    }
}
