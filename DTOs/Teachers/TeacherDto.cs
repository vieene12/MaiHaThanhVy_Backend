using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Teachers;

public class TeacherDto
{
    [Required]
    [MaxLength(50)]
    public string TeacherCode { get; set; } = string.Empty;

    [Required]
    [MaxLength(100)]
    public string FullName { get; set; } = string.Empty;

    [Required]
    [EmailAddress]
    [MaxLength(150)]
    public string Email { get; set; } = string.Empty;

    [MaxLength(100)]
    public string? Department { get; set; }

    public int? UserId { get; set; }

    public bool IsActive { get; set; } = true;
}

public class TeacherResponse
{
    public int Id { get; set; }
    public string TeacherCode { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? Department { get; set; }
    public int? UserId { get; set; }
    public bool IsActive { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }
    public int AssignedClassesCount { get; set; }
}
