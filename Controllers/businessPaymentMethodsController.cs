
using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api")]
    public class BusinessPaymentMethodsController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public BusinessPaymentMethodsController(DishoraDbContext context)
        {
            _context = context;
        }

        [HttpGet("business/{businessId}/payment-methods")]
        public async Task<IActionResult> GetBusinessPaymentMethods(long businessId)
        {
            var methods = await _context.payment_methods // Start from master table
                .Where(pm => pm.status == "active")
                .Select(pm => new
                {
                    // Get the master method
                    MasterMethod = pm,
                    // Find the link table entry (if it exists)
                    BusinessLink = pm.BusinessPaymentMethods
                        .FirstOrDefault(bpm => bpm.business_id == businessId)
                })
                .Select(x => new PaymentMethodDto
                {
                    // Get details from the master method
                    master_method_id = x.MasterMethod.payment_method_id,
                    method_name = x.MasterMethod.method_name,
                    description = x.MasterMethod.description,

                    // Check if the link exists
                    enabled = (x.BusinessLink != null),

                    // Get details from the link (if it exists)
                    // This is the junction table ID (business_payment_method_id)
                    payment_method_id = (x.BusinessLink != null) ? x.BusinessLink.business_payment_method_id : 0,

                    // Use the 'BusinessPmDetails' navigation property we fixed
                    account_name = (x.BusinessLink != null && x.BusinessLink.business_pm_detail != null)
                                     ? x.BusinessLink.business_pm_detail.account_name : null,
                    account_number = (x.BusinessLink != null && x.BusinessLink.business_pm_detail != null)
                                     ? x.BusinessLink.business_pm_detail.account_number : null
                })
                .ToListAsync();

            return Ok(methods);
        }

        // POST /api/business/{businessId}/payment-methods
        [HttpPost("business/{businessId}/payment-methods")]
        public async Task<IActionResult> UpdateBusinessPaymentMethods(
            long businessId,
            [FromBody] UpdatePaymentMethodsDto dto)
        {
            // 1. Get the current state:
            // Fetch all methods currently linked to this business
            var existingMethods = await _context.business_payment_methods
                .Where(bpm => bpm.business_id == businessId)
                .ToListAsync();

            // 2. Get the new desired state:
            // This is the list of MASTER method IDs that should be enabled.
            // Ensure dto.Payment_Methods is not null.
            var desiredMasterIds = dto.Payment_Methods ?? new List<long>();

            // 3. Find what to delete:
            // Find records that are in the database BUT NOT in the new list from the app.
            // These are the methods the user just *disabled*.
            var methodsToDelete = existingMethods
                .Where(em => !desiredMasterIds.Contains(em.payment_method_id))
                .ToList();

            // 4. Find what to add:
            // Get a simple list of master IDs that are *already* in the database.
            var existingMasterIds = existingMethods
                .Select(em => em.payment_method_id)
                .ToHashSet();

            var methodsToAdd = new List<business_payment_methods>();

            // Loop through the new list from the app
            foreach (var desiredId in desiredMasterIds)
            {
                // If a desired ID is NOT in the database, it's new.
                if (!existingMasterIds.Contains(desiredId))
                {
                    // This is a method the user just *enabled*.
                    methodsToAdd.Add(new business_payment_methods
                    {
                        business_id = businessId,
                        payment_method_id = desiredId, // This is the master method ID
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    });
                }
            }

            // 5. Perform the database operations
            if (methodsToDelete.Any())
            {
                // This will trigger the cascade delete ONLY for the disabled items
                _context.business_payment_methods.RemoveRange(methodsToDelete);
            }

            if (methodsToAdd.Any())
            {
                await _context.business_payment_methods.AddRangeAsync(methodsToAdd);
            }

            // 6. Save all changes in one transaction
            await _context.SaveChangesAsync();

            // Return a more informative response
            return Ok(new
            {
                success = true,
                methodsAdded = methodsToAdd.Count,
                methodsRemoved = methodsToDelete.Count
            });
        }

        [HttpPost("business/payment-methods/details")]
        public async Task<IActionResult> SavePaymentMethodDetails([FromBody] PaymentDetailsDto dto)
        {
            if (dto == null)
            {
                return BadRequest(new { message = "No data provided." });
            }

            // 1. Safety check: Ensure the parent record exists
            var parentPaymentMethod = await _context.business_payment_methods
                .FindAsync(dto.business_payment_method_id);

            if (parentPaymentMethod == null)
            {
                return NotFound(new { message = "Payment method link not found." });
            }

            // 2. Find or create the details record (Upsert)
            var existingDetails = await _context.business_pm_details
                .FirstOrDefaultAsync(d => d.business_payment_method_id == dto.business_payment_method_id);

            if (existingDetails != null)
            {
                // Update existing
                existingDetails.account_name = dto.account_name;
                existingDetails.account_number = dto.account_number;
                existingDetails.updated_at = DateTime.UtcNow;
            }
            else
            {
                // Create new
                var newDetails = new business_pm_details
                {
                    business_payment_method_id = dto.business_payment_method_id,
                    account_name = dto.account_name,
                    account_number = dto.account_number,
                    is_active = true,
                    created_at = DateTime.UtcNow,
                    updated_at = DateTime.UtcNow
                };
                _context.business_pm_details.Add(newDetails);
            }

            // 3. Save to database
            await _context.SaveChangesAsync();
            return Ok(new { message = "Details saved successfully." });
        }
    }
}
