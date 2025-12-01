using Microsoft.AspNetCore.SignalR;
using System.Security.Claims;

namespace Dishora.Hubs
{
    public class NameUserIdProvider : IUserIdProvider
    {
        public virtual string GetUserId(HubConnectionContext connection)
        {
            // This looks for the standard 'nameidentifier' claim in the user's JWT token
            // and returns its value. This is how SignalR links a connection to your user ID.
            return connection.User?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        }
    }
}
