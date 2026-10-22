using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace MaiHaThanhVy_Backend.Models;

[Table("Subjects")]
public class Subject
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int Id { get; set; }

    [Required]
    [MaxLength(50)]
    public string SubjectCode { get; set; } = string.Empty;

    [Required]
    [MaxLength(200)]
    public string SubjectName { get; set; } = string.Empty;

    public int Credits { get; set; } = 3;

    [MaxLength(1000)]
    public string? Description { get; set; }

    public bool IsActive { get; set; } = true;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public DateTime? UpdatedAt { get; set; }

    // Navigation properties
    public virtual ICollection<ClassRoom> ClassRooms { get; set; } = new List<ClassRoom>();
}
