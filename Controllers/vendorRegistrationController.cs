using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Dishora.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using System.Text.Json;

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class VendorRegistrationController : ControllerBase
    {
        private readonly DishoraDbContext _context;
        private readonly S3Service _s3Service;

        public VendorRegistrationController(DishoraDbContext context, S3Service s3Service)
        {
            _context = context;
            _s3Service = s3Service;
        }

        [HttpPost]
        [RequestSizeLimit(25_000_000)] // max 25 MB
        public async Task<IActionResult> Register([FromForm] RegistrationRequest request)
        {
            // 🔍 Debug: log all form keys received
            foreach (var key in Request.Form.Keys)
            {
                Console.WriteLine($"Form key received: {key}");
            }

            // ✅ Get user Id from claims (JWT authentication)
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userIdClaim) || !long.TryParse(userIdClaim, out var userId))
                return Unauthorized(new { success = false, message = "Invalid authentication." });

            // ✅ Parse JSON input into proper DTOs
            var vendorData = JsonSerializer.Deserialize<VendorDto>(request.VendorJson);
            var businessData = JsonSerializer.Deserialize<BusinessDto>(request.BusinessJson);
            var hoursData = JsonSerializer.Deserialize<List<OpeningHourDto>>(request.OpeningHoursJson);

            // Prevent duplicate vendor registration
            var vendorExists = await _context.vendors.AnyAsync(v => v.user_id == userId);
            if (vendorExists)
                return Conflict(new { success = false, message = "You are already registered as a vendor." });

            using var transaction = await _context.Database.BeginTransactionAsync();
            try
            {
                // 1. Create Vendor record
                var vendor = new vendors
                {
                    user_id = userId,
                    fullname = vendorData.fullName,
                    phone_number = vendorData.phoneNumber,
                    registration_status = "Pending",
                    created_at = DateTime.UtcNow,
                    updated_at = DateTime.UtcNow
                };

                _context.vendors.Add(vendor);
                await _context.SaveChangesAsync();

                // 2. Upload files to S3 (store URLs)
                var businessImageUrl = request.BusinessImage != null ? await _s3Service.UploadFileAsync(request.BusinessImage, "business") : null;
                var birRegFileUrl = request.BirRegFile != null ? await _s3Service.UploadFileAsync(request.BirRegFile, "documents") : null;
                var businessPermitFileUrl = request.BusinessPermitFile != null ? await _s3Service.UploadFileAsync(request.BusinessPermitFile, "permits") : null;
                var validIdFileUrl = request.ValidIdFile != null ? await _s3Service.UploadFileAsync(request.ValidIdFile, "ids") : null;
                var mayorPermitFileUrl = request.MayorPermitFile != null ? await _s3Service.UploadFileAsync(request.MayorPermitFile, "permits") : null;

                // 3. Create Business record
                var business = new business_details
                {
                    vendor_id = vendor.vendor_id,
                    business_name = businessData.businessName,
                    business_description = businessData.description,
                    business_type = businessData.type,
                    business_location = businessData.location,
                    latitude = businessData.latitude,
                    longitude = businessData.longitude,
                    business_image = businessImageUrl,
                    business_duration = businessData.businessDuration,
                    bir_reg_no = businessData.birRegNo,
                    bir_reg_file = birRegFileUrl,
                    business_permit_no = businessData.businessPermitNo,
                    business_permit_file = businessPermitFileUrl,
                    valid_id_type = businessData.validIdType,
                    valid_id_no = businessData.validIdNo,
                    valid_id_file = validIdFileUrl,
                    mayor_permit_file = mayorPermitFileUrl,
                    verification_status = "Pending",
                    created_at = DateTime.UtcNow,
                    updated_at = DateTime.UtcNow
                };

                _context.business_details.Add(business);
                await _context.SaveChangesAsync();

                // 4. Opening Hours records
                foreach (var oh in hoursData)
                {
                    // Normalize casing(e.g. "monday" → "Monday", "tuesday " → "Tuesday")
                    string normalizedDay = string.IsNullOrWhiteSpace(oh.dayOfWeek) ? "Monday" : // fallback
                    char.ToUpper(oh.dayOfWeek[0]) + oh.dayOfWeek.Substring(1).ToLower();


                    _context.business_opening_hours.Add(new business_opening_hours
                    {
                        business_id = business.business_id,
                        day_of_week = normalizedDay,
                        opens_at = string.IsNullOrEmpty(oh.opensAt) ? null : TimeOnly.Parse(oh.opensAt),
                        closes_at = string.IsNullOrEmpty(oh.closesAt) ? null : TimeOnly.Parse(oh.closesAt),
                        is_closed = oh.isClosed,
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    });
                }

                // await _context.SaveChangesAsync();
                try
                {
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateException ex)
                {
                    Console.WriteLine(ex.InnerException?.Message);
                    return StatusCode(500, new
                    {
                        success = false,
                        message = "DB error",
                        error = ex.InnerException?.Message
                    });
                }
                await transaction.CommitAsync();

                return Ok(new
                {
                    success = true,
                    message = "Business registration submitted successfully, pending approval.",
                    data = new { vendorId = vendor.vendor_id, businessId = business.business_id }
                });
            }
            catch (Exception ex)
            {
                await transaction.RollbackAsync();

                // log the full chain to your console/logging
                Console.WriteLine($"Exception: {ex}");
                Console.WriteLine($"Inner Exception: {ex.InnerException?.Message}");

                return StatusCode(500, new
                {
                    success = false,
                    message = "Registration failed.",
                    error = ex.InnerException?.Message ?? ex.Message
                });
            }
        }

        [Authorize]
        [HttpGet("status")]
        public async Task<ActionResult<VendorStatusDto>> GetStatus()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userIdClaim) || !long.TryParse(userIdClaim, out var userId))
                return Unauthorized(new VendorStatusDto { IsVendor = false, VendorStatus = "Unauthorized" });

            var vendor = await _context.vendors.FirstOrDefaultAsync(v => v.user_id == userId);

            if (vendor == null)
            {
                return Ok(new VendorStatusDto
                {
                    IsVendor = false,
                    VendorStatus = "Not Registered"
                });
            }

            return Ok(new VendorStatusDto
            {
                IsVendor = true,
                VendorId = vendor.vendor_id,
                VendorStatus = vendor.registration_status ?? "Pending"
            });
        }
    }
}