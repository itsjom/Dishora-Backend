using Dishora.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Dishora.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class DietarySpecificationsController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public DietarySpecificationsController(DishoraDbContext context)
        {
            _context = context;
        }

        // GET: api/dietaryspecifications
        [HttpGet]
        public async Task<IActionResult> GetDietarySpecifications()
        {
            var specs = await _context.dietary_specifications
                .Select(s => new
                {
                    s.dietary_specification_id,
                    s.dietary_spec_name
                })
                .OrderBy(s => s.dietary_spec_name)
                .ToListAsync();

            return Ok(specs);
        }
    }
}
