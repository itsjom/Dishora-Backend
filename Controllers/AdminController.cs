using Dishora.Data;
using Dishora.Models; // --- MAKE SURE 'notifications' MODEL IS IMPORTED ---
using Dishora.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Text.Json; // --- ADD THIS FOR JSON PAYLOAD ---

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/admin")]
    [Authorize(Roles = "Admin")]
    public class AdminController : ControllerBase
    {
        private readonly DishoraDbContext _context;
        private readonly INotificationService _notificationService;

        public AdminController(DishoraDbContext context, INotificationService notificationService)
        {
            _context = context;
            _notificationService = notificationService;
        }

        [HttpPost("vendors/{vendorId}/approve")]
        public async Task<IActionResult> ApproveVendor(long vendorId)
        {
            var vendor = await _context.vendors.FindAsync(vendorId);
            if (vendor == null)
            {
                return NotFound(new { success = false, message = "Vendor not found." });
            }

            vendor.registration_status = "Approved";

            var business = await _context.business_details.FirstOrDefaultAsync(b => b.vendor_id == vendorId);
            if (business != null)
            {
                business.verification_status = "Verified";
            }

            // 1. SEND THE PUSH NOTIFICATION (You already have this)
            string title = "Your registration is approved!";
            string body = "Congratulations! You can now start selling on Dishora.";
            await _notificationService.SendVendorStatusUpdateAsync(vendor.user_id, "Approved", title, body);

            // --- ADD THIS BLOCK ---
            // 2. CREATE THE PERSISTENT IN-APP NOTIFICATION
            var payload = new
            {
                title = title,
                message = body
            };

            var inAppNotification = new notifications
            {
                user_id = vendor.user_id,
                event_type = "VENDOR_APPROVED", // This is the key our Android app will look for
                payload = JsonSerializer.Serialize(payload),
                is_read = false, // <-- Crucial
                channel = "IN_APP",
                created_at = DateTime.UtcNow
            };
            _context.notifications.Add(inAppNotification);
            // --- END OF BLOCK ---

            // 3. Save all changes (vendor status AND new notification)
            await _context.SaveChangesAsync();

            return Ok(new { success = true, message = "Vendor approved and user has been notified." });
        }

        // ... (We should add the same logic to your RejectVendor method) ...
        [HttpPost("vendors/{vendorId}/reject")]
        public async Task<IActionResult> RejectVendor(long vendorId)
        {
            // ... (your existing logic to find vendor and business) ...

            var vendor = await _context.vendors.FindAsync(vendorId);
            vendor.registration_status = "Rejected";

            // 1. SEND THE PUSH NOTIFICATION
            string title = "Your registration update";
            string body = "Unfortunately, your vendor registration was not approved at this time.";
            await _notificationService.SendVendorStatusUpdateAsync(vendor.user_id, "Rejected", title, body);

            // --- ADD THIS BLOCK ---
            // 2. CREATE THE PERSISTENT IN-APP NOTIFICATION
            var payload = new
            {
                title = title,
                message = body
            };

            var inAppNotification = new notifications
            {
                user_id = vendor.user_id,
                event_type = "VENDOR_REJECTED", // A new key for the "Rejected" event
                payload = JsonSerializer.Serialize(payload),
                is_read = false,
                channel = "IN_APP",
                created_at = DateTime.UtcNow
            };
            _context.notifications.Add(inAppNotification);
            // --- END OF BLOCK ---

            // 3. Save all changes
            await _context.SaveChangesAsync();

            return Ok(new { success = true, message = "Vendor rejected and user has been notified." });
        }
    }
}
