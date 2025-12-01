using Dishora.DTO;
using Dishora.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace Dishora.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize] // All endpoints in this controller require a valid login
    public class PreOrdersController : ControllerBase
    {
        private readonly IPreOrderService _preOrderService;

        public PreOrdersController(IPreOrderService preOrderService)
        {
            _preOrderService = preOrderService;
        }

        // =====================================================
        //  GET /api/preorders/vendor
        //  This is the endpoint your app is trying to call.
        // =====================================================
        [HttpGet("vendor")]
        [Authorize(Roles = "Vendor")] // Ensures only users with the "Vendor" role can access
        public async Task<ActionResult<IEnumerable<GroupedPreOrderDto>>> GetVendorPreOrders()
        {
            // We get the user's ID from their JWT token
            var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!long.TryParse(userIdString, out var vendorUserId))
            {
                return Unauthorized("Invalid user token.");
            }

            try
            {
                // This calls the service we built to get the data
                var preOrders = await _preOrderService.GetGroupedPreOrdersForVendorAsync(vendorUserId);
                return Ok(preOrders);
            }
            catch (Exception ex)
            {
                // In production, you should log this 'ex' variable
                return StatusCode(500, new { error = "An error occurred fetching pre-orders." });
            }
        }

        // =====================================================
        //  PUT /api/preorders/{groupId}/status?newStatus=...
        //  This is the endpoint for your "Accept" / "Cancel" buttons.
        // =====================================================
        [HttpPut("{groupId}/status")]
        [Authorize(Roles = "Vendor")] // Ensures only vendors can update
        public async Task<IActionResult> UpdatePreOrderStatus(string groupId, [FromQuery] string newStatus)
        {
            if (string.IsNullOrEmpty(newStatus))
            {
                return BadRequest("New status is required.");
            }

            var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!long.TryParse(userIdString, out var vendorUserId))
            {
                return Unauthorized("Invalid user token.");
            }

            try
            {
                // This calls the service to update the database
                var success = await _preOrderService.UpdateOrderStatusAsync(groupId, newStatus, vendorUserId);

                if (!success)
                {
                    return NotFound(new { message = "Order not found or update failed." });
                }

                // 204 No Content is the standard, successful response for a PUT
                return NoContent();
            }
            catch (Exception ex)
            {
                // In production, you should log this 'ex' variable
                return StatusCode(500, new { error = "An error occurred updating the status." });
            }
        }
    }
}
