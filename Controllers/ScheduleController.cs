using Dishora.Data;
using Dishora.DTO; // This now contains your new ScheduleItem DTO
using Microsoft.AspNetCore.Authorization; // Need this for [AllowAnonymous]
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore; // Need this for .Select and .ToListAsync
using System.Collections.Generic; // Need this for List<>
using System.Linq; // Need this for .Where, .OrderBy

namespace Dishora.Controllers
{
    [Route("api/v1/business")] // Controller-level route
    [ApiController]
    public class ScheduleController : ControllerBase
    {
        // Your DbContext, injected in the constructor
        private readonly DishoraDbContext _context;

        public ScheduleController(DishoraDbContext context)
        {
            _context = context;
        }

        // --- THIS IS THE CORRECTED METHOD ---

        [HttpGet("{businessId}/schedules")] // Matches the Android @GET request
        [Produces("application/json")]
        [AllowAnonymous] // Make it public for the customer app
        public async Task<ActionResult<List<ScheduleItem>>> GetBusinessSchedules(long businessId)
        {
            try
            {
                // 1. Get today's date (server-side)
                // Use DateOnly to match your database
                var today = DateOnly.FromDateTime(DateTime.UtcNow.Date);

                // 2. Query your database
                var schedules = await _context.preorder_schedules // Use your actual table name
                    .Where(s => s.business_id == businessId && // Use DB column names
                                s.is_active == true &&
                                s.available_date >= today)
                    .OrderBy(s => s.available_date)
                    // 3. Map the DB model to your new ScheduleItem DTO
                    .Select(s => new ScheduleItem
                    {
                        ScheduleId = s.schedule_id,
                        AvailableDate = s.available_date.ToString("yyyy-MM-dd"), // Format DateOnly to string
                        MaxOrders = s.max_orders,
                        CurrentOrderCount = s.current_order_count, // This now matches your DTO
                        IsActive = s.is_active ?? false,
                        BusinessId = s.business_id
                    })
                    .ToListAsync();

                // 4. Check if any schedules were found
                if (schedules == null || !schedules.Any())
                {
                    // Return an empty list, not an error. The app can handle this.
                    return Ok(new List<ScheduleItem>());
                }

                // 5. Return the DTO list.
                return Ok(schedules);
            }
            catch (Exception ex)
            {
                // Log the error
                // Consider logging ex.Message for debugging
                return StatusCode(500, "An internal server error occurred.");
            }
        }
    }
}