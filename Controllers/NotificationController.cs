using Dishora.Data;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace Dishora.Controllers
{
    [Authorize] // <-- Requires user to be logged in
    [ApiController]
    [Route("api/[controller]")]
    public class NotificationsController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public NotificationsController(DishoraDbContext context)
        {
            _context = context;
        }

        [HttpGet("all")]
        public async Task<ActionResult<IEnumerable<notifications>>> GetAllNotifications([FromQuery] string role = "customer")
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!long.TryParse(userIdClaim, out var userId))
            {
                return Unauthorized();
            }

            var query = _context.notifications.Where(n => n.user_id == userId);
            if (role.Equals("vendor", StringComparison.OrdinalIgnoreCase))
            {
                query = query.Where(n => n.recipient_role == "vendor");
            }
            else
            {
                query = query.Where(n => n.recipient_role == "customer");
            }

            var allNotifications = await query 
                .OrderByDescending(n => n.created_at) // Show newest first
                .Take(50) // Only get the 50 most recent
                .ToListAsync();

            return Ok(allNotifications);
        }

        // This is the endpoint our Android app will call
        // GET: /api/notifications/unread
        [HttpGet("unread")]
        public async Task<ActionResult<IEnumerable<notifications>>> GetUnreadNotifications([FromQuery] string role = "customer")
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!long.TryParse(userIdClaim, out var userId))
            {
                return Unauthorized();
            }

            var query = _context.notifications
                .Where(n => n.user_id == userId && !n.is_read);

            if (role.Equals("vendor", StringComparison.OrdinalIgnoreCase))
            {
                query = query.Where(n => n.recipient_role == "vendor");
            }
            else
            {
                query = query.Where(n => n.recipient_role == "customer");
            }

            var unreadNotifications = await query
                .OrderBy(n => n.created_at) // Show oldest first
                .ToListAsync();

            return Ok(unreadNotifications);
        }

        [HttpGet("unread-count")]
        public async Task<ActionResult<object>> GetUnreadCount([FromQuery] string role = "customer")
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!long.TryParse(userIdClaim, out var userId))
            {
                return Unauthorized();
            }

            var query = _context.notifications
                .Where(n => n.user_id == userId && !n.is_read);

            if (role.Equals("vendor", StringComparison.OrdinalIgnoreCase))
            {
                query = query.Where(n => n.recipient_role == "vendor");
            }
            else
            {
                query = query.Where(n => n.recipient_role == "customer");
            }

            // This is very fast - it just runs a COUNT(*) query
            var unreadCount = await query.CountAsync();

            // Return a simple JSON object: { "unreadCount": 5 }
            return Ok(new { unreadCount = unreadCount });
        }

        [HttpGet("unread-order-count")]
        public async Task<ActionResult<object>> GetUnreadOrderCount([FromQuery] string role = "customer")
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!long.TryParse(userIdClaim, out var userId))
            {
                return Unauthorized();
            }

            // 1. Base query for unread notifications for this user/role
            var query = _context.notifications
                .Where(n => n.user_id == userId &&
                            !n.is_read &&
                            n.recipient_role == role);

            // 2. Define which event types count for the "Order" badge
            var orderEventTypes = new List<string> {
                "new_order_received",
                "order_status_changed",
                "order_confirmed",
                "order_created" 
                // Add any other order-related event_types here
            };

            // 3. Filter the query to only include those event types
            query = query.Where(n => orderEventTypes.Contains(n.event_type));

            // 4. Get the final count (this is a very fast database query)
            var unreadOrderCount = await query.CountAsync();

            // 5. Return the simple JSON object that your DTO expects
            // This returns: { "unreadCount": 5 }
            return Ok(new { unreadCount = unreadOrderCount });
        }

        [HttpPost("mark-all-read")]
        public async Task<IActionResult> MarkAllNotificationsAsRead([FromQuery] string role = "customer")
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!long.TryParse(userIdClaim, out var userId))
            {
                return Unauthorized();
            }

            var query = _context.notifications
                .Where(n => n.user_id == userId && !n.is_read);

            if (role.Equals("vendor", StringComparison.OrdinalIgnoreCase))
            {
                query = query.Where(n => n.recipient_role == "vendor");
            }
            else
            {
                query = query.Where(n => n.recipient_role == "customer");
            }

            // This is the most efficient way to update many rows.
            // It runs one SQL UPDATE command.
            var rowsAffected = await query
                .ExecuteUpdateAsync(s => s.SetProperty(n => n.is_read, true));

            return Ok(new { success = true, rows_updated = rowsAffected });
        }

        // This is the endpoint our Android app will call after showing the popup
        // POST: /api/notifications/123/mark-read
        [HttpPost("{id}/mark-read")]
        public async Task<IActionResult> MarkNotificationAsRead(long id)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!long.TryParse(userIdClaim, out var userId))
            {
                return Unauthorized();
            }

            var notification = await _context.notifications.FindAsync(id);

            if (notification == null)
            {
                return NotFound();
            }

            // Security check: Make sure this user owns this notification
            if (notification.user_id != userId)
            {
                return Forbid(); // User is trying to mark someone else's notification
            }

            notification.is_read = true;
            // notification.updated_at = DateTime.UtcNow; // Optional, but good practice

            await _context.SaveChangesAsync();

            return Ok(new { success = true, message = "Notification marked as read." });
        }
    }
}
