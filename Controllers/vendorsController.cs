using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Claims; // For LINQ methods like GroupBy, Any

namespace Dishora.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class VendorsController : ControllerBase
    {
        private readonly DishoraDbContext _context;

        public VendorsController(DishoraDbContext context)
        {
            _context = context;
        }

        // GET: api/vendors
        [HttpGet]
        public async Task<IActionResult> GetVendors()
        {
            // ✅ STEP 1: Execute a robust EF query to fetch ALL necessary data, including related collections.
            var vendorsWithDetails = await _context.vendors
                .Where(v => v.registration_status == "Approved")
                // Eager load business details
                .Include(v => v.BusinessDetails)
                    // Then load opening hours and reviews linked to BusinessDetails
                    .ThenInclude(bd => bd.opening_hours)
                .Include(v => v.BusinessDetails)
                    .ThenInclude(bd => bd.reviews)
                .ToListAsync(); // Execute query and bring data into memory

            // ✅ STEP 2: Process and map data to DTO in memory.
            var vendorListDtos = vendorsWithDetails.Select(v => {
                // Safely get the first business detail record (data is already loaded from DB)
                business_details firstDetail = v.BusinessDetails.FirstOrDefault();

                // Calculate rating client-side to avoid the SQL translation error
                double rating = 0;
                if (firstDetail?.reviews.Any() == true)
                {
                    // Use standard C# LINQ (runs in memory)
                    rating = firstDetail.reviews.Average(r => r.rating);
                }

                return new VendorListDto
                {
                    // --- Map existing fields ---
                    VendorId = v.vendor_id,
                    UserId = v.user_id,
                    BusinessId = firstDetail != null ? firstDetail.business_id : 0,
                    BusinessName = firstDetail != null ? firstDetail.business_name : "N/A",
                    BusinessDescription = firstDetail != null ? firstDetail.business_description : "N/A",
                    BusinessAddress = firstDetail != null ? firstDetail.business_location : "N/A",
                    BusinessImage = firstDetail != null ? firstDetail.business_image : null,
                    VendorStatus = v.registration_status,

                    // ✅ Map Calculated Rating
                    Rating = rating,

                    // ✅ Format Opening Hours (using materialised data)
                    OpeningHours = FormatOpeningHours(firstDetail?.opening_hours)

                };
            }).ToList(); // Materialize DTOs

            // Optional Debug Output (remove later)
            foreach (var vendor in vendorListDtos)
            {
                Console.WriteLine(
                    $"DBG → VendorId: {vendor.VendorId,-3} | Name: {vendor.BusinessName,-25} | Hours: {vendor.OpeningHours}"
                );
            }

            return Ok(vendorListDtos);
        }


        [Authorize] // <-- Make sure to secure this endpoint
        [HttpGet("dashboard-stats")]
        public async Task<ActionResult<DashboardStatsDto>> GetDashboardStats()
        {
            try
            {
                // 1. Get the authenticated user's ID (as a string)
                var userIdString = User.FindFirstValue(ClaimTypes.NameIdentifier);
                if (string.IsNullOrEmpty(userIdString))
                {
                    return Unauthorized(new { Message = "User not authenticated." });
                }

                // 2. Parse the string ID into a long
                if (!long.TryParse(userIdString, out long userId))
                {
                    return Unauthorized(new { Message = "Invalid user ID format in token." });
                }

                // 3. Find the vendor using the parsed long ID
                var vendor = await _context.vendors
                    .Include(v => v.BusinessDetails) // Eager load business details
                    .FirstOrDefaultAsync(v => v.user_id == userId);

                if (vendor == null)
                {
                    return NotFound(new { Message = "Vendor profile not found for this user." });
                }

                // 4. Get the primary Business ID
                var business = vendor.BusinessDetails.FirstOrDefault();
                if (business == null)
                {
                    return NotFound(new { Message = "No business details found for this vendor." });
                }

                long businessId = business.business_id;

                // 5. Perform the database queries

                // --- QUERY 1: Total Revenue ---
                // (Uses 'orders', 'order_item', 'pre_orders' models)
                decimal totalRevenueDecimal = await _context.orders // <-- Using lowercase DbSet
                    .Where(o => o.business_id == businessId &&
                                (
                                    (o.preorder == null && o.order_item.Any() && o.order_item.All(oi => oi.order_item_status == "Completed")) ||
                                    (o.preorder != null && o.preorder.preorder_status == "Completed")
                                ))
                    .SumAsync(o => o.total);

                double totalRevenue = (double)totalRevenueDecimal;

                // --- QUERY 2: New Orders ---
                // (Uses 'orders', 'order_item', 'pre_orders' models)
                int newOrders = await _context.orders // <-- Using lowercase DbSet
                    .CountAsync(o => o.business_id == businessId &&
                                     (
                                         (o.preorder == null && o.order_item.Any(oi => oi.order_item_status == "Pending")) ||
                                         (o.preorder != null && o.preorder.preorder_status == "Pending")
                                     ));

                // --- QUERY 3: Active Products (UPDATED) ---
                // This now correctly uses 'business_id' and 'is_available' from your 'products' model
                int activeProducts = await _context.products // <-- Using lowercase DbSet
                    .CountAsync(p => p.business_id == businessId && p.is_available == true);

                // --- QUERY 4: Average Rating (UPDATED) ---
                // This correctly uses 'business_id' and 'rating' from your 'reviews' model
                double averageRating = 0;
                if (await _context.reviews.AnyAsync(r => r.business_id == businessId)) // <-- Using lowercase DbSet
                {
                    averageRating = await _context.reviews // <-- Using lowercase DbSet
                        .Where(r => r.business_id == businessId)
                        .AverageAsync(r => r.rating); // 'rating' is the correct property
                }

                // 6. Build and return the DTO
                var stats = new DashboardStatsDto
                {
                    TotalRevenue = totalRevenue,
                    NewOrders = newOrders,
                    ActiveProducts = activeProducts,
                    AverageRating = Math.Round(averageRating, 1) // Round to 1 decimal place
                };

                return Ok(stats);
            }
            catch (Exception ex)
            {
                // Log the exception (ex)
                return StatusCode(500, new { Message = "An error occurred while fetching dashboard stats." });
            }
        }


        // ✅ --- HELPER FUNCTION: FormatOpeningHours (Rest of the code remains the same) ---
        private string FormatOpeningHours(ICollection<business_opening_hours> hours)
        {
            if (hours == null || !hours.Any())
            {
                return "Hours not available";
            }

            var dayOrder = new Dictionary<string, int> {
                { "Monday", 1 }, { "Tuesday", 2 }, { "Wednesday", 3 }, { "Thursday", 4 },
                { "Friday", 5 }, { "Saturday", 6 }, { "Sunday", 7 }
            };

            var orderedHours = hours
                .Where(h => !string.IsNullOrEmpty(h.day_of_week) && dayOrder.ContainsKey(h.day_of_week))
                .OrderBy(h => dayOrder[h.day_of_week])
                .ToList();

            if (!orderedHours.Any()) return "Hours not available";

            var groupedHours = orderedHours
                .GroupBy(h => new { h.opens_at, h.closes_at, h.is_closed })
                .Select(g => new {
                    g.Key.opens_at,
                    g.Key.closes_at,
                    g.Key.is_closed,
                    Days = g.Select(h => h.day_of_week).ToList()
                })
                .ToList();

            List<string> formattedEntries = new List<string>();

            foreach (var group in groupedHours)
            {
                string timeRange;
                if (group.is_closed == true)
                {
                    timeRange = "Closed";
                }
                else if (group.opens_at.HasValue && group.closes_at.HasValue)
                {
                    string opensStr = group.opens_at.Value.ToString("h:mm tt", System.Globalization.CultureInfo.InvariantCulture);
                    string closesStr = group.closes_at.Value.ToString("h:mm tt", System.Globalization.CultureInfo.InvariantCulture);
                    timeRange = $"{opensStr} - {closesStr}";
                }
                else
                {
                    timeRange = "Hours vary";
                }

                string dayRange = FormatDayRange(group.Days, dayOrder);
                formattedEntries.Add($"{dayRange}: {timeRange}");
            }

            return string.Join(" | ", formattedEntries);
        }

        // --- HELPER FUNCTION: FormatDayRange ---
        private string FormatDayRange(List<string> days, Dictionary<string, int> dayOrder)
        {
            if (days == null || !days.Any()) return "";
            if (days.Count == 1) return AbbreviateDay(days[0]);

            var sortedDays = days
                .Where(d => !string.IsNullOrEmpty(d) && dayOrder.ContainsKey(d))
                .OrderBy(d => dayOrder[d])
                .ToList();

            if (!sortedDays.Any()) return "";

            List<string> ranges = new List<string>();
            int startRange = 0;
            for (int i = 1; i < sortedDays.Count; i++)
            {
                if (dayOrder[sortedDays[i]] != dayOrder[sortedDays[i - 1]] + 1)
                {
                    if (i - 1 == startRange)
                    {
                        ranges.Add(AbbreviateDay(sortedDays[startRange]));
                    }
                    else
                    {
                        ranges.Add($"{AbbreviateDay(sortedDays[startRange])}-{AbbreviateDay(sortedDays[i - 1])}");
                    }
                    startRange = i;
                }
            }

            if (sortedDays.Count - 1 == startRange)
            {
                ranges.Add(AbbreviateDay(sortedDays[startRange]));
            }
            else
            {
                if (startRange < sortedDays.Count)
                {
                    ranges.Add($"{AbbreviateDay(sortedDays[startRange])}-{AbbreviateDay(sortedDays[sortedDays.Count - 1])}");
                }
            }

            return string.Join(", ", ranges);
        }

        // --- HELPER FUNCTION: AbbreviateDay ---
        private string AbbreviateDay(string day)
        {
            switch (day?.Trim())
            {
                case "Monday": return "Mon";
                case "Tuesday": return "Tue";
                case "Wednesday": return "Wed";
                case "Thursday": return "Thu";
                case "Friday": return "Fri";
                case "Saturday": return "Sat";
                case "Sunday": return "Sun";
                default: return day ?? "";
            }
        }
    }
}