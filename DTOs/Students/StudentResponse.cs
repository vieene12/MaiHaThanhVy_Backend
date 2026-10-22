namespace MaiHaThanhVy_Backend.DTOs.Students;

public class StudentResponse
{
    public int Id { get; set; }
    public string StudentCode { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public DateTime? DateOfBirth { get; set; }
    public string? Gender { get; set; }
    public string? PhoneNumber { get; set; }
    public string? Address { get; set; }
    public string? Avatar { get; set; }
    public string? Major { get; set; }
    public int? UserId { get; set; }
    public bool IsActive { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }
    public int EnrolledClassesCount { get; set; }
    public bool HasFaceData { get; set; }
}
