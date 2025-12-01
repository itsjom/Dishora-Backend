using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using System.Security.Cryptography;
using System.Text;

// This filter automatically verifies that incoming webhook requests are genuinely from PayMongo.
public class PayMongoSignatureFilter : IAsyncActionFilter
{
    private readonly ILogger<PayMongoSignatureFilter> _logger; // Add logger field
    private readonly IConfiguration _configuration;        // Add config field

    public PayMongoSignatureFilter(ILogger<PayMongoSignatureFilter> logger, IConfiguration configuration)
    {
        _logger = logger;
        _configuration = configuration;
    }

    public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
    {
        // 1. Get the secret key from your appsettings.json
        var secret = _configuration["PayMongoWebhookSecret"];

        if (string.IsNullOrEmpty(secret))
        {
            _logger.LogCritical("CRITICAL ERROR: PayMongo Webhook Secret is not configured."); 
            context.Result = new StatusCodeResult(500); // Internal Server Error
            return;
        }

        // 2. Get the signature provided by PayMongo from the request header
        if (!context.HttpContext.Request.Headers.TryGetValue("Paymongo-Signature", out var signatureHeader))
        {
            _logger.LogWarning("Webhook rejected: Missing Paymongo-Signature header."); // Use logger
            context.Result = new UnauthorizedObjectResult("Missing Paymongo-Signature header.");
            return;
        }

        // 3. Read the raw request body (the JSON payload)
        // We need to do this carefully so the body can be read again by the controller later.
        context.HttpContext.Request.EnableBuffering();
        string requestBody;
        using (var reader = new StreamReader(context.HttpContext.Request.Body, leaveOpen: true))
        {
            requestBody = await reader.ReadToEndAsync();
            // Reset the stream position so the controller can read the body again
            context.HttpContext.Request.Body.Position = 0;
        }

        // 4. Parse the signature header to get the timestamp (t=) and the actual signature (h=)
        var signatureParts = signatureHeader.ToString().Split(',');
        var timePart = signatureParts.FirstOrDefault(p => p.StartsWith("t="))?.Substring(2);
        var signatureFromPayMongo = signatureParts.FirstOrDefault(p => p.StartsWith("h="))?.Substring(2);

        if (timePart == null || signatureFromPayMongo == null)
        {
            _logger.LogWarning("Webhook rejected: Invalid signature format in header. Header: {HeaderValue}", signatureHeader.ToString()); // Use logger
            context.Result = new UnauthorizedObjectResult("Invalid signature format in header.");
            return;
        }

        // 5. Construct the payload-to-sign string: "{timestamp}.{request_body}"
        var payloadToSign = $"{timePart}.{requestBody}";

        // 6. Calculate our own signature using the secret key
        using var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(secret));
        var computedHash = hmac.ComputeHash(Encoding.UTF8.GetBytes(payloadToSign));
        var computedSignature = BitConverter.ToString(computedHash).Replace("-", "").ToLower();

        // --- DEBUG LOGGING ---
        _logger.LogInformation("Webhook Signature Verification:");
        _logger.LogInformation("  > Timestamp (t): {Timestamp}", timePart);
        _logger.LogInformation("  > Received Signature (h): {ReceivedSignature}", signatureFromPayMongo);
        _logger.LogInformation("  > Computed Signature:     {ComputedSignature}", computedSignature);
        // _logger.LogInformation("  > Payload Signed: {Payload}", payloadToSign); // Optional: Log payload if needed, careful with sensitive data
        // --- END DEBUG LOGGING ---

        // 7. Compare our signature with the one from PayMongo
        if (!CryptographicOperations.FixedTimeEquals(
        Encoding.UTF8.GetBytes(computedSignature),
        Encoding.UTF8.GetBytes(signatureFromPayMongo)))
        {
            _logger.LogWarning("Webhook rejected: Signature verification failed."); // Use logger
            context.Result = new UnauthorizedObjectResult("Signature verification failed.");
            return;
        }
        _logger.LogInformation("Webhook signature verified successfully."); // Log success
        // If we reach here, the signature is valid. Allow the request to proceed to the controller's action.
        await next();
    }
}