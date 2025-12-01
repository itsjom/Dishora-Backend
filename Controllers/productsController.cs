using Amazon.S3;
using Amazon.S3.Model;
using Amazon;
using Dishora.Data;
using Dishora.DTO; // ✅ Make sure this is using your DTO namespace
using Dishora.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore; // ✅ Make sure this is included for .Select()

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ProductsController : ControllerBase
    {
        private readonly DishoraDbContext _context;
        private readonly IConfiguration _config;

        public ProductsController(DishoraDbContext context, IConfiguration config)
        {
            _context = context;
            _config = config;
        }

        // ========= Upload to S3 ==========
        private async Task<string?> UploadToS3(IFormFile file)
        {
            if (file == null) return null;

            var allowedMimeTypes = new[] { "image/jpeg", "image/png" };
            var contentType = file.ContentType?.ToLower() ?? "image/jpeg";
            if (!allowedMimeTypes.Contains(contentType))
                throw new Exception($"Invalid Content-Type: {contentType}. Only PNG and JPEG allowed");

            var bucketName = _config["AWS:BucketName"];
            var region = RegionEndpoint.GetBySystemName(_config["AWS:Region"]);

            using var s3Client = new AmazonS3Client(
                _config["AWS:AccessKey"],
                _config["AWS:SecretKey"],
                region
            );

            var extension = contentType == "image/png" ? ".png" : ".jpg";
            var fileName = Guid.NewGuid().ToString() + extension;

            using var stream = file.OpenReadStream();
            var putRequest = new PutObjectRequest
            {
                BucketName = bucketName,
                Key = fileName,
                InputStream = stream,
                ContentType = contentType
            };

            var response = await s3Client.PutObjectAsync(putRequest);
            if (response.HttpStatusCode != System.Net.HttpStatusCode.OK)
                throw new Exception("Image upload to S3 failed");

            return $"https://{bucketName}.s3.{region.SystemName}.amazonaws.com/{fileName}";
        }

        // ========= Upload Product ==========
        [HttpPost("upload")]
        public async Task<IActionResult> Upload([FromForm] IFormFile? image,
                                                [FromForm] string item_name,
                                                [FromForm] decimal price,
                                                [FromForm] decimal? advance_amount,
                                                [FromForm] long business_id,
                                                [FromForm] long? product_category_id,
                                                [FromForm] bool? is_pre_order,
                                                [FromForm] bool? is_available,
                                                [FromForm] string? description,
                                                [FromForm] int? cutoff_minutes,
                                                [FromForm] List<long>? dietary_specification_ids)
        {
            if (string.IsNullOrWhiteSpace(item_name))
                return BadRequest("Item name is required");

            string? imageUrl = null;
            if (image != null)
                imageUrl = await UploadToS3(image);

            var product = new products
            {
                business_id = business_id,
                product_category_id = product_category_id,
                item_name = item_name,
                price = price,
                advance_amount = advance_amount ?? 0,
                is_pre_order = is_pre_order,
                is_available = is_available ?? true,
                image_url = imageUrl,
                description = description,
                created_at = DateTime.UtcNow,
                updated_at = DateTime.UtcNow,
                cutoff_minutes = cutoff_minutes
            };

            _context.products.Add(product);
            await _context.SaveChangesAsync();

            if (dietary_specification_ids != null)
            {
                foreach (var id in dietary_specification_ids.Distinct())
                {
                    var link = new product_dietary_specifications
                    {
                        product_id = product.product_id,
                        dietary_specification_id = id,
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    };
                    _context.product_dietary_specifications.Add(link);
                }
                await _context.SaveChangesAsync();
            }

            return Ok(new
            {
                message = "Product saved successfully",
                product_id = product.product_id
            });
        }

        // ========= Get Single Product (✅ UPDATED) ==========
        [HttpGet("{id}")]
        public async Task<IActionResult> GetProduct(long id)
        {
            var product = await _context.products
                .Where(p => p.product_id == id)
                .Select(p => new ProductDto
                {
                    product_id = p.product_id,
                    item_name = p.item_name,
                    price = p.price,
                    advance_amount = p.advance_amount,
                    is_available = p.is_available ?? true,
                    is_pre_order = p.is_pre_order ?? false,
                    description = p.description,
                    image_url = p.image_url,
                    vendor_id = p.business_id,

                    // ✅ Get NAMES for customer chips
                    tags = p.product_dietary_specifications
                        .Select(spec_link => spec_link.dietary_specification.dietary_spec_name)
                        .ToList(),

                    // ✅ Get IDs for vendor edit form
                    dietary_specification_ids = p.product_dietary_specifications
                        .Select(spec_link => spec_link.dietary_specification_id)
                        .ToList(),

                    product_category_id = p.product_category_id,
                    cutoff_minutes = p.cutoff_minutes
                })
                .FirstOrDefaultAsync();

            if (product == null) return NotFound("Product not found");

            return Ok(product);
        }

        // ========= Get Products (Customer view) (✅ UPDATED) ==========
        [HttpGet("customer/{businessId}")]
        public async Task<IActionResult> GetCustomerProducts(long businessId)
        {
            var products = await _context.products
                .Where(p => p.business_id == businessId && p.is_available == true)
                .Select(p => new ProductDto
                {
                    product_id = p.product_id,
                    item_name = p.item_name,
                    price = p.price,
                    advance_amount = p.advance_amount,
                    is_available = p.is_available ?? true,
                    is_pre_order = p.is_pre_order ?? false,
                    description = p.description,
                    image_url = p.image_url,
                    vendor_id = p.business_id,

                    // ✅ Get NAMES for customer chips
                    tags = p.product_dietary_specifications
                        .Select(spec_link => spec_link.dietary_specification.dietary_spec_name)
                        .ToList(),

                    // ✅ Get IDs for vendor edit form
                    dietary_specification_ids = p.product_dietary_specifications
                        .Select(spec_link => spec_link.dietary_specification_id)
                        .ToList(),

                    product_category_id = p.product_category_id,
                    cutoff_minutes = p.cutoff_minutes
                })
                .ToListAsync();

            return Ok(products);
        }

        // ========= Get Products (Vendor view) (✅ UPDATED) ==========
        [HttpGet("vendor/{businessId}")]
        public async Task<IActionResult> GetVendorProducts(long businessId)
        {
            var products = await _context.products
                .Where(p => p.business_id == businessId)
                .Select(p => new ProductDto
                {
                    product_id = p.product_id,
                    item_name = p.item_name,
                    price = p.price,
                    advance_amount = p.advance_amount,
                    is_available = p.is_available ?? true,
                    is_pre_order = p.is_pre_order ?? false,
                    description = p.description,
                    image_url = p.image_url,
                    vendor_id = p.business_id,

                    // ✅ Get NAMES for customer chips
                    tags = p.product_dietary_specifications
                        .Select(spec_link => spec_link.dietary_specification.dietary_spec_name)
                        .ToList(),

                    // ✅ Get IDs for vendor edit form
                    dietary_specification_ids = p.product_dietary_specifications
                        .Select(spec_link => spec_link.dietary_specification_id)
                        .ToList(),

                    product_category_id = p.product_category_id,
                    cutoff_minutes = p.cutoff_minutes
                })
                .ToListAsync();

            return Ok(products);
        }

        // ========= Update Availability ==========
        [HttpPatch("{id}/availability")]
        public async Task<IActionResult> UpdateAvailability(long id, [FromQuery] bool isAvailable)
        {
            var product = await _context.products.FindAsync(id);
            if (product == null) return NotFound("Product not found");

            product.is_available = isAvailable;
            product.updated_at = DateTime.UtcNow;

            _context.products.Update(product);
            await _context.SaveChangesAsync();

            return Ok(new
            {
                message = "Availability updated successfully",
                product_id = product.product_id,
                is_available = product.is_available
            });
        }

        // ========= Update Product (no image) ==========
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateProduct(long id, [FromBody] ProductUpdateDto dto)
        {
            var product = await _context.products.FindAsync(id);
            if (product == null) return NotFound("Product not found");

            if (!string.IsNullOrEmpty(dto.item_name)) product.item_name = dto.item_name;
            if (dto.price.HasValue) product.price = dto.price.Value;
            if (dto.advance_amount.HasValue) product.advance_amount = dto.advance_amount.Value;
            if (dto.is_available.HasValue) product.is_available = dto.is_available;
            if (dto.is_pre_order.HasValue) product.is_pre_order = dto.is_pre_order;
            if (!string.IsNullOrEmpty(dto.description)) product.description = dto.description;
            if (dto.product_category_id.HasValue) product.product_category_id = dto.product_category_id.Value;

            product.updated_at = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return Ok(new { message = "Product updated successfully" });
        }

        // ========= Update Product (with image) ==========
        [HttpPut("{id}/upload")]
        public async Task<IActionResult> UpdateProductWithImage(
            long id,
            [FromForm] IFormFile? image,
            [FromForm] string? item_name,
            [FromForm] decimal? price,
            [FromForm] decimal? advance_amount,
            [FromForm] long? product_category_id,
            [FromForm] bool? is_pre_order,
            [FromForm] bool? is_available,
            [FromForm] string? description,
            [FromForm] int? cutoff_minutes,
            [FromForm] List<long>? dietary_specification_ids)
        {
            var product = await _context.products.FindAsync(id);
            if (product == null) return NotFound("Product not found");

            // image upload if provided
            if (image != null) product.image_url = await UploadToS3(image);
            if (!string.IsNullOrWhiteSpace(item_name)) product.item_name = item_name;
            if (price.HasValue) product.price = price.Value;
            if (advance_amount.HasValue) product.advance_amount = advance_amount.Value;
            if (product_category_id.HasValue) product.product_category_id = product_category_id.Value;
            if (is_pre_order.HasValue) product.is_pre_order = is_pre_order;
            if (is_available.HasValue) product.is_available = is_available;
            if (!string.IsNullOrWhiteSpace(description)) product.description = description;
            if (cutoff_minutes.HasValue) product.cutoff_minutes = cutoff_minutes;

            product.updated_at = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            // dietary spec updates
            if (dietary_specification_ids != null)
            {
                var existing = _context.product_dietary_specifications
                    .Where(x => x.product_id == id);
                _context.product_dietary_specifications.RemoveRange(existing);

                foreach (var specId in dietary_specification_ids.Distinct())
                {
                    _context.product_dietary_specifications.Add(new product_dietary_specifications
                    {
                        product_id = id,
                        dietary_specification_id = specId,
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    });
                }

                await _context.SaveChangesAsync();
            }

            return Ok(new { message = "Product updated successfully with image" });
        }

        // ========= Delete Product ==========
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteProduct(long id)
        {
            var product = await _context.products.FindAsync(id);
            if (product == null) return NotFound("Product not found");

            _context.products.Remove(product);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Product deleted successfully" });
        }
    }
}