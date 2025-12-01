using Dishora.Data;
using Dishora.DTO;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class BusinessController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        // Inject your DbContext
        public BusinessController(DishoraDbContext context)
        {
            _context = context;
        }

        [HttpGet("nearby")]
        public async Task<IActionResult> GetNearbyVendors([FromQuery] double latitude, [FromQuery] double longitude)
        {
            // Set your desired search radius in kilometers
            double radiusInKm = 10;

            // 1. Get all businesses from the DB that HAVE a location
            var allBusinesses = await _context.business_details
                .Where(b => b.latitude.HasValue && b.longitude.HasValue)
                .ToListAsync();

            // 2. Filter the list in-memory

            var nearbyBusinesses = allBusinesses
                .Select(b => new {
                    Business = b,
                    Distance = GetDistance(latitude, longitude, b.latitude.Value, b.longitude.Value)
                })
                .Where(b => b.Distance <= radiusInKm)
                .OrderBy(b => b.Distance)
                // ✅ START: UPDATE THIS SELECT STATEMENT
                .Select(b => new NearbyVendorDto
                {
                    Id = b.Business.business_id,
                    Name = b.Business.business_name,
                    LogoUrl = b.Business.business_image,
                    // Map the coordinates from the Business object
                    Latitude = b.Business.latitude.Value,  // Use .Value since they are nullable doubles
                    Longitude = b.Business.longitude.Value // Use .Value since they are nullable doubles
                })
                // ✅ END: UPDATE THIS SELECT STATEMENT
                .ToList();

            return Ok(nearbyBusinesses);
        }


        // --- Helper Functions for Distance Calculation ---

        // This is the Haversine formula to calculate distance between two lat/lon points
        private double GetDistance(double lat1, double lon1, double lat2, double lon2)
        {
            const double R = 6371; // Radius of Earth in kilometers
            var dLat = ToRadians(lat2 - lat1);
            var dLon = ToRadians(lon2 - lon1);
            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2)) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
            return R * c; // Distance in km
        }

        private double ToRadians(double angle)
        {
            return Math.PI * angle / 180.0;
        }


        // --- Additional Endpoint to Get Business Details for Vendor Profile Tab ---
        [HttpGet("{id}")]
        public async Task<IActionResult> GetBusinessDetails(long id)
        {
            var business = await _context.business_details
                .Where(b => b.business_id == id)
                .Select(b => new BusinessProfileDto
                {
                    BusinessName = b.business_name,
                    BusinessImage = b.business_image
                })
                .FirstOrDefaultAsync();

            if (business == null)
            {
                return NotFound("Business not found.");
            }

            return Ok(business);
        }
    }
}
