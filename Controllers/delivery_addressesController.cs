using Dishora.Data;
using Dishora.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class delivery_addressesController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public delivery_addressesController(DishoraDbContext context)
        {
            _context = context;
        }

        // POST: api/DeliveryAddresses
        [HttpPost]
        public async Task<IActionResult> PostDeliveryAddress([FromBody] delivery_addresses address)
        {
            if (address == null)
                return BadRequest("Address data is required.");

            address.created_at = DateTime.UtcNow;
            address.updated_at = DateTime.UtcNow;

            _context.delivery_addresses.Add(address);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(PostDeliveryAddress), new { id = address.delivery_address_id }, address);
        }

        // GET: api/DeliveryAddresses
        [HttpGet]
        public async Task<IActionResult> GetDeliveryAddresses()
        {
            var addresses = await Task.FromResult(_context.delivery_addresses.ToList());
            return Ok(addresses);
        }

        // GET: api/DeliveryAddresses/5
        [HttpGet("{id}")]
        public async Task<IActionResult> GetDeliveryAddressById(long id)
        {
            var address = await _context.delivery_addresses.FindAsync(id);
            if (address == null) return NotFound();
            return Ok(address);
        }
    }
}
