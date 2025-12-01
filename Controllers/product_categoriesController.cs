using Dishora.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class CategoriesController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public CategoriesController(DishoraDbContext context)
        {
            _context = context;
        }

        // GET: api/categories
        [HttpGet]
        public async Task<IActionResult> GetCategories()
        {
            var categories = await _context.product_categories
                                           .Select(c => new {
                                               c.product_category_id,
                                               c.category_name
                                           })
                                           .ToListAsync();

            return Ok(categories);
        }
    }
}
