using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Subjects;

public class SubjectDto
{
    [Required]
    [MaxLength(50)]
    public string SubjectCode { get; set; } = string.Empty;

    [Required]
    [MaxLength(200)]
    public string SubjectName { get; set; } = string.Empty;

    [Range(1, 10)]
    public int Credits { get; set; } = 3;

    [MaxLength(1000)]
    public string? Description { get; set; }

    public bool IsActive { get; set; } = true;
}

public class SubjectResponse
{
    public int Id { get; set; }
    public string SubjectCode { get; set; } = string.Empty;
    public string SubjectName { get; set; } = string.Empty;
    public int Credits { get; set; }
    public string? Description { get; set; }
    public bool IsActive { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }
    public int TotalClassesCount { get; set; }
}
