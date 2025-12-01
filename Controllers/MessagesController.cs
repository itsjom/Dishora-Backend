using Amazon.S3; // ✅ S3
using Amazon.S3.Model; // ✅ S3 Model
using Dishora.Data;
using Dishora.DTO;
using Dishora.Hubs;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration; // Needed for config
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Claims;
using System.Text.Json;
using System.Threading.Tasks;

namespace Dishora.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class MessagesController : ControllerBase
    {
        private readonly DishoraDbContext _context;
        private readonly IHubContext<ChatHub> _hubContext;
        private readonly IAmazonS3 _s3Client; // ✅ Inject S3 Client
        private readonly IConfiguration _config; // ✅ Inject Config (to get bucket name)

        public MessagesController(DishoraDbContext context, IHubContext<ChatHub> hubContext, IAmazonS3 s3Client, IConfiguration config)
        {
            _context = context;
            _hubContext = hubContext;
            _s3Client = s3Client;
            _config = config;
        }

        // ---
        // 🛠️ HELPER: S3 UPLOADER
        // ---
        private async Task<string> SaveImageAsync(IFormFile imageFile)
        {
            if (imageFile == null || imageFile.Length == 0) return null;

            // 1. Get Bucket Name from your appsettings.json
            // Make sure your appsettings.json has "AWS": { "BucketName": "..." }
            string bucketName = _config["AWS:BucketName"];
            if (string.IsNullOrEmpty(bucketName)) throw new Exception("AWS BucketName is not configured.");

            // 2. Generate unique filename
            string keyName = $"chat-images/{Guid.NewGuid()}{Path.GetExtension(imageFile.FileName)}";

            // 3. Upload to S3
            using (var newMemoryStream = new MemoryStream())
            {
                await imageFile.CopyToAsync(newMemoryStream);

                var putRequest = new PutObjectRequest
                {
                    InputStream = newMemoryStream,
                    BucketName = bucketName,
                    Key = keyName,
                    CannedACL = S3CannedACL.PublicRead, // Ensure app can read it
                    ContentType = imageFile.ContentType
                };

                await _s3Client.PutObjectAsync(putRequest);
            }

            // 4. Return Full URL
            // Ensure you get the region correctly. If hardcoded in Program.cs, you might need to hardcode it here or pull from config.
            string region = _config["AWS:Region"] ?? "ap-southeast-1";
            return $"https://{bucketName}.s3.{region}.amazonaws.com/{keyName}";
        }

        // ---
        // CUSTOMER ENDPOINTS
        // ---

        [HttpGet("customer/conversations")]
        public async Task<ActionResult<IEnumerable<CustomerConversationDto>>> GetCustomerConversations()
        {
            var customerId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));

            var messages = await _context.messages
                .Where(m => (m.sender_id == customerId && m.sender_role == "customer") ||
                            (m.receiver_id == customerId && m.receiver_role == "customer"))
                .OrderByDescending(m => m.sent_at)
                .ToListAsync();

            var groups = messages.GroupBy(m => (m.sender_id == customerId) ? m.receiver_id : m.sender_id);
            var businessIds = groups.Select(g => g.Key).ToList();

            var businessDetails = await _context.business_details
                .Where(b => businessIds.Contains(b.business_id))
                .ToDictionaryAsync(b => b.business_id);

            var conversations = new List<CustomerConversationDto>();
            foreach (var group in groups)
            {
                if (!businessDetails.TryGetValue(group.Key, out var business)) continue;

                var lastMessage = group.First();
                var unreadCount = group.Count(m => m.receiver_id == customerId && m.is_read == false);

                string previewText = !string.IsNullOrEmpty(lastMessage.message_text)
                    ? lastMessage.message_text
                    : (lastMessage.image_url != null ? "[Image]" : "");

                conversations.Add(new CustomerConversationDto
                {
                    BusinessId = business.business_id,
                    BusinessName = business.business_name,
                    BusinessImageUrl = business.business_image,
                    LastMessage = previewText,
                    LatestMessageTime = lastMessage.sent_at,
                    UnreadCount = unreadCount
                });
            }
            return Ok(conversations.OrderByDescending(c => c.LatestMessageTime));
        }

        [HttpGet("customer/thread/{businessId}")]
        public async Task<ActionResult<IEnumerable<ChatMessageDto>>> GetCustomerMessageThread(long businessId)
        {
            var customerId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));

            var messages = await _context.messages
                .Where(m => (m.sender_id == customerId && m.sender_role == "customer" && m.receiver_id == businessId && m.receiver_role == "business") ||
                            (m.sender_id == businessId && m.sender_role == "business" && m.receiver_id == customerId && m.receiver_role == "customer"))
                .OrderBy(m => m.sent_at)
                .ToListAsync();

            var messageIdsToMark = messages
                .Where(m => m.receiver_id == customerId && m.is_read == false)
                .Select(m => m.message_id)
                .ToList();

            if (messageIdsToMark.Any())
            {
                await _context.messages
                    .Where(m => messageIdsToMark.Contains(m.message_id))
                    .ExecuteUpdateAsync(s => s.SetProperty(m => m.is_read, true));
            }

            var customer = await _context.users.FindAsync(customerId);
            var business = await _context.business_details.FindAsync(businessId);

            var dtos = messages.Select(m => new ChatMessageDto
            {
                message_id = m.message_id,
                sender_id = m.sender_id,
                sender_role = m.sender_role,
                sender_name = m.sender_role == "customer" ? customer?.fullname : business?.business_name,
                message_text = m.message_text,
                image_url = m.image_url,
                sent_at = m.sent_at
            });

            return Ok(dtos);
        }

        // ---
        // VENDOR ENDPOINTS
        // ---

        [HttpGet("vendor/conversations/{businessId}")]
        public async Task<ActionResult<IEnumerable<VendorConversationDto>>> GetVendorConversations(long businessId)
        {
            var vendorUserId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));

            var isOwner = await _context.business_details
                .AnyAsync(b => b.business_id == businessId && b.vendor.user_id == vendorUserId);

            if (!isOwner) return Unauthorized("You do not have access to this business.");

            var messages = await _context.messages
                .Where(m => (m.sender_id == businessId && m.sender_role == "business") ||
                            (m.receiver_id == businessId && m.receiver_role == "business"))
                .OrderByDescending(m => m.sent_at)
                .ToListAsync();

            var groups = messages.GroupBy(m => (m.sender_id == businessId) ? m.receiver_id : m.sender_id);
            var customerIds = groups.Select(g => g.Key).ToList();

            var customers = await _context.users
                .Where(u => customerIds.Contains(u.user_id))
                .ToDictionaryAsync(u => u.user_id);

            var conversations = new List<VendorConversationDto>();
            foreach (var group in groups)
            {
                if (!customers.TryGetValue(group.Key, out var customer)) continue;

                var lastMessage = group.First();
                var unreadCount = group.Count(m => m.receiver_id == businessId && m.is_read == false);

                string previewText = !string.IsNullOrEmpty(lastMessage.message_text)
                   ? lastMessage.message_text
                   : (lastMessage.image_url != null ? "[Image]" : "");

                conversations.Add(new VendorConversationDto
                {
                    CustomerId = customer.user_id,
                    CustomerName = customer.fullname,
                    LastMessage = previewText,
                    LatestMessageTime = lastMessage.sent_at,
                    UnreadCount = unreadCount
                });
            }
            return Ok(conversations.OrderByDescending(c => c.LatestMessageTime));
        }

        [HttpGet("vendor/thread/{businessId}/{customerId}")]
        public async Task<ActionResult<IEnumerable<ChatMessageDto>>> GetVendorMessageThread(long businessId, long customerId)
        {
            var vendorUserId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));

            var isOwner = await _context.business_details
                .AnyAsync(b => b.business_id == businessId && b.vendor.user_id == vendorUserId);
            if (!isOwner) return Unauthorized();

            var messages = await _context.messages
                .Where(m => (m.sender_id == customerId && m.sender_role == "customer" && m.receiver_id == businessId && m.receiver_role == "business") ||
                            (m.sender_id == businessId && m.sender_role == "business" && m.receiver_id == customerId && m.receiver_role == "customer"))
                .OrderBy(m => m.sent_at)
                .ToListAsync();

            var messageIdsToMark = messages
                .Where(m => m.receiver_id == businessId && m.is_read == false)
                .Select(m => m.message_id)
                .ToList();

            if (messageIdsToMark.Any())
            {
                await _context.messages
                    .Where(m => messageIdsToMark.Contains(m.message_id))
                    .ExecuteUpdateAsync(s => s.SetProperty(m => m.is_read, true));
            }

            var customer = await _context.users.FindAsync(customerId);
            var business = await _context.business_details.FindAsync(businessId);

            var dtos = messages.Select(m => new ChatMessageDto
            {
                message_id = m.message_id,
                sender_id = m.sender_id,
                sender_role = m.sender_role,
                sender_name = m.sender_role == "customer" ? customer?.fullname : business?.business_name,
                message_text = m.message_text,
                image_url = m.image_url,
                sent_at = m.sent_at
            });

            return Ok(dtos);
        }

        // ---
        // 🚀 SEND ENDPOINTS (With S3 Image Support)
        // ---

        [HttpPost("customer/send-with-image/{businessId}")]
        public async Task<IActionResult> CustomerSendMessageWithImage(
            long businessId,
            [FromForm] string messageText,
            [FromForm] IFormFile? image) // ✅ Nullable '?' allows text-only sending
        {
            var customerId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));
            var customer = await _context.users.FindAsync(customerId);

            var business = await _context.business_details
                .Include(b => b.vendor)
                .FirstOrDefaultAsync(b => b.business_id == businessId);

            if (business == null || customer == null) return NotFound("Business or customer not found.");

            // 1. Upload to S3 (Returns Full URL)
            string imageUrl = await SaveImageAsync(image);

            // 2. Create Message
            var message = new messages
            {
                sender_id = customerId,
                sender_role = "customer",
                receiver_id = businessId,
                receiver_role = "business",
                message_text = messageText ?? "",
                image_url = imageUrl, // Saved as https://s3....
                sent_at = DateTime.UtcNow,
                is_read = false
            };
            _context.messages.Add(message);
            await _context.SaveChangesAsync();

            // 3. Broadcast
            string channel = $"chat.business.{businessId}";
            var messageDto = new ChatMessageDto
            {
                message_id = message.message_id,
                sender_id = message.sender_id,
                sender_role = message.sender_role,
                sender_name = customer.fullname,
                message_text = message.message_text,
                image_url = message.image_url,
                sent_at = message.sent_at
            };
            await _hubContext.Clients.Group(channel).SendAsync("ReceiveMessage", messageDto);

            // 4. Notification
            if (business.vendor != null)
            {
                string displayMsg = !string.IsNullOrEmpty(message.message_text)
                    ? (message.message_text.Length > 100 ? message.message_text.Substring(0, 100) + "..." : message.message_text)
                    : "[Sent an Image]";

                var notificationPayload = new
                {
                    title = $"New message from {customer.fullname}",
                    message = displayMsg,
                    actor_name = customer.fullname,
                    actor_id = customer.user_id,
                    business_id = business.business_id,
                    business_name = business.business_name
                };
                string payloadJson = JsonSerializer.Serialize(notificationPayload);

                var notification = new notifications
                {
                    user_id = business.vendor.user_id,
                    actor_user_id = customerId,
                    event_type = "NEW_MESSAGE",
                    reference_table = "messages",
                    reference_id = message.message_id,
                    business_id = businessId,
                    recipient_role = "vendor",
                    payload = payloadJson,
                    created_at = DateTime.UtcNow
                };
                _context.notifications.Add(notification);
                await _context.SaveChangesAsync();
            }

            return Ok(messageDto);
        }

        [HttpPost("vendor/send-with-image/{businessId}/{customerId}")]
        public async Task<IActionResult> VendorSendMessageWithImage(
            long businessId,
            long customerId,
            [FromForm] string messageText,
            [FromForm] IFormFile? image) // ✅ Nullable '?' allows text-only sending
        {
            var vendorUserId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));

            var business = await _context.business_details
                .Include(b => b.vendor)
                .FirstOrDefaultAsync(b => b.business_id == businessId && b.vendor.user_id == vendorUserId);

            if (business == null) return Unauthorized("You do not own this business.");
            var customer = await _context.users.FindAsync(customerId);
            if (customer == null) return NotFound("Customer not found.");

            // 1. Upload to S3
            string imageUrl = await SaveImageAsync(image);

            // 2. Create Message
            var message = new messages
            {
                sender_id = businessId,
                sender_role = "business",
                receiver_id = customerId,
                receiver_role = "customer",
                message_text = messageText ?? "",
                image_url = imageUrl, // Saved as https://s3....
                sent_at = DateTime.UtcNow,
                is_read = false
            };
            _context.messages.Add(message);
            await _context.SaveChangesAsync();

            // 3. Broadcast
            string channel = $"chat.business.{businessId}";
            var messageDto = new ChatMessageDto
            {
                message_id = message.message_id,
                sender_id = message.sender_id,
                sender_role = message.sender_role,
                sender_name = business.business_name,
                message_text = message.message_text,
                image_url = message.image_url,
                sent_at = message.sent_at
            };
            await _hubContext.Clients.Group(channel).SendAsync("ReceiveMessage", messageDto);

            // 4. Notification
            string displayMsg = !string.IsNullOrEmpty(message.message_text)
                ? (message.message_text.Length > 100 ? message.message_text.Substring(0, 100) + "..." : message.message_text)
                : "[Sent an Image]";

            var notificationPayload = new
            {
                title = $"New message from {business.business_name}",
                message = displayMsg,
                actor_name = business.business_name,
                business_id = business.business_id
            };
            string payloadJson = JsonSerializer.Serialize(notificationPayload);

            var notification = new notifications
            {
                user_id = customerId,
                actor_user_id = vendorUserId,
                event_type = "NEW_MESSAGE",
                reference_table = "messages",
                reference_id = message.message_id,
                business_id = businessId,
                recipient_role = "customer",
                payload = payloadJson,
                created_at = DateTime.UtcNow
            };
            _context.notifications.Add(notification);
            await _context.SaveChangesAsync();

            return Ok(messageDto);
        }

        // ---
        // BADGE COUNT ENDPOINTS
        // ---

        [HttpGet("customer/unread-count")]
        public async Task<ActionResult<object>> GetCustomerUnreadCount()
        {
            var customerId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));
            var unreadCount = await _context.messages
                .Where(m => m.receiver_id == customerId &&
                            m.receiver_role == "customer" &&
                            m.is_read == false)
                .CountAsync();
            return Ok(new { unreadCount = unreadCount });
        }

        [HttpGet("vendor/unread-count")]
        public async Task<ActionResult<object>> GetVendorUnreadCount()
        {
            var vendorUserId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));
            var businessIds = await _context.business_details
                .Where(b => b.vendor.user_id == vendorUserId)
                .Select(b => b.business_id)
                .ToListAsync();

            if (!businessIds.Any()) return Ok(new { unreadCount = 0 });

            var unreadCount = await _context.messages
                .Where(m => businessIds.Contains(m.receiver_id) &&
                            m.receiver_role == "business" &&
                            m.is_read == false)
                .CountAsync();

            return Ok(new { unreadCount = unreadCount });
        }
    }
}