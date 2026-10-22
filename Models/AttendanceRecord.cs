using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace MaiHaThanhVy_Backend.Models;

[Table("AttendanceRecords")]
public class AttendanceRecord
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int Id { get; set; }

    [Required]
    public int SessionId { get; set; }

    [Required]
    public int StudentId { get; set; }

    public DateTime CheckInTime { get; set; } = DateTime.UtcNow;

    [Required]
    [MaxLength(20)]
    public string Method { get; set; } = "MANUAL"; // MANUAL, QR, FACE, WIFI, NFC

    [Required]
    [MaxLength(20)]
    public string Status { get; set; } = "PRESENT"; // PRESENT, ABSENT, LATE, EXCUSED

    public double? Latitude { get; set; }

    public double? Longitude { get; set; }

    [MaxLength(500)]
    public string? Note { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public DateTime? UpdatedAt { get; set; }

    // Navigation properties
    [ForeignKey("SessionId")]
    public virtual AttendanceSession? Session { get; set; }

    [ForeignKey("StudentId")]
    public virtual Student? Student { get; set; }
}
