using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Attendance;

public class AttendanceSessionDto
{
    [Required]
    public int ClassRoomId { get; set; }

    [Required]
    public int TeacherId { get; set; }

    [Required]
    public DateTime SessionDate { get; set; }

    [Required]
    public string StartTime { get; set; } = "08:00"; // format "HH:mm" or "HH:mm:ss"

    [Required]
    public string EndTime { get; set; } = "11:30";

    public string? QRCode { get; set; }

    public double? Latitude { get; set; }

    public double? Longitude { get; set; }

    public string? WifiSSID { get; set; }

    public string? Note { get; set; }

    public bool IsActive { get; set; } = true;
}

public class AttendanceSessionResponse
{
    public int Id { get; set; }
    public int ClassRoomId { get; set; }
    public string ClassName { get; set; } = string.Empty;
    public string SubjectName { get; set; } = string.Empty;
    public int TeacherId { get; set; }
    public string TeacherName { get; set; } = string.Empty;
    public DateTime SessionDate { get; set; }
    public string StartTime { get; set; } = string.Empty;
    public string EndTime { get; set; } = string.Empty;
    public string? QRCode { get; set; }
    public double? Latitude { get; set; }
    public double? Longitude { get; set; }
    public string? WifiSSID { get; set; }
    public string? Note { get; set; }
    public bool IsActive { get; set; }
    public DateTime CreatedAt { get; set; }
    public int TotalStudents { get; set; }
    public int PresentCount { get; set; }
    public int LateCount { get; set; }
    public int AbsentCount { get; set; }
    public int ExcusedCount { get; set; }
}
