using Dishora.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Dishora.Controllers
{
    //[ApiController]
    //[Route("api/[controller]")]
    //public class FiltersController : ControllerBase
    //{
    //    private readonly DishoraDbContext _context;

    //    public FiltersController(DishoraDbContext context)
    //    {
    //        _context = context;
    //    }

    //    // GET: api/filters/Diet
    //    [HttpGet("{category}")]
    //    public async Task<ActionResult<IEnumerable<string>>> GetFiltersByCategory(string category)
    //    {
    //        var filtersList = await _context.dietary_specifications
    //            .Select(f => f.dietary_spec_name)
    //            .ToListAsync();

    //        if (!filtersList.Any())
    //            return NotFound();

    //        return Ok(filtersList);
    //    }
    //}

    [ApiController]
    [Route("api/[controller]")]
    public class FiltersController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public FiltersController(DishoraDbContext context)
        {
            _context = context;
        }

        // GET: api/dietaryspecifications
        [HttpGet]
        public async Task<ActionResult<IEnumerable<string>>> GetDietarySpecifications()
        {
            var specs = await _context.dietary_specifications
                .Select(d => d.dietary_spec_name)
                .ToListAsync();

            if (!specs.Any())
                return NotFound();

            return Ok(specs);
        }
    }
}
