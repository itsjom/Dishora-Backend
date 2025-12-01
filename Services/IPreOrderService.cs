using Dishora.DTO;

namespace Dishora.Services
{
    public interface IPreOrderService
    {
        /// <summary>
        /// Gets all pre-orders for a specific vendor, grouped and formatted for the Android app.
        /// </summary>
        /// <param name="vendorUserId">The User ID of the logged-in vendor.</param>
        /// <returns>A list of GroupedPreOrderDto.</returns>
        Task<IEnumerable<GroupedPreOrderDto>> GetGroupedPreOrdersForVendorAsync(long vendorUserId);

        /// <summary>
        /// Updates the status of a pre-order (e.g., "Pending" -> "Preparing").
        /// </summary>
        /// <param name="groupId">The ID of the pre-order (as a string).</param>
        /// <param name="newStatus">The new status to set.</param>
        /// <param name="vendorUserId">The User ID of the vendor, used for a security check.</param>
        /// <returns>True if the update was successful.</returns>
        Task<bool> UpdateOrderStatusAsync(string groupId, string newStatus, long vendorUserId);
    }
}
