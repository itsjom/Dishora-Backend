using Amazon.SimpleNotificationService; 
using Amazon.SimpleNotificationService.Model;
using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Net;
using System.Security.Claims;
using System.Text.Json;

namespace Dishora.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class DevicesController : ControllerBase
    {
        private readonly DishoraDbContext _context;
        private readonly IAmazonSimpleNotificationService _snsService;
        private readonly IConfiguration _config;
        private readonly ILogger<DevicesController> _logger;

        // Ensure your constructor is public and matches this
        public DevicesController(
            DishoraDbContext context,
            IAmazonSimpleNotificationService snsService,
            IConfiguration config,
            ILogger<DevicesController> logger)
        {
            _context = context;
            _snsService = snsService;
            _config = config;
            _logger = logger;
        }

        [HttpPost("register")]
        public async Task<IActionResult> RegisterDevice([FromBody] RegisterDeviceTokenDto dto)
        {
            _logger.LogInformation("--- RegisterDevice endpoint hit. ---");

            // 1. Check if model binding failed
            if (dto == null || string.IsNullOrEmpty(dto.Token))
            {
                _logger.LogWarning("Model binding failed or token was null/empty.");
                return BadRequest("Invalid token data provided.");
            }

            // 2. Get the current user's ID
            var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!long.TryParse(userIdString, out long userId))
            {
                _logger.LogWarning("User ID not found in token. Returning 401 Unauthorized.");
                return Unauthorized("User ID not found in token.");
            }

            // Log only the first 10 chars of the token for security
            _logger.LogInformation($"Found user ID: {userId}. Registering token: {dto.Token.Substring(0, 10)}...");

            // 3. Get the AWS Platform ARN
            var platformApplicationArn = _config["AWS:SnsPlatformArn"];
            if (string.IsNullOrEmpty(platformApplicationArn))
            {
                _logger.LogCritical("AWS:SnsPlatformArn is not configured in appsettings.json. Returning 500.");
                return StatusCode((int)HttpStatusCode.InternalServerError, "Server configuration error.");
            }

            string endpointArn;
            try
            {
                _logger.LogInformation("Creating SNS platform endpoint...");
                // 4. Create an endpoint in AWS SNS for this device token
                var createEndpointRequest = new CreatePlatformEndpointRequest
                {
                    PlatformApplicationArn = platformApplicationArn,
                    Token = dto.Token
                };
                var response = await _snsService.CreatePlatformEndpointAsync(createEndpointRequest);
                endpointArn = response.EndpointArn;
                _logger.LogInformation($"Successfully created/found SNS endpoint ARN: {endpointArn}");
            }
            catch (InvalidParameterException ex)
            {
                _logger.LogWarning(ex, "Failed to create SNS endpoint. InvalidParameterException. Token from client may be invalid, or your SNS Platform Application credentials (the .json file) are wrong. Returning 400.");
                return BadRequest("Invalid or expired device token.");
            }
            catch (NotFoundException ex)
            {
                _logger.LogError(ex, "Failed to create SNS endpoint. NotFoundException. Check PlatformApplicationArn in appsettings.json. Returning 500.");
                return StatusCode((int)HttpStatusCode.InternalServerError, "Server configuration error with SNS.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unexpected error while creating SNS endpoint. Returning 500.");
                return StatusCode((int)HttpStatusCode.InternalServerError, "An unexpected error occurred.");
            }

            try
            {
                // 5. Check if this token is already registered for this user
                _logger.LogInformation($"Checking for existing token for user {userId}...");
                var existingToken = await _context.device_tokens
                    .FirstOrDefaultAsync(t => t.token == dto.Token && t.user_id == userId);

                if (existingToken != null)
                {
                    _logger.LogInformation("Found existing token. Updating it.");
                    // 6a. Update existing token record
                    existingToken.sns_endpoint_arn = endpointArn;
                    existingToken.last_seen = DateTime.UtcNow;
                    existingToken.is_active = true;
                }
                else
                {
                    _logger.LogInformation("No existing token found. Creating new record.");
                    // 6b. Create a new token record
                    var newToken = new device_tokens
                    {
                        user_id = userId,
                        provider = "FCM",
                        token = dto.Token,
                        sns_endpoint_arn = endpointArn,
                        platform = "Android",
                        last_seen = DateTime.UtcNow,
                        is_active = true,
                        created_at = DateTime.UtcNow
                    };
                    await _context.device_tokens.AddAsync(newToken);
                }

                _logger.LogInformation("Calling SaveChangesAsync...");
                await _context.SaveChangesAsync();
                _logger.LogInformation("--- SaveChangesAsync successful! ---");
            }
            catch (DbUpdateException ex)
            {
                _logger.LogError(ex, "Database error while saving device token. Check migrations and connection string. Returning 500.");
                return StatusCode((int)HttpStatusCode.InternalServerError, "Database error.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unexpected error while saving to database. Returning 500.");
                return StatusCode((int)HttpStatusCode.InternalServerError, "An unexpected error occurred.");
            }

            return Ok(new { message = "Device registered successfully." });
        }

        // Test notification endpoint
        [HttpPost("test-notification")]
        public async Task<IActionResult> SendTestNotification()
        {
            var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!long.TryParse(userIdString, out long userId))
            {
                return Unauthorized("User ID not found in token.");
            }

            var userTokens = await _context.device_tokens
                .Where(t => t.user_id == userId && t.is_active == true)
                .ToListAsync();

            if (userTokens.Count == 0)
            {
                return NotFound("No active device tokens found for this user.");
            }

            var androidDataPayload = new
            {
                type = "vendor_status_update", // Matches your app's "if" check
                status = "Approved",           // Test with "Approved"
                title = "Dishora Test",
                body = "This is a DATA message. It works!"
            };

            // 2. Create the GCM wrapper that SNS requires.
            // Note: We are serializing our 'androidDataPayload' object *inside* the 'data' field.
            var snsMessage = JsonSerializer.Serialize(new
            {
                GCM = JsonSerializer.Serialize(new { data = androidDataPayload })
            });

            _logger.LogInformation($"Sending test DATA notification to {userTokens.Count} device(s) for user {userId}");
            _logger.LogInformation($"Payload: {snsMessage}");

            int successCount = 0;
            foreach (var token in userTokens)
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
                    successCount++;
                }
                catch (EndpointDisabledException ex)
                {
                    _logger.LogWarning(ex, $"SNS endpoint {token.sns_endpoint_arn} is disabled. Marking as inactive.");
                    token.is_active = false;
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, $"Failed to send to SNS endpoint {token.sns_endpoint_arn}.");
                }
            }

            await _context.SaveChangesAsync(); // Save changes if any tokens were marked inactive
            return Ok(new { message = $"Test notification sent to {successCount} out of {userTokens.Count} devices." });
        }
    }
}
