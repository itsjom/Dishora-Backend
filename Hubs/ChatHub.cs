using Dishora.Data;
using Dishora.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;

namespace Dishora.Hubs
{
    [Authorize] // Ensures only logged-in users can connect to the hub
    public class ChatHub : Hub
    {
        // No DbContext needed here anymore, as the controller handles saving.

        /// <summary>
        /// Called by the client when they enter a chat screen.
        /// This subscribes them to a specific business's chat channel.
        /// </summary>
        public async Task JoinChatGroup(string businessId)
        {
            string channel = $"chat.business.{businessId}";
            await Groups.AddToGroupAsync(Context.ConnectionId, channel);

            Console.WriteLine($"--> Client {Context.ConnectionId} joined group {channel}");
        }

        /// <summary>
        /// Called by the client when they leave a chat screen.
        /// </summary>
        public async Task LeaveChatGroup(string businessId)
        {
            string channel = $"chat.business.{businessId}";
            await Groups.RemoveFromGroupAsync(Context.ConnectionId, channel);

            Console.WriteLine($"--> Client {Context.ConnectionId} left group {channel}");
        }

        public override Task OnConnectedAsync()
        {
            Console.WriteLine($"--> User connected: {Context.UserIdentifier}");
            return base.OnConnectedAsync();
        }

        public override Task OnDisconnectedAsync(System.Exception exception)
        {
            Console.WriteLine($"--> User disconnected: {Context.UserIdentifier}");
            return base.OnDisconnectedAsync(exception);
        }
    }
}
