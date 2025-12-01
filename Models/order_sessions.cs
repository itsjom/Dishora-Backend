namespace Dishora.Models
{
    public class order_sessions
    {
        public long order_session_id { get; set; }

        public long? user_id { get; set; }

        public string session_id { get; set; } = null!;

        public string orders { get; set; } = null!;

        public DateTime? created_at { get; set; }

        public DateTime? updated_at { get; set; }
    }
}
