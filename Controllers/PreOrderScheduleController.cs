using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Linq;
using System.Security.Claims;

namespace Dishora.Controllers
{
    [Route("api/vendor/schedule")]
    [ApiController]
    [Authorize(Roles = "Vendor")]
    public class PreOrderScheduleController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public PreOrderScheduleController(DishoraDbContext context)
        {
            _context = context;
        }

        private long GetCurrentBusinessId()
        {
            // ⚠️ Use the actual claim key name that stores the business ID.
            // Example keys are "business_id", ClaimTypes.NameIdentifier, or a custom one.
            var businessIdClaim = User.FindFirstValue("business_id");

            if (businessIdClaim == null || !long.TryParse(businessIdClaim, out long businessId))
            {
                // This exception will result in a 401/403 response if the claim is missing/invalid
                throw new UnauthorizedAccessException("Business ID claim is missing or invalid in the token.");
            }
            return businessId;
        }

        // ------------------------------------------------------------------
        // GET: /api/vendor/schedule
        // Purpose: Fetch all active schedules for the current vendor's business.
        // ------------------------------------------------------------------
        [HttpGet]
        public async Task<ActionResult<IEnumerable<PreOrderScheduleResponseDto>>> GetPreOrderSchedules()
        {
            long currentBusinessId;
            try
            {
                currentBusinessId = GetCurrentBusinessId();
            }
            catch (UnauthorizedAccessException ex)
            {
                return Unauthorized(ex.Message);
            }

            var schedules = await _context.preorder_schedules
                // ✅ FILTER FIX: Use the dynamically retrieved business ID
                .Where(s => s.business_id == currentBusinessId && s.is_active == true)
                .OrderBy(s => s.available_date)
                .Select(s => new PreOrderScheduleResponseDto
                {
                    ScheduleId = s.schedule_id,
                    AvailableDate = s.available_date.ToString("yyyy-MM-dd"),
                    MaxOrders = s.max_orders,
                    CurrentOrderCount = s.current_order_count,
                    IsActive = s.is_active ?? false,
                    BusinessId = s.business_id
                })
                .ToListAsync();

            return Ok(schedules);
        }

        // ------------------------------------------------------------------
        // POST: /api/vendor/schedule
        // Purpose: Create a new schedule entry.
        // ------------------------------------------------------------------
        [HttpPost]
        public async Task<ActionResult<PreOrderScheduleResponseDto>> PostPreOrderSchedule([FromBody] PreOrderScheduleCreateDto dto)
        {
            long currentBusinessId;
            try
            {
                currentBusinessId = GetCurrentBusinessId();
            }
            catch (UnauthorizedAccessException ex)
            {
                return Unauthorized(ex.Message);
            }

            // 1. Check if a schedule already exists for this date AND this business
            var existingSchedule = await _context.preorder_schedules
                // ✅ FILTER FIX: Use the dynamically retrieved business ID
                .FirstOrDefaultAsync(s => s.business_id == currentBusinessId && s.available_date == dto.AvailableDate);

            if (existingSchedule != null)
            {
                // Update the existing schedule
                existingSchedule.max_orders = dto.MaxOrders;
                existingSchedule.updated_at = DateTime.UtcNow;
                await _context.SaveChangesAsync();

                return Ok(new PreOrderScheduleResponseDto
                {
                    ScheduleId = existingSchedule.schedule_id,
                    AvailableDate = existingSchedule.available_date.ToString("yyyy-MM-dd"),
                    MaxOrders = existingSchedule.max_orders,
                    CurrentOrderCount = existingSchedule.current_order_count,
                    IsActive = existingSchedule.is_active ?? false,
                    BusinessId = existingSchedule.business_id
                });
            }

            // 2. Create the new schedule entry
            var schedule = new preorder_schedule
            {
                business_id = currentBusinessId, // ✅ ASSIGNMENT FIX: Use the dynamically retrieved business ID
                available_date = dto.AvailableDate,
                max_orders = dto.MaxOrders,
                current_order_count = 0,
                is_active = true,
                created_at = DateTime.UtcNow,
                updated_at = DateTime.UtcNow
            };

            _context.preorder_schedules.Add(schedule);
            await _context.SaveChangesAsync();

            var responseDto = new PreOrderScheduleResponseDto
            {
                ScheduleId = schedule.schedule_id,
                AvailableDate = schedule.available_date.ToString("yyyy-MM-dd"),
                MaxOrders = schedule.max_orders,
                CurrentOrderCount = schedule.current_order_count,
                IsActive = schedule.is_active ?? false,
                BusinessId = schedule.business_id
            };

            return CreatedAtAction(nameof(GetPreOrderSchedules), responseDto);
        }
    }
}