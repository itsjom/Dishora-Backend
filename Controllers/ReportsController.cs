using Dishora.Data;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Text;

namespace Dishora.Controllers
{
    [Route("api/reports")]
    [ApiController]
    public class ReportsController : ControllerBase
    {
        // Replace 'ApplicationDbContext' with whatever your context class is named
        private readonly DishoraDbContext _context;

        public ReportsController(DishoraDbContext context)
        {
            _context = context;
        }

        [HttpGet("sales")]
        [Authorize]
        public async Task<IActionResult> GetSalesReport(
            [FromQuery] long businessId,
            [FromQuery] DateTime start,
            [FromQuery] DateTime end)
        {
            DateTime queryEndDate = end.Date.AddDays(1).AddTicks(-1);

            // 1. Fetch Raw Data (Only Completed items)
            var salesData = await _context.orders
                .Include(o => o.order_item)
                .Include(o => o.User)
                .Include(o => o.payment_method)
                .Where(o => o.business_id == businessId
                         && o.created_at >= start
                         && o.created_at <= queryEndDate
                         && o.order_item.Any(i => i.order_item_status == "Completed"))
                .OrderByDescending(o => o.created_at)
                .ToListAsync();

            if (salesData == null || !salesData.Any())
            {
                var emptyCsv = "Order ID,Date,Customer,Product,Quantity,Price,Total,Status,Payment Method\n";
                return File(Encoding.UTF8.GetBytes(emptyCsv), "text/csv", "No_Data.csv");
            }

            var builder = new StringBuilder();

            // --- SECTION 1: DETAILED LIST ---
            builder.AppendLine("DETAILED SALES LOG");
            builder.AppendLine("Order ID,Date,Customer,Product,Quantity,Unit Price,Total Price,Status,Payment Method");

            decimal grandTotal = 0;

            foreach (var order in salesData)
            {
                string dateStr = order.created_at?.ToString("yyyy-MM-dd HH:mm") ?? "N/A";
                string customerName = EscapeCsv(order.User?.fullname ?? "Guest");
                string payMethod = EscapeCsv(order.payment_method?.method_name ?? "Unknown");

                foreach (var item in order.order_item)
                {
                    if (item.order_item_status != "Completed") continue;

                    string productName = EscapeCsv(item.product_name);
                    string status = EscapeCsv(item.order_item_status);
                    decimal lineTotal = item.quantity * item.price_at_order_time;

                    grandTotal += lineTotal;

                    builder.AppendLine($"{order.order_id},{dateStr},{customerName},{productName},{item.quantity},{item.price_at_order_time:F2},{lineTotal:F2},{status},{payMethod}");
                }
            }

            builder.AppendLine("");
            builder.AppendLine($",,,,,GRAND TOTAL:,{grandTotal:F2},,");
            builder.AppendLine("");
            builder.AppendLine("");

            // --- SECTION 2: MOST ORDERED PRODUCTS SUMMARY ---
            builder.AppendLine("MOST ORDERED PRODUCTS SUMMARY");
            builder.AppendLine("Rank,Product Name,Total Quantity Sold,Total Revenue Generated");

            // Use LINQ to Group by Product Name and Sum the Quantities
            var topProducts = salesData
                .SelectMany(o => o.order_item)                 // Flatten the list of lists
                .Where(i => i.order_item_status == "Completed") // Filter only completed items
                .GroupBy(i => i.product_name)                  // Group by Name
                .Select(g => new
                {
                    ProductName = g.Key,
                    TotalQty = g.Sum(x => x.quantity),
                    TotalRevenue = g.Sum(x => x.quantity * x.price_at_order_time)
                })
                .OrderByDescending(x => x.TotalQty)            // Sort by Quantity (Highest first)
                .ToList();

            int rank = 1;
            foreach (var prod in topProducts)
            {
                string safeName = EscapeCsv(prod.ProductName);
                builder.AppendLine($"{rank},{safeName},{prod.TotalQty},{prod.TotalRevenue:F2}");
                rank++;
            }

            // Final Output
            string fileName = $"Sales_Report_{start:yyyyMMdd}_{end:yyyyMMdd}.csv";
            byte[] fileBytes = Encoding.UTF8.GetBytes(builder.ToString());

            return File(fileBytes, "text/csv", fileName);
        }

        private string EscapeCsv(string value)
        {
            if (string.IsNullOrEmpty(value)) return "";
            if (value.Contains(",") || value.Contains("\"") || value.Contains("\n"))
            {
                return $"\"{value.Replace("\"", "\"\"")}\"";
            }
            return value;
        }
    }
}
