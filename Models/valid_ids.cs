using System;
using System.Collections.Generic;

namespace Dishora.Models;

public partial class valid_ids
{
    public long valid_id_id { get; set; }

    public string valid_id_name { get; set; } = null!;

    public DateTime? created_at { get; set; }

    public DateTime? updated_at { get; set; }
}
