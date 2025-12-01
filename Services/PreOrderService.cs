using Dishora.Data;
using Dishora.DTO;
using Microsoft.EntityFrameworkCore;

namespace Dishora.Services
{
    public class PreOrderService : IPreOrderService
    {
        private readonly DishoraDbContext _context;

        public PreOrderService(DishoraDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<GroupedPreOrderDto>> GetGroupedPreOrdersForVendorAsync(long vendorUserId)
        {
            // --- Step 1: Find the Vendor and Business IDs ---
            // 'vendorUserId' is the 'user_id' from the JWT token.

            // ✅ CORRECTION: Find the 'vendors' record first.
            var vendorRecord = await _context.vendors
                .AsNoTracking()
                .FirstOrDefaultAsync(v => v.user_id == vendorUserId);

            if (vendorRecord == null)
            {
                // This user is not a vendor.
                return new List<GroupedPreOrderDto>();
            }

            // ✅ CORRECTION: Now find the business using the 'vendors.vendor_id'.
            var business = await _context.business_details
                .AsNoTracking()
                .FirstOrDefaultAsync(b => b.vendor_id == vendorRecord.vendor_id);

            if (business == null)
            {
                // This vendor has no business details record.
                return new List<GroupedPreOrderDto>();
            }

            var vendorBusinessId = business.business_id;

            // --- Step 2: Query all pre-orders that belong to this vendor's business ---
            //var preOrders = await _context.pre_orders
            //    .Include(p => p.Order)
            //        .ThenInclude(o => o.User) // For customer name
            //    .Include(p => p.Order)
            //        .ThenInclude(o => o.order_item) // For product list

            //    .Where(p => p.Order.business_id == vendorBusinessId)
            //    .OrderByDescending(p => p.created_at)
            //    .ToListAsync();

            var preOrders = await _context.pre_orders
                .Include(p => p.Order)
                    .ThenInclude(o => o.User) // For customer name
                .Include(p => p.Order)
                    .ThenInclude(o => o.order_item) // For product list
                .Include(p => p.Order)
                    .ThenInclude(o => o.delivery_address) // For contact/address
                .Where(p => p.Order.business_id == business.business_id)
                .OrderByDescending(p => p.created_at)
                .ToListAsync();

            // --- Step 3: Map the EF Models to the DTOs ---
            var dtos = preOrders.Select(p => {
                // Find the first delivery address for this order
                var address = p.Order.delivery_address.FirstOrDefault();

                return new GroupedPreOrderDto
                {
                    GroupId = p.pre_order_id.ToString(),
                    CustomerName = p.Order.User?.fullname ?? "Customer",
                    TotalAmount = (double)p.Order.total,
                    Status = p.preorder_status,

                    // --- MAP THE NEW DATA ---
                    OrderDate = p.created_at?.ToString("MMM d, yyyy") ?? "N/A",

                    // Combine date and time
                    DeliveryDate = $"{p.Order.delivery_date.ToString("MMM d, yyyy")} at {p.Order.delivery_time}",

                    // Use '?' to safely get values from the address
                    ContactNumber = address?.phone_number ?? "N/A",
                    DeliveryAddress = address?.full_address ?? "N/A",

                    AdvancePaid = (double)p.advance_paid_amount,
                    BalanceDue = (double)p.amount_due,

                    // --- (Item mapping stays the same) ---
                    Items = p.Order.order_item.Select(oi => new PreOrderItemDto
                    {
                        MenuItemName = oi.product_name,
                        Quantity = oi.quantity,
                        Price = (double)oi.price_at_order_time
                    }).ToList()
                };
            });

            return dtos;
        }

        public async Task<bool> UpdateOrderStatusAsync(string groupId, string newStatus, long vendorUserId)
        {
            // --- Step 1: Find the vendor's business ID (for security) ---
            // 'vendorUserId' is the 'user_id' from the JWT token.

            // ✅ CORRECTION: Find the 'vendors' record first.
            var vendorRecord = await _context.vendors
                .AsNoTracking()
                .FirstOrDefaultAsync(v => v.user_id == vendorUserId);

            if (vendorRecord == null)
            {
                return false; // This user is not a vendor.
            }

            // ✅ CORRECTION: Now find the business using the 'vendors.vendor_id'.
            var business = await _context.business_details
                .AsNoTracking()
                .FirstOrDefaultAsync(b => b.vendor_id == vendorRecord.vendor_id);

            if (business == null)
            {
                return false; // This vendor has no business details record.
            }

            // --- Step 2: Find the Pre-Order to update ---
            if (!long.TryParse(groupId, out var preOrderId))
            {
                return false;
            }

            var preOrder = await _context.pre_orders
                .Include(p => p.Order)
                .FirstOrDefaultAsync(p => p.pre_order_id == preOrderId);

            if (preOrder == null)
            {
                return false;
            }

            // --- Step 3: SECURITY CHECK ---
            if (preOrder.Order.business_id != business.business_id)
            {
                return false;
            }

            // --- Step 4: Update and Save ---
            preOrder.preorder_status = newStatus;
            await _context.SaveChangesAsync();
            return true;
        }
    }
}
