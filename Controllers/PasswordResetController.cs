using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Dishora.Services;
using Microsoft.AspNetCore.Identity.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Cryptography;

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class PasswordResetController : ControllerBase
    {
        private readonly DishoraDbContext _context;
        private readonly IEmailService _emailService;
        private const int TOKEN_EXPIRATION_HOURS = 1; // Token expires in 1 hour

        public PasswordResetController(
            DishoraDbContext context,
            IEmailService emailService)
        {
            _context = context;
            _emailService = emailService;
        }

        // --- NEW ENDPOINT 1: FORGOT PASSWORD (UPDATED) ---
        [HttpPost("forgot-password")]
        public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordRequest request)
        {
            var user = await _context.users.SingleOrDefaultAsync(u => u.email == request.Email);
            if (user == null)
            {
                // Don't reveal that the user doesn't exist
                return Ok(new MessageResponse
                {
                    Success = true,
                    Message = "If an account with this email exists, a password reset link has been sent."
                });
            }

            // 1. Generate a secure token
            var token = Convert.ToBase64String(RandomNumberGenerator.GetBytes(64)).Replace('+', '-').Replace('/', '_');

            // 2. Check for an existing token and remove it
            var existingToken = await _context.password_reset_tokens.FindAsync(request.Email);
            if (existingToken != null)
            {
                _context.password_reset_tokens.Remove(existingToken);
            }

            // 3. Create and save the new token
            var newToken = new password_reset_tokens
            {
                email = request.Email,
                token = token,
                created_at = DateTime.UtcNow
            };
            _context.password_reset_tokens.Add(newToken);
            await _context.SaveChangesAsync();

            // 4. Send the email
            var resetLink = $"http://dishora-mobile-env.eba-jym3ahey.ap-southeast-1.elasticbeanstalk.com/reset-password?token={token}";
            var emailSubject = "Reset Your Dishora Password";
            var emailBody = $"<p>Please click the link below to reset your password:</p>" +
                            $"<a href='{resetLink}'>Reset Password</a>" +
                            $"<p>This link will expire in {TOKEN_EXPIRATION_HOURS} hour.</p>";

            await _emailService.SendEmailAsync(user.email, emailSubject, emailBody);

            return Ok(new MessageResponse
            {
                Success = true,
                Message = "If an account with this email exists, a password reset link has been sent."
            });
        }


        // --- NEW ENDPOINT 2: RESET PASSWORD (UPDATED) ---
        [HttpPost("reset-password")]
        public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordRequestDto request)
        {
            // 1. Find the token in the database
            // We find by token, not email, because token is what's unique
            var tokenEntry = await _context.password_reset_tokens
                                         .SingleOrDefaultAsync(t => t.token == request.Token);

            // 2. Validate the token
            if (tokenEntry == null)
            {
                return BadRequest(new MessageResponse
                {
                    Success = false,
                    Message = "Invalid password reset token."
                });
            }

            // 3. Check if token is expired
            var tokenAge = DateTime.UtcNow - tokenEntry.created_at;
            if (tokenAge.Value.TotalHours > TOKEN_EXPIRATION_HOURS)
            {
                _context.password_reset_tokens.Remove(tokenEntry); // Clean up expired token
                await _context.SaveChangesAsync();
                return BadRequest(new MessageResponse
                {
                    Success = false,
                    Message = "Password reset token has expired."
                });
            }

            // 4. Find the user associated with the token's email
            var user = await _context.users.SingleOrDefaultAsync(u => u.email == tokenEntry.email);
            if (user == null)
            {
                // This should not happen if the DB is consistent
                return BadRequest(new MessageResponse { Success = false, Message = "User not found." });
            }

            // 5. Hash and set the new password
            user.password = BCrypt.Net.BCrypt.HashPassword(request.Password);

            // 6. Invalidate the token by deleting it
            _context.password_reset_tokens.Remove(tokenEntry);

            // 7. Save all changes
            await _context.SaveChangesAsync();

            return Ok(new MessageResponse
            {
                Success = true,
                Message = "Password has been reset successfully."
            });
        }
    }
}
