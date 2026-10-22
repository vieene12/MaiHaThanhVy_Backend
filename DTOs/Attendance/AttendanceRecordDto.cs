using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Attendance;

public class AttendanceRecordDto
{
    [Required]
    public int SessionId { get; set; }

    [Required]
    public int StudentId { get; set; }

    [Required]
    public string Method { get; set; } = "MANUAL"; // MANUAL, QR, FACE, WIFI, NFC

    [Required]
    public string Status { get; set; } = "PRESENT"; // PRESENT, ABSENT, LATE, EXCUSED

    public double? Latitude { get; set; }

    public double? Longitude { get; set; }

    [MaxLength(500)]
    public string? Note { get; set; }
}

public class ManualCheckInDto
{
    [Required]
    public int StudentId { get; set; }

    [Required]
    public string Status { get; set; } = "PRESENT"; // PRESENT, ABSENT, LATE, EXCUSED

    public string? Note { get; set; }
}

public class QrCheckInDto
{
    [Required]
    public int StudentId { get; set; }

    [Required]
    public string QRCode { get; set; } = string.Empty;

    public double? Latitude { get; set; }

    public double? Longitude { get; set; }
}

public class WifiCheckInDto
{
    [Required]
    public int StudentId { get; set; }

    [Required]
    public string WifiSSID { get; set; } = string.Empty;

    public double? Latitude { get; set; }

    public double? Longitude { get; set; }
}

public class UpdateRecordStatusDto
{
    [Required]
    public string Status { get; set; } = "PRESENT"; // PRESENT, ABSENT, LATE, EXCUSED

    public string? Note { get; set; }
}

public class AttendanceRecordResponse
{
    public int Id { get; set; }
    public int SessionId { get; set; }
    public int StudentId { get; set; }
    public string StudentCode { get; set; } = string.Empty;
    public string StudentName { get; set; } = string.Empty;
    public string? StudentAvatar { get; set; }
    public DateTime CheckInTime { get; set; }
    public string Method { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public double? Latitude { get; set; }
    public double? Longitude { get; set; }
    public string? Note { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }
}

public class StudentAttendanceSummaryDto
{
    public int StudentId { get; set; }
    public string StudentCode { get; set; } = string.Empty;
    public string StudentName { get; set; } = string.Empty;
    public int TotalSessions { get; set; }
    public int PresentSessions { get; set; }
    public int LateSessions { get; set; }
    public int AbsentSessions { get; set; }
    public int ExcusedSessions { get; set; }
    public double AttendanceRate { get; set; }
}

public class ClassAttendanceReportResponse
{
    public int ClassRoomId { get; set; }
    public string ClassName { get; set; } = string.Empty;
    public string SubjectName { get; set; } = string.Empty;
    public int TotalSessions { get; set; }
    public int TotalStudents { get; set; }
    public List<StudentAttendanceSummaryDto> StudentsSummary { get; set; } = new List<StudentAttendanceSummaryDto>();
}
