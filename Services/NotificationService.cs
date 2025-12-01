using Amazon.SimpleNotificationService;
using Amazon.SimpleNotificationService.Model;
using Dishora.Data;
using Dishora.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Text.Json;

namespace Dishora.Services
{
    public class NotificationService : INotificationService
    {
        private readonly IAmazonSimpleNotificationService _snsService;
        private readonly DishoraDbContext _context; // Your EF DbContext
        private readonly ILogger<NotificationService> _logger;

        public NotificationService(IAmazonSimpleNotificationService snsService, DishoraDbContext context, ILogger<NotificationService> logger)
        {
            _snsService = snsService;
            _context = context;
            _logger = logger;
        }

        public async Task SendVendorStatusUpdateAsync(long userId, string status, string title, string body)
        {
            // 1. Define the payload
            // This is the "data" object your Android app will receive
            var dataPayload = new
            {
                type = "vendor_status_update",
                status = status,
                title = title,
                body = body
            };

            // 2. Create the notification record in your database
            var notification = new notifications
            {
                user_id = userId,
                event_type = "vendor_status_update",
                payload = JsonSerializer.Serialize(dataPayload),
                is_read = false,
                channel = "push", // From your model
                created_at = DateTime.UtcNow,
                is_global = false,
                recipient_role = "vendor"
            };
            await _context.notifications.AddAsync(notification);
            await _context.SaveChangesAsync(); // Save to get the notification_id

            // 3. Find all active devices for this user
            var activeDevices = await _context.device_tokens
                .Where(dt => dt.user_id == userId &&
                             dt.is_active &&
                             dt.sns_endpoint_arn != null)
                .ToListAsync();

            if (!activeDevices.Any())
            {
                _logger.LogInformation($"User {userId} has no active device tokens to notify.");
                return;
            }

            // 4. Prepare the SNS message
            // This "GCM" wrapper is required by FCM (via SNS) for data-only messages
            var fcmPayload = new
            {
                // The "data" block is still sent for your foreground app logic
                data = dataPayload
            };
            var messagePayload = new { GCM = JsonSerializer.Serialize(fcmPayload) };
            var finalMessage = JsonSerializer.Serialize(messagePayload);

            // 5. Loop through each device and send the push notification
            foreach (var device in activeDevices)
            {
                var publishRequest = new PublishRequest
                {
                    TargetArn = device.sns_endpoint_arn, // Send to this specific device
                    MessageStructure = "json",           // IMPORTANT: Must be "json"
                    Message = finalMessage
                };

                // 6. Log the delivery attempt
                var deliveryRecord = new notification_deliveries
                {
                    notification_id = notification.notification_id,
                    provider = "AWS_SNS",
                    attempted_at = DateTime.UtcNow
                };

                try
                {
                    var response = await _snsService.PublishAsync(publishRequest);

                    deliveryRecord.provider_response = response.MessageId;
                    deliveryRecord.success = true;
                }
                catch (EndpointDisabledException ex)
                {
                    _logger.LogWarning($"Endpoint {device.sns_endpoint_arn} is disabled. Deactivating token.", ex);
                    deliveryRecord.success = false;
                    deliveryRecord.provider_response = "EndpointDisabled: " + ex.Message;

                    // Deactivate this token so we don't try again
                    device.is_active = false;
                }
                catch (Exception ex)
                {
                    _logger.LogError($"Failed to send SNS message to {device.sns_endpoint_arn}", ex);
                    deliveryRecord.success = false;
                    deliveryRecord.provider_response = ex.Message;
                }

                await _context.notification_deliveries.AddAsync(deliveryRecord);
            }

            // Save changes (like deactivated tokens)
            await _context.SaveChangesAsync();
        }
    }
}
