using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Classes;

public class ClassRoomDto
{
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

    [Range(1, 200)]
    public int MaxStudents { get; set; } = 50;

    public bool IsActive { get; set; } = true;
}

public class ClassRoomResponse
{
    public int Id { get; set; }
    public string ClassName { get; set; } = string.Empty;
    public int SubjectId { get; set; }
    public string SubjectCode { get; set; } = string.Empty;
    public string SubjectName { get; set; } = string.Empty;
    public int Credits { get; set; }
    public int TeacherId { get; set; }
    public string TeacherName { get; set; } = string.Empty;
    public string TeacherEmail { get; set; } = string.Empty;
    public string? Room { get; set; }
    public string? Semester { get; set; }
    public string? Schedule { get; set; }
    public int MaxStudents { get; set; }
    public int EnrolledStudentsCount { get; set; }
    public bool IsActive { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }
}

public class EnrollStudentDto
{
    [Required]
    public int StudentId { get; set; }
}

public class ClassEnrollmentResponse
{
    public int Id { get; set; }
    public int ClassRoomId { get; set; }
    public int StudentId { get; set; }
    public string StudentCode { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? PhoneNumber { get; set; }
    public string? Avatar { get; set; }
    public DateTime EnrolledAt { get; set; }
    public bool IsActive { get; set; }
}
