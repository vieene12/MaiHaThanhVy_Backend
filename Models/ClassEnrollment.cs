using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace MaiHaThanhVy_Backend.Models;

[Table("ClassEnrollments")]
public class ClassEnrollment
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int Id { get; set; }

    [Required]
    public int ClassRoomId { get; set; }

    [Required]
    public int StudentId { get; set; }

    public DateTime EnrolledAt { get; set; } = DateTime.UtcNow;

    public bool IsActive { get; set; } = true;

    // Navigation properties
    [ForeignKey("ClassRoomId")]
    public virtual ClassRoom? ClassRoom { get; set; }

    [ForeignKey("StudentId")]
    public virtual Student? Student { get; set; }
}
