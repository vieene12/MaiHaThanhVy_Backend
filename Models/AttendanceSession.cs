using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace MaiHaThanhVy_Backend.Models;

[Table("AttendanceSessions")]
public class AttendanceSession
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int Id { get; set; }

    [Required]
    public int ClassRoomId { get; set; }

    [Required]
    public int TeacherId { get; set; }

    [Required]
    public DateTime SessionDate { get; set; }

    [Required]
    public TimeSpan StartTime { get; set; }

    [Required]
    public TimeSpan EndTime { get; set; }

    [MaxLength(500)]
    public string? QRCode { get; set; }

    public double? Latitude { get; set; }

    public double? Longitude { get; set; }

    [MaxLength(100)]
    public string? WifiSSID { get; set; }

    [MaxLength(500)]
    public string? Note { get; set; }

    public bool IsActive { get; set; } = true;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    // Navigation properties
    [ForeignKey("ClassRoomId")]
    public virtual ClassRoom? ClassRoom { get; set; }

    [ForeignKey("TeacherId")]
    public virtual Teacher? Teacher { get; set; }

    public virtual ICollection<AttendanceRecord> AttendanceRecords { get; set; } = new List<AttendanceRecord>();
}
