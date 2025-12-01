using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class password_reset_tokens
{
    public string email { get; set; } = null!;

    public string token { get; set; } = null!;

    public DateTime? created_at { get; set; }
}
