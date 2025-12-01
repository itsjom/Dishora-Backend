using Dishora.Data;
using Dishora.DTO;
using Dishora.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;

namespace Dishora.Services
{
    public class OrderService : IOrderService
    {
        private readonly DishoraDbContext _db;

        public OrderService(DishoraDbContext db)
        {
            _db = db;
        }

        // =====================================================
        //  HELPER METHOD TO DERIVE ORDER STATUS FROM ITS ITEMS
        // =====================================================
        private string DeriveOrderStatus(ICollection<order_items> items)
        {
            if (items == null || !items.Any())
            {
                return "Pending";
            }
            if (items.All(i => i.order_item_status.Equals("Cancelled", StringComparison.OrdinalIgnoreCase)))
            {
                return "Cancelled";
            }
            if (items.All(i => i.order_item_status.Equals("Completed", StringComparison.OrdinalIgnoreCase)))
            {
                return "Completed";
            }
            if (items.Any(i => i.order_item_status.Equals("For Delivery", StringComparison.OrdinalIgnoreCase)))
            {
                return "For Delivery";
            }
            if (items.Any(i => i.order_item_status.Equals("Preparing", StringComparison.OrdinalIgnoreCase)))
            {
                return "Preparing";
            }
            return "Pending";
        }


        // =====================================================
        //  CREATE ORDER 
        // =====================================================
        public async Task<long> CreateOrderAsync(OrderRequest request)
        {
            var firstItem = request.Items.FirstOrDefault();
            if (firstItem == null)
            {
                throw new Exception("Order request has no items.");
            }

            // --- Use ONE transaction for everything ---
            using var transaction = await _db.Database.BeginTransactionAsync();
            try
            {
                // --- 1. Create the main 'orders' record ---
                var order = new orders
                {
                    user_id = request.UserId,
                    business_id = request.BusinessId,
                    payment_method_id = request.PaymentMethodId,
                    total = request.Total, // This is the total AMOUNT PAID for the pre-order advance
                    created_at = DateTime.UtcNow,
                    updated_at = DateTime.UtcNow
                };

                // Safely parse and set delivery date/time
                if (!string.IsNullOrEmpty(request.DeliveryDate) && DateOnly.TryParse(request.DeliveryDate, out var parsedDate))
                {
                    order.delivery_date = parsedDate;
                }
                else
                {
                    // Handle invalid or missing date for pre-order if needed
                    // Maybe throw an exception or set a default?
                    // For now, let's assume it's required for pre-orders
                    if (firstItem.IsPreOrder)
                    {
                        await transaction.RollbackAsync();
                        throw new FormatException($"Valid delivery date is required for pre-orders.");
                    }
                }

                if (!string.IsNullOrEmpty(request.DeliveryTime))
                {
                    order.delivery_time = request.DeliveryTime;
                }
                // (Add similar check for time if required for pre-orders)

                _db.orders.Add(order);
                await _db.SaveChangesAsync(); // Save here to get the order_id

                // --- 2. Create Delivery Address ---
                if (request.Address != null)
                {
                    var newAddress = new delivery_addresses
                    {
                        order_id = order.order_id, // Link to the created order_id
                        user_id = request.UserId,
                        full_address = request.Address.FullAddress,
                        phone_number = request.Address.PhoneNumber,
                        street_name = request.Address.StreetName,
                        barangay = request.Address.Barangay,
                        city = request.Address.City,
                        province = request.Address.Province,
                        postal_code = request.Address.PostalCode,
                        region = request.Address.Region,
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    };
                    _db.delivery_addresses.Add(newAddress);
                }

                // --- 3. Create Order Items ---
                decimal calculatedTotalAdvanceRequired = 0; // For pre-order calculation
                decimal calculatedAmountDue = 0;           // For pre-order calculation

                foreach (var i in request.Items)
                {
                    var item = new order_items
                    {
                        order_id = order.order_id, // Link to the created order_id
                        product_id = i.ProductId,
                        product_name = i.ProductName,
                        product_description = i.ProductDescription,
                        quantity = i.Quantity,
                        price_at_order_time = i.PriceAtOrderTime,
                        order_item_status = "Pending", // Initial status for all items
                        order_item_note = i.OrderItemNote,
                        is_pre_order = i.IsPreOrder, // Set based on the item
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    };
                    _db.order_items.Add(item);

                    // If it's a pre-order, calculate necessary amounts
                    if (i.IsPreOrder)
                    {
                        // Assuming 'advance_amount' is stored on the product, fetch it
                        var product = await _db.products.FindAsync(i.ProductId);
                        if (product != null)
                        {
                            calculatedTotalAdvanceRequired += product.advance_amount * i.Quantity;
                        }
                        calculatedAmountDue += i.PriceAtOrderTime * i.Quantity;
                    }
                }

                // --- 4. Create 'pre_orders' record IF it's a pre-order ---
                if (firstItem.IsPreOrder)
                {
                    // Fetch user's full name (Needed for pre_orders table based on previous attempt, BUT IT'S NOT IN YOUR MODEL)
                    // If your actual DB table requires full_name, contact_number, address etc.
                    // THOSE COLUMNS SHOULD BE MOVED to the 'orders' or 'delivery_addresses' table.
                    // The pre_orders table should only contain pre-order specific financial/status info.

                    // Assuming your pre_orders model is correct and only needs these fields:
                    var newPreOrder = new pre_orders
                    {
                        order_id = order.order_id, // Link to the main order
                        total_advance_required = calculatedTotalAdvanceRequired,
                        advance_paid_amount = (decimal)request.Total, // Amount paid now
                        amount_due = calculatedAmountDue - (decimal)request.Total, // Remaining amount
                        // payment_transaction_id = null, // Set this after actual payment if using gateway
                        // payment_option = request.PaymentOption, // If you pass this in OrderRequest
                        preorder_status = "Pending", // Initial status
                        // receipt_url = null, // Set after payment
                        created_at = DateTime.UtcNow,
                        updated_at = DateTime.UtcNow
                    };
                    _db.pre_orders.Add(newPreOrder);
                }

                // --- 5. CREATE NOTIFICATION FOR THE VENDOR ---
                // We need to find the vendor's user_id from the business_id
                var business = await _db.business_details
                                         .Include(b => b.vendor) // Load the related 'vendor'
                                         .FirstOrDefaultAsync(b => b.business_id == request.BusinessId);

                var customer = await _db.users.FindAsync(request.UserId);
                string customerName = customer != null ? customer.username : "Unknown User";

                if (business != null && business.vendor != null)
                {
                    long vendorUserId = business.vendor.user_id; // <-- This is the recipient!
                    long customerUserId = request.UserId;       // <-- This is the actor!

                    var payload = new
                    {
                        title = "You have a new order!",
                        message = $"A new order from {customerName} has been placed."
                    };

                    var inAppNotification = new notifications
                    {
                        user_id = vendorUserId,           // The ID of the VENDOR
                        actor_user_id = customerUserId,   // The ID of the CUSTOMER
                        event_type = "new_order_received",// Our new event type
                        reference_table = "orders",
                        reference_id = order.order_id,
                        payload = JsonSerializer.Serialize(payload),
                        is_read = false,
                        channel = "in_app",
                        created_at = DateTime.UtcNow,
                        recipient_role = "vendor"
                    };

                    _db.notifications.Add(inAppNotification);
                }
                else
                {
                    // Optional: Log a warning if the business or vendor wasn't found
                    // _logger.LogWarning($"Could not find vendor_user_id for business_id {request.BusinessId}. Notification not created.");
                }

                // --- 6. Save Everything ---
                await _db.SaveChangesAsync();
                await transaction.CommitAsync();

                return order.order_id; // Return the main order ID
            }
            catch (Exception)
            {
                await transaction.RollbackAsync();
                throw;
            }
        }

        // =====================================================
        //  GET LIST OF USER'S ORDERS
        // =====================================================
        public async Task<IEnumerable<OrderDto>> GetOrdersByUserIdAsync(long userId)
        {
            var ordersFromDb = await _db.orders
                .Where(o => o.user_id == userId)
                .Include(o => o.business)
                .Include(o => o.order_item)
                .Include(o => o.delivery_address)
                .OrderByDescending(o => o.created_at)
                .ToListAsync();

            var orderDtos = ordersFromDb.Select(o => new OrderDto
            {
                Id = o.order_id,
                Total = o.total,
                PlacedDate = o.created_at.HasValue ? o.created_at.Value.ToString("MMM d, yyyy") : "Date Unknown",
                VendorName = o.business != null ? o.business.business_name : "Unknown Vendor",
                Status = DeriveOrderStatus(o.order_item),
                IsPreOrder = o.order_item.Any(item => item.is_pre_order == true),
                OrderType = o.delivery_address.Any(a => !string.IsNullOrEmpty(a.full_address)) ? "Delivery" : "Pickup"
            }).ToList();

            return orderDtos;
        }

        // =====================================================
        //  GET FULL DETAILS OF A SINGLE ORDER
        // =====================================================
        public async Task<OrderDetailDto> GetOrderDetailsByIdAsync(long orderId, long userId)
        {
            var order = await _db.orders
                .Where(o => o.order_id == orderId && o.user_id == userId)
                .Include(o => o.business)
                .Include(o => o.order_item)
                .Include(o => o.delivery_address) // Include the address
                .FirstOrDefaultAsync();

            if (order == null) return null;

            var orderDetailsDto = new OrderDetailDto
            {
                Id = order.order_id,
                Total = order.total,
                PlacedDate = order.created_at.HasValue ? order.created_at.Value.ToString("MMM d, yyyY") : "N/A",
                VendorName = order.business.business_name,
                DeliveryDate = order.delivery_date.ToString("MMM d, yyyy"),
                Status = DeriveOrderStatus(order.order_item),
                IsPaid = order.payment_method_id != 4, // Assuming '4' is COD

                // Map the address fields
                DeliveryAddress = order.delivery_address.FirstOrDefault()?.full_address,
                ContactNumber = order.delivery_address.FirstOrDefault()?.phone_number,
                IsPreOrder = order.order_item.Any(item => item.is_pre_order == true),

                Items = order.order_item.Select(oi => new OrderItemDto
                {
                    ProductName = oi.product_name,
                    Quantity = oi.quantity,
                    Price = oi.price_at_order_time,
                    Subtotal = oi.quantity * oi.price_at_order_time
                }).ToList()
            };

            return orderDetailsDto;
        }

        // =====================================================
        //  CANCEL AN ORDER
        // =====================================================
        public async Task<bool> CancelOrderAsync(long orderId, long userId)
        {
            var order = await _db.orders
                .Include(o => o.order_item)
                .FirstOrDefaultAsync(o => o.order_id == orderId && o.user_id == userId);

            if (order == null || !order.order_item.All(i => i.order_item_status.Equals("Pending", StringComparison.OrdinalIgnoreCase)))
            {
                return false;
            }

            foreach (var item in order.order_item)
            {
                item.order_item_status = "Cancelled";
            }

            await _db.SaveChangesAsync();
            return true;
        }
    }
}