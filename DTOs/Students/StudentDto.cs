using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Students;

public class StudentDto
{
    [Required]
    [MaxLength(50)]
    public string StudentCode { get; set; } = string.Empty;

    [Required]
    [MaxLength(100)]
    public string FullName { get; set; } = string.Empty;

    [Required]
    [EmailAddress]
    [MaxLength(150)]
    public string Email { get; set; } = string.Empty;

    public DateTime? DateOfBirth { get; set; }

    [MaxLength(20)]
    public string? Gender { get; set; }

    [MaxLength(20)]
    public string? PhoneNumber { get; set; }

    [MaxLength(255)]
    public string? Address { get; set; }

    [MaxLength(500)]
    public string? Avatar { get; set; }

    [MaxLength(100)]
    public string? Major { get; set; }

    public int? UserId { get; set; }

    public bool IsActive { get; set; } = true;
}
