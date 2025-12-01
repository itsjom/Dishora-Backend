using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReviewsController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public ReviewsController(DishoraDbContext context)
        {
            _context = context;
        }

        // --- 1. ENDPOINT TO GET ALL REVIEWS ---
        // GET: /api/reviews/{businessId}
        [HttpGet("{businessId}")]
        [AllowAnonymous] // Anyone can read reviews
        public async Task<IActionResult> GetReviews(long businessId)
        {
            var reviews = await _context.reviews
                .Where(r => r.business_id == businessId)
                .Include(r => r.customer) // This joins with the 'users' table
                    .ThenInclude(c => c.user)
                .OrderByDescending(r => r.created_at)
                .Select(r => new ReviewDto
                {
                    // This works because of the .Include(r => r.customer)
                    CustomerName = r.customer.user.fullname,
                    Rating = r.rating,
                    Comment = r.comment,
                    CreatedAt = r.created_at.HasValue
                                ? r.created_at.Value.ToString("MMM dd, yyyy")
                                : "N/A"
                })
                .ToListAsync();

            return Ok(reviews);
        }

        // --- 2. ENDPOINT TO SUBMIT A NEW REVIEW ---
        // POST: /api/reviews
        [HttpPost]
        [Authorize] // ❗️ Ensures only logged-in users can post
        public async Task<IActionResult> SubmitReview([FromBody] ReviewRequestDto reviewRequest)
        {
            // Get the user's ID from their auth token
            var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (string.IsNullOrEmpty(userIdString))
            {
                return Unauthorized("User not found.");
            }

            var userId = long.Parse(userIdString);

            // 2. Find the CUSTOMER profile linked to that USER ID
            var customer = await _context.customers.FirstOrDefaultAsync(c => c.user_id == userId);

            // 3. Check if a customer profile exists for this user
            if (customer == null)
            {
                // This user is either a vendor or something went wrong during registration
                return NotFound("A customer profile for this user does not exist.");
            }

            // 4. Create the new review entity using the CORRECT ID
            var newReview = new reviews
            {
                customer_id = customer.customer_id, // <-- ✅ THE FIX
                business_id = reviewRequest.BusinessId,
                rating = reviewRequest.Rating,
                comment = reviewRequest.Comment,
                created_at = DateTime.UtcNow,
                updated_at = DateTime.UtcNow
            };

            // Add to the database
            _context.reviews.Add(newReview);
            await _context.SaveChangesAsync();

            return Ok(); // Send a 200 OK
        }
    }
}
