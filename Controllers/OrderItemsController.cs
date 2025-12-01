using Amazon.S3;
using Amazon.S3.Transfer;
using Amazon.SimpleNotificationService;
using Amazon.SimpleNotificationService.Model;
using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Linq;
using System.Security.Claims;
using System.Text.Json;
using System.Threading.Tasks;

namespace Dishora.Controllers
{
    [ApiController]
    [Authorize(Roles = "Vendor")]
    [Route("api/[controller]")]
    public class OrderItemsController : ControllerBase
    {
        private readonly DishoraDbContext _context;
        private readonly IAmazonSimpleNotificationService _snsService;
        private readonly ILogger<OrderItemsController> _logger;
        private readonly IAmazonS3 _s3Client;
        private readonly string _platformArn;
        private readonly string _bucketName;

        public OrderItemsController(DishoraDbContext context, IAmazonSimpleNotificationService snsService, IConfiguration config, ILogger<OrderItemsController> logger, IAmazonS3 s3Client)
        {
            _context = context;
            _snsService = snsService;
            _logger = logger;
            _s3Client = s3Client;
            _platformArn = config["AWS:SnsPlatformArn"];
            _bucketName = config["AWS:BucketName"];
        }

        // In Controllers/OrderItemsController.cs

        [HttpGet("mine")]
        public async Task<ActionResult<IEnumerable<VendorOrderItemDto>>> GetMyOrderItems(
            [FromQuery] string? status, [FromQuery] bool? isPreOrder)
        {
            var businessIdClaim = User.Claims.FirstOrDefault(c => c.Type == "business_id");
            if (businessIdClaim == null || !long.TryParse(businessIdClaim.Value, out long businessId))
            {
                return Unauthorized("Invalid or missing business ID in token.");
            }

            var query = _context.order_items
                .Include(oi => oi.order)
                    .ThenInclude(o => o.payment_method)
                .Include(oi => oi.order)
                    .ThenInclude(o => o.User)
                // We do NOT need .Include(oi => oi.product) anymore since we removed PrepTime
                .Include(oi => oi.order)
                    .ThenInclude(o => o.delivery_address)
                .Where(oi => oi.order.business_id == businessId);

            if (!string.IsNullOrEmpty(status) && status != "All")
                query = query.Where(o => o.order_item_status == status);

            if (isPreOrder.HasValue)
                query = query.Where(o => o.is_pre_order == isPreOrder);

            var result = await query
                .OrderByDescending(o => o.created_at)
                .Select(oi => new VendorOrderItemDto
                {
                    OrderItemId = oi.order_item_id,
                    OrderId = oi.order_id,
                    ProductName = oi.product_name,
                    Quantity = oi.quantity,
                    PriceAtOrderTime = oi.price_at_order_time,
                    OrderItemStatus = oi.order_item_status,
                    CreatedAt = oi.created_at,
                    PaymentMethodName = oi.order.payment_method.method_name,
                    CustomerFullName = oi.order.User.fullname,

                    ContactNumber = oi.order.delivery_address.FirstOrDefault() != null
                        ? oi.order.delivery_address.FirstOrDefault().phone_number
                        : "N/A",

                    DeliveryAddress = oi.order.delivery_address.FirstOrDefault() != null
                        ? oi.order.delivery_address.FirstOrDefault().full_address
                        : "Pickup Order",

                    DeliveryDate = oi.order.delivery_date.ToDateTime(new TimeOnly(0, 0)),

                    DeliveryTime = oi.order.delivery_time,

                    // ✅ MAP PROOF OF DELIVERY (This is the only change in Select)
                    ProofOfDelivery = oi.order.proof_of_delivery
                })
                .ToListAsync();

            return result;
        }


        [HttpPost("{orderId}/upload-proof")]
        public async Task<IActionResult> UploadProofOfDelivery(long orderId, IFormFile file)
        {
            if (file == null || file.Length == 0)
                return BadRequest("No file uploaded.");

            var order = await _context.orders.FindAsync(orderId);
            if (order == null) return NotFound("Order not found.");

            // Basic Validation: Check if this order belongs to the logged-in vendor
            var businessIdClaim = User.Claims.FirstOrDefault(c => c.Type == "business_id");
            if (businessIdClaim == null || long.Parse(businessIdClaim.Value) != order.business_id)
                return Unauthorized("This order does not belong to your business.");

            try
            {
                // 1. Generate unique filename
                var fileExtension = Path.GetExtension(file.FileName);
                var keyName = $"proofs/order_{orderId}_{Guid.NewGuid()}{fileExtension}";

                // 2. Upload to S3
                using (var newMemoryStream = new MemoryStream())
                {
                    await file.CopyToAsync(newMemoryStream);
                    var uploadRequest = new TransferUtilityUploadRequest
                    {
                        InputStream = newMemoryStream,
                        Key = keyName,
                        BucketName = _bucketName,
                        CannedACL = S3CannedACL.PublicRead // Or Private if you use presigned URLs
                    };

                    var fileTransferUtility = new TransferUtility(_s3Client);
                    await fileTransferUtility.UploadAsync(uploadRequest);
                }

                // 3. Construct Public URL (Assuming public read access)
                // If private, you'd generate a PreSignedURL here instead.
                string fileUrl = $"https://{_bucketName}.s3.amazonaws.com/{keyName}";

                // 4. Update Database
                order.proof_of_delivery = fileUrl;
                await _context.SaveChangesAsync();

                return Ok(new { url = fileUrl });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error uploading proof of delivery to S3");
                return StatusCode(500, "Internal server error uploading file.");
            }
        }



        // ... (The UpdateOrderItemStatus method remains unchanged)
        [HttpPut("{id}/status")]
        public async Task<IActionResult> UpdateOrderItemStatus(long id, [FromBody] StatusUpdateRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.Status))
                return BadRequest("Status is required.");

            using var transaction = await _context.Database.BeginTransactionAsync();
            try
            {
                var item = await _context.order_items
                    .Include(oi => oi.order) // We MUST include the order
                    .Include(oi => oi.order).ThenInclude(o => o.business)
                    .FirstOrDefaultAsync(oi => oi.order_item_id == id);

                if (item == null) return NotFound();

                var allowed = new[] { "Pending", "Preparing", "For Delivery", "Completed", "Cancelled" };
                if (!allowed.Contains(request.Status))
                    return BadRequest("Invalid status value.");

                // 1. Update the item status
                item.order_item_status = request.Status;
                item.updated_at = DateTime.UtcNow;

                // --- NOTIFICATION LOGIC (MOVED BEFORE SAVE) ---

                // 2. Get the customer's User ID
                long customerUserId = item.order.user_id;
                string businessName = item.order.business?.business_name ?? "Vendor"; // ✅ GET NAME

                // 3. Get the Vendor's User ID (the person doing the action)
                var vendorUserIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                long.TryParse(vendorUserIdClaim, out var vendorActorId); // This is the 'actor_user_id'

                // 4. (Your existing push notification logic is good)
                // We'll run this task, but we won't wait for it
                await SendStatusUpdateNotification(customerUserId, item);

                // 5. (MANDATORY) Create the In-App Notification
                var payload = new
                {
                    title = "Order Update",
                    message = $"Your ordered product '{item.product_name}' from {businessName} is now {item.order_item_status}."
                };

                var inAppNotification = new notifications
                {
                    user_id = customerUserId,           // The ID of the CUSTOMER
                    actor_user_id = vendorActorId,      // The ID of the VENDOR
                    event_type = "order_status_changed",
                    reference_table = "orders",         // Link it to the 'orders' table
                    reference_id = item.order_id,       // Link it to this specific order
                    payload = JsonSerializer.Serialize(payload),
                    is_read = false,
                    channel = "in_app",
                    created_at = DateTime.UtcNow,
                    recipient_role = "customer"
                };
                _context.notifications.Add(inAppNotification);

                // 6. Save BOTH the status change AND the new notification
                await _context.SaveChangesAsync();

                // 7. Commit the transaction
                await transaction.CommitAsync();

                _logger.LogInformation($"Successfully updated and notified for OrderItem {id}.");
            }
            catch (Exception ex)
            {
                await transaction.RollbackAsync(); // Roll back all changes if anything fails
                _logger.LogError(ex, $"Failed to update status or send notification for OrderItem {id}.");
                return StatusCode(500, new { message = "An error occurred." });
            }
            // --- END OF FIX ---

            return Ok(new { updatedStatus = request.Status });
        }



        [HttpPut("group-status")]
        public async Task<IActionResult> UpdateGroupStatus([FromBody] GroupStatusUpdateRequest request)
        {
            // 1. Find all items for this order.
            // --- MODIFIED QUERY ---
            // Need to Include the order to get the customer's User ID
            using var transaction = await _context.Database.BeginTransactionAsync();
            try
            {
                var items = await _context.order_items
                    .Include(oi => oi.order)
                        .ThenInclude(o => o.User) // Existing include for User
                    .Include(oi => oi.order)
                        .ThenInclude(o => o.business) // ✅ ADDED: Include Business to get name
                    .Where(oi => oi.order_id == request.OrderId)
                    .ToListAsync();
                    /* .Include(oi => oi.order)
                    .Where(oi => oi.order_id == request.OrderId)
                    .ToListAsync(); */

                if (items == null || !items.Any())
                {
                    return NotFound(new { message = "Order not found." });
                }

                var firstItem = items.First(); // Get info from the first item

                string businessName = firstItem.order.business?.business_name ?? "Vendor";

                // 1. Get the customer's User ID
                long customerUserId = firstItem.order.user_id;

                // 2. Get the Vendor's User ID (the actor)
                var vendorUserIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                long.TryParse(vendorUserIdClaim, out var vendorActorId);

                // 3. Loop and update all items
                foreach (var item in items)
                {
                    item.order_item_status = request.NewStatus;
                    item.updated_at = DateTime.UtcNow;
                }

                // 4. (Your existing push notification logic)
                var notificationItem = new order_items
                {
                    product_name = $"Your order from {businessName} with Order ID: {firstItem.order_id})",
                    order_item_status = request.NewStatus,
                    order_item_id = firstItem.order_item_id
                };
                await SendStatusUpdateNotification(customerUserId, notificationItem);

                // 5. (MANDATORY) Create the In-App Notification
                var payload = new
                {
                    title = "Order Update",
                    message = $"Your order from {businessName} with Order ID: {firstItem.order_id} is now {request.NewStatus}."
                };

                var inAppNotification = new notifications
                {
                    user_id = customerUserId,
                    actor_user_id = vendorActorId,
                    event_type = "order_status_changed",
                    reference_table = "orders",
                    reference_id = firstItem.order_id,
                    payload = JsonSerializer.Serialize(payload),
                    is_read = false,
                    channel = "in_app",
                    created_at = DateTime.UtcNow,
                    recipient_role = "customer"
                };
                _context.notifications.Add(inAppNotification);

                // 6. Save ALL changes (all items + new notification)
                await _context.SaveChangesAsync();

                // 7. Commit the transaction
                await transaction.CommitAsync();

                _logger.LogInformation($"Successfully triggered group notification for Order {request.OrderId}.");
            }
            catch (Exception ex)
            {
                await transaction.RollbackAsync();
                _logger.LogError(ex, $"Failed to send group notification for Order {request.OrderId}.");
                return StatusCode(500, new { message = "An error occurred." });
            }
            // --- END OF FIX ---

            return Ok(new { message = "Order updated successfully" });
        }









        private async Task SendStatusUpdateNotification(long customerUserId, order_items orderItem)
        {
            // 1. Find all active devices for the customer
            var customerTokens = await _context.device_tokens
                .Where(t => t.user_id == customerUserId && t.is_active)
                .ToListAsync();

            if (customerTokens.Count == 0)
            {
                _logger.LogWarning($"No active device tokens found for customer {customerUserId}. Notification not sent.");
                return;
            }

            // 2. Create the "data" payload
            var dataPayload = new
            {
                type = "order_status_update",
                order_item_id = orderItem.order_item_id.ToString(),
                new_status = orderItem.order_item_status,
                title = "Order Update",
                body = $"Your order '{orderItem.product_name}' is now {orderItem.order_item_status}."
            };

            // 3. Create the full SNS message
            var options = new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };
            var snsMessage = JsonSerializer.Serialize(new
            {
                GCM = JsonSerializer.Serialize(new { data = dataPayload }, options)
            }, options);

            // 4. Send to all devices for that customer
            _logger.LogInformation($"Sending order status notification to {customerTokens.Count} device(s) for user {customerUserId}.");
            foreach (var token in customerTokens)
            {
                try
                {
                    var publishRequest = new PublishRequest
                    {
                        TargetArn = token.sns_endpoint_arn,
                        Message = snsMessage,
                        MessageStructure = "json"
                    };
                    await _snsService.PublishAsync(publishRequest);
                }
                catch (EndpointDisabledException ex)
                {
                    _logger.LogWarning(ex, $"Endpoint {token.sns_endpoint_arn} is disabled. Marking as inactive.");
                    token.is_active = false;
                    await _context.SaveChangesAsync(); // Save the inactive status
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, $"Failed to send to endpoint {token.sns_endpoint_arn}.");
                }
            }
        }
    }
}