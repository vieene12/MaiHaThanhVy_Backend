using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace MaiHaThanhVy_Backend.Models;

[Table("ClassRooms")]
public class ClassRoom
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int Id { get; set; }

    [Required]
    [MaxLength(100)]
    public string ClassName { get; set; } = string.Empty;

    [Required]
    public int SubjectId { get; set; }

    [Required]
    public int TeacherId { get; set; }

    [MaxLength(50)]
    public string? Room { get; set; }

    [MaxLength(50)]
    public string? Semester { get; set; }

    [MaxLength(200)]
    public string? Schedule { get; set; }

    public int MaxStudents { get; set; } = 50;

    public bool IsActive { get; set; } = true;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public DateTime? UpdatedAt { get; set; }

    // Navigation properties
    [ForeignKey("SubjectId")]
    public virtual Subject? Subject { get; set; }

    [ForeignKey("TeacherId")]
    public virtual Teacher? Teacher { get; set; }

    public virtual ICollection<ClassEnrollment> ClassEnrollments { get; set; } = new List<ClassEnrollment>();
    public virtual ICollection<AttendanceSession> AttendanceSessions { get; set; } = new List<AttendanceSession>();
}
