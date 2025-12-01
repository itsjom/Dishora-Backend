using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Dishora.Data;
using Dishora.Models;

namespace Dishora.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class customersController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public customersController(DishoraDbContext context)
        {
            _context = context;
        }

        // GET: api/customers
        [HttpGet]
        public async Task<ActionResult<IEnumerable<customers>>> Getcustomers()
        {
            return await _context.customers.ToListAsync();
        }

        // GET: api/customers/5
        [HttpGet("{id}")]
        public async Task<ActionResult<customers>> Getcustomer(long id)
        {
            var customer = await _context.customers.FindAsync(id);

            if (customer == null)
            {
                return NotFound();
            }

            return customer;
        }

        // PUT: api/customers/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> Putcustomer(long id, customers customer)
        {
            if (id != customer.customer_id)
            {
                return BadRequest();
            }

            _context.Entry(customer).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!customerExists(id))
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

        // POST: api/customers
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<customers>> Postcustomer(customers customer)
        {
            _context.customers.Add(customer);
            await _context.SaveChangesAsync();

            return CreatedAtAction("Getcustomer", new { id = customer.customer_id }, customer);
        }

        // DELETE: api/customers/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> Deletecustomer(long id)
        {
            var customer = await _context.customers.FindAsync(id);
            if (customer == null)
            {
                return NotFound();
            }

            _context.customers.Remove(customer);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool customerExists(long id)
        {
            return _context.customers.Any(e => e.customer_id == id);
        }
    }
}
