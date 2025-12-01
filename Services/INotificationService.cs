namespace Dishora.Services
{
    public interface INotificationService
    {
        Task SendVendorStatusUpdateAsync(long userId, string status, string title, string body);
    }
}
