using System.ComponentModel.DataAnnotations;

namespace Dishora.DTO
{
    public class PreOrderScheduleCreateDto
    {
        [Required]
        public DateOnly AvailableDate { get; set; }

        [Required]
        [Range(1, int.MaxValue, ErrorMessage = "Capacity must be greater than zero.")]
        public int MaxOrders { get; set; }

        // business_id will be derived from the authenticated vendor user/token
        // No need to pass it in the body.
    }
}
