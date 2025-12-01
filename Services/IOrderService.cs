using Dishora.DTO;

namespace Dishora.Services
{
    public interface IOrderService
    {
        Task<long> CreateOrderAsync(OrderRequest request);
        Task<IEnumerable<OrderDto>> GetOrdersByUserIdAsync(long userId);
        Task<OrderDetailDto> GetOrderDetailsByIdAsync(long orderId, long userId);
        Task<bool> CancelOrderAsync(long orderId, long userId);
    }
}
