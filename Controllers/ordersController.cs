using Dishora.DTO;
using Dishora.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace Dishora.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    // [Authorize] // Protects all endpoints in this controller
    public class OrdersController : ControllerBase
    {
        private readonly IOrderService _orderService;

        public OrdersController(IOrderService orderService)
        {
            _orderService = orderService;
        }

        // =====================================================
        //  CREATE ORDER
        //  POST /api/orders
        // =====================================================
        [HttpPost]
        public async Task<IActionResult> CreateOrder([FromBody] OrderRequest request)
        {
            if (request == null || request.Items == null || !request.Items.Any())
                return BadRequest("Invalid order request.");

            try
            {
                var orderId = await _orderService.CreateOrderAsync(request);
                return Ok(new { orderId, status = "Created" });
            }
            catch (Exception ex)
            {
                // In production, use a real logger
                Console.WriteLine($"CreateOrder failed: {ex}");
                return StatusCode(500, new { error = "An unexpected error occurred." });
            }
        }

        // =====================================================
        //  GET MY ORDERS (This fixes your 404 error)
        //  GET /api/orders/my-orders
        // =====================================================
        [HttpGet("my-orders")]
        public async Task<ActionResult<IEnumerable<OrderDto>>> GetMyOrders()
        {
            var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);

            if (string.IsNullOrEmpty(userIdString))
            {
                return Unauthorized("User ID not found in token.");
            }

            if (!long.TryParse(userIdString, out var userId))
            {
                return BadRequest("Invalid user ID format.");
            }

            var orders = await _orderService.GetOrdersByUserIdAsync(userId);
            return Ok(orders);
        }

        // =====================================================
        //  GET ORDER DETAILS
        //  GET /api/orders/5 (or any ID)
        // =====================================================
        [HttpGet("{id}")]
        public async Task<ActionResult<OrderDetailDto>> GetOrderDetails(long id)
        {
            var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!long.TryParse(userIdString, out var userId))
            {
                return BadRequest("Invalid user ID format.");
            }

            var orderDetails = await _orderService.GetOrderDetailsByIdAsync(id, userId);

            if (orderDetails == null)
            {
                // Returns 404 if the order doesn't exist OR doesn't belong to the user
                return NotFound();
            }

            return Ok(orderDetails);
        }

        // =====================================================
        //  CANCEL AN ORDER
        //  POST /api/orders/5/cancel
        // =====================================================
        [HttpPost("{id}/cancel")]
        public async Task<IActionResult> CancelOrder(long id)
        {
            var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!long.TryParse(userIdString, out var userId))
            {
                return BadRequest("Invalid user ID format.");
            }

            var success = await _orderService.CancelOrderAsync(id, userId);

            if (!success)
            {
                // Happens if order is not "Pending" or doesn't exist
                return BadRequest(new { message = "Order could not be cancelled." });
            }

            return Ok(new { message = "Order successfully cancelled." });
        }
    }
}