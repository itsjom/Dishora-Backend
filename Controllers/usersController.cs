using BCrypt.Net;
using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Dishora.Services;
using DishoraAzureSqlApi.Dtos;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Org.BouncyCastle.Crypto.Generators;
using System;
using System.Collections.Generic;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Security.Claims;
using System.Text;
using System.Threading.Tasks;

namespace Dishora.Controllers
{

    [Route("api/[controller]")]
    [ApiController]
    public class usersController : ControllerBase
    {

        private readonly DishoraDbContext _context;
        private readonly EmailService _emailService;
        private readonly ILogger<usersController> _logger;
        private readonly IConfiguration _configuration;

        public usersController(DishoraDbContext context, EmailService emailService, ILogger<usersController> logger, IConfiguration configuration)
        {
            _context = context;
            _emailService = emailService;
            _logger = logger;
            _configuration = configuration;
        }

        // GET: api/users

        [HttpGet]
        public async Task<ActionResult<IEnumerable<users>>> Getusers()
        {
            return await _context.users.ToListAsync();
        }

        // GET: api/users/5
        [HttpGet("{id}")]
        public async Task<ActionResult<users>> Getuser(long id)
        {
            var user = await _context.users.FindAsync(id);

            if (user == null)
            {
                return NotFound();
            }

            return user;
        }

        // PUT: api/users/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> Putuser(long id, users user)
        {
            if (id != user.user_id)
            {
                return BadRequest();
            }

            _context.Entry(user).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!userExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/users
        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] UserRegisterDto dto)
        {
            try
            {
                if (dto == null) return BadRequest("DTO is null");

                if (string.IsNullOrWhiteSpace(dto.Email) || string.IsNullOrWhiteSpace(dto.Password))
                {
                    return BadRequest(new ApiResponseDto { success = false, message = "Email and Password are required." });
                }

                if (string.IsNullOrWhiteSpace(dto.UserName))
                {
                    return BadRequest(new ApiResponseDto { success = false, message = "Username is required." });
                }

                var existing = await _context.users.FirstOrDefaultAsync(u => u.email == dto.Email);

                if (existing != null)
                {
                    return Conflict(new ApiResponseDto { success = false, message = "Email already registered." });
                }

                // --- 💡 1. Start a database transaction ---
                // This ensures we create BOTH the user and the customer, or neither.
                await using var transaction = await _context.Database.BeginTransactionAsync();

                try
                {
                    var newUser = new users
                    {
                        fullname = dto.FullName,
                        username = dto.UserName,
                        email = dto.Email,
                        email_verified_at = null,
                        password = BCrypt.Net.BCrypt.HashPassword(dto.Password), // ✅ hash password
                        is_verified = false,                                  // ✅ mark as unverified
                        verification_token = Guid.NewGuid().ToString(),
                        remember_token = Guid.NewGuid().ToString(),
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    };

                    // --- 💡 2. Save the new user to get their ID ---
                    _context.users.Add(newUser);
                    // await _context.SaveChangesAsync(); // This retrieves the newUser.user_id from the database

                    // --- 💡 3. Create the linked customer profile ---
                    var newCustomer = new customers
                    {
                        user_id = newUser.user_id, // This is the foreign key link
                        // Set other customer fields if you have them in the DTO,
                        // otherwise they will be null, which is fine.
                        // contact_number = dto.ContactNumber, 
                        // user_address = dto.Address,
                        user = newUser,
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    };

                    // --- 💡 4. Save the new customer ---
                    _context.customers.Add(newCustomer);
                    await _context.SaveChangesAsync();

                    // --- 💡 5. Commit the transaction if both saves succeeded ---
                    await transaction.CommitAsync();

                    // 📧 Send email
                    await _emailService.SendVerificationEmail(newUser.email, newUser.verification_token);
                    return Ok(new ApiResponseDto { success = true, message = "Registered successfully. Please check your email to verify your account." });
                }
                catch (Exception ex)
                {
                    // --- 💡 6. If anything failed, roll back all changes ---
                    await transaction.RollbackAsync();

                    _logger.LogError(ex, "Register failed during transaction");
                    return StatusCode(500, new { success = false, message = "Internal error during registration", error = ex.Message });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Register failed pre-transaction (e.g., validation)");
                return StatusCode(500, new { success = false, message = "Internal error", error = ex.Message });
            }
        }

        [HttpGet("verify")]
        public async Task<IActionResult> VerifyEmail(string token)
        {
            if (string.IsNullOrWhiteSpace(token))
            {
                string htmlError = BuildHtmlPage("Verification Failed", "No token was provided. Please check your link.");

                return Content(htmlError, "text/html");
            }

            var user = await _context.users.FirstOrDefaultAsync(u => u.verification_token == token);
            if (user == null)
            {
                // This is the error from your screenshot
                string htmlError = BuildHtmlPage("Verification Failed", "This verification link is invalid, has expired, or has already been used.");

                return Content(htmlError, "text/html");
            }

            if (user.is_verified)
            {
                string htmlInfo = BuildHtmlPage("Already Verified", "Your email has already been verified. You can close this window and log in to the app.");

                return Content(htmlInfo, "text/html");
            }

            // Verify user
            user.is_verified = true;
            user.email_verified_at = DateTime.UtcNow;
            user.verification_token = null;  // Clears the token so it can't be reused.

            await _context.SaveChangesAsync();

            // Success!
            string htmlSuccess = BuildHtmlPage("Email Verified!", "Your account is now active. You can close this window and log in to the Dishora app.");

            return Content(htmlSuccess, "text/html");
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] UserLoginDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.email) || string.IsNullOrWhiteSpace(dto.password))
            {
                return BadRequest(new LoginResponseDto
                {
                    Success = false,
                    Message = "Email and password are required."
                });
            }

            var user = await _context.users.FirstOrDefaultAsync(u => u.email == dto.email);

            if (user == null)
                return Unauthorized(new LoginResponseDto { Success = false, Message = "Invalid email or password." });

            if (!user.is_verified)
                return Unauthorized(new LoginResponseDto { Success = false, Message = "Please verify your email before logging in." });

            if (!BCrypt.Net.BCrypt.Verify(dto.password, user.password))
                return Unauthorized(new LoginResponseDto { Success = false, Message = "Invalid email or password." });

            // ✅ Fetch vendor / business information
            var vendor = await _context.vendors.FirstOrDefaultAsync(v => v.user_id == user.user_id);

            business_details? business = null;
            if (vendor != null)
            {
                business = await _context.business_details
                    .FirstOrDefaultAsync(b => b.vendor_id == vendor.vendor_id);
            }

            // ✅ Build claims
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, user.user_id.ToString()),
                new Claim(ClaimTypes.Name, user.username ?? string.Empty),
                new Claim(ClaimTypes.Email, user.email ?? string.Empty)
            };

            // 👉 Add vendor‑specific claims when applicable
            if (vendor != null && business != null)
            {
                claims.Add(new Claim(ClaimTypes.Role, "Vendor"));
                claims.Add(new Claim("business_id", business.business_id.ToString()));
            }
            else
            {
                claims.Add(new Claim(ClaimTypes.Role, "Customer"));
            }

            // ✅ Create JWT
            var jwtKey = _configuration["Jwt:Key"];
            var jwtIssuer = _configuration["Jwt:Issuer"];
            var jwtAudience = _configuration["Jwt:Audience"];

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
                issuer: jwtIssuer,
                audience: jwtAudience,
                claims: claims,
                expires: DateTime.UtcNow.AddHours(6),
                signingCredentials: creds
            );

            var tokenString = new JwtSecurityTokenHandler().WriteToken(token);

            // ✅ Response payload
            var response = new LoginResponseDto
            {
                Success = true,
                Message = "Login successful.",
                Data = new LoginUserData
                {
                    Token = tokenString,
                    User_Id = user.user_id,
                    Username = user.username,
                    FullName = user.fullname,
                    Email = user.email,
                    IsVendor = vendor != null,
                    VendorStatus = vendor?.registration_status ?? "Not Registered",
                    VendorId = vendor?.vendor_id,
                    // Include Business_Id if found
                    Business_Id = business?.business_id
                }
            };

            return Ok(response);
        }

        // DELETE: api/users/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> Deleteuser(long id)
        {
            var user = await _context.users.FindAsync(id);

            if (user == null)
            {
                return NotFound();
            }

            _context.users.Remove(user);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool userExists(long id)
        {
            return _context.users.Any(e => e.user_id == id);
        }

        // ▼▼▼ ADD THIS HELPER METHOD AT THE END OF YOUR CLASS ▼▼▼
        private string BuildHtmlPage(string title, string message)
        {
            // You can style this HTML string however you like
            return $@"
            <html>
                <head>
                    <title>{title}</title>
                    <meta name='viewport' content='width=device-width, initial-scale=1'>

                    <style>
                        body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; display: flex; align-items: center; justify-content: center; height: 90vh; background-color: #f4f4f4; text-align: center; color: #333; }}
                        .container {{ padding: 25px 30px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }}
                        h2 {{ margin-top: 0; }}
                        p {{ font-size: 1.1em; color: #555; }}
                    </style>
                </head>

                <body>
                    <div class='container'>
                        <h2>{title}</h2>
                        <p>{message}</p>
                    </div>
                </body>
            </html>";
        }
    }
}