namespace MaiHaThanhVy_Backend.DTOs.Dashboard;

public class AdminDashboardDto
{
    public int TotalUsers { get; set; }
    public int TotalStudents { get; set; }
    public int TotalTeachers { get; set; }
    public int TotalSubjects { get; set; }
    public int TotalClasses { get; set; }
    public int TotalSessionsToday { get; set; }
    public int TotalAttendanceRecordsToday { get; set; }
    public double AverageAttendanceRate { get; set; }
    public List<RecentActivityDto> RecentActivities { get; set; } = new List<RecentActivityDto>();
}

public class TeacherDashboardDto
{
    public int TeacherId { get; set; }
    public string TeacherName { get; set; } = string.Empty;
    public int TotalClasses { get; set; }
    public int TotalStudents { get; set; }
    public int TotalSessions { get; set; }
    public int TodaySessionsCount { get; set; }
    public double OverallAttendanceRate { get; set; }
    public List<TeacherClassSummaryDto> Classes { get; set; } = new List<TeacherClassSummaryDto>();
    public List<RecentSessionDto> RecentSessions { get; set; } = new List<RecentSessionDto>();
}

public class StudentDashboardDto
{
    public int StudentId { get; set; }
    public string StudentName { get; set; } = string.Empty;
    public string StudentCode { get; set; } = string.Empty;
    public int EnrolledClassesCount { get; set; }
    public int TotalSessions { get; set; }
    public int PresentCount { get; set; }
    public int LateCount { get; set; }
    public int AbsentCount { get; set; }
    public int ExcusedCount { get; set; }
    public double AttendanceRate { get; set; }
    public List<StudentClassAttendanceDto> EnrolledClasses { get; set; } = new List<StudentClassAttendanceDto>();
}

public class RecentActivityDto
{
    public string Type { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }
}

public class TeacherClassSummaryDto
{
    public int ClassRoomId { get; set; }
    public string ClassName { get; set; } = string.Empty;
    public string SubjectName { get; set; } = string.Empty;
    public int StudentCount { get; set; }
    public int SessionCount { get; set; }
}

public class RecentSessionDto
{
    public int SessionId { get; set; }
    public string ClassName { get; set; } = string.Empty;
    public DateTime SessionDate { get; set; }
    public int PresentCount { get; set; }
    public int TotalStudents { get; set; }
}

public class StudentClassAttendanceDto
{
    public int ClassRoomId { get; set; }
    public string ClassName { get; set; } = string.Empty;
    public string SubjectName { get; set; } = string.Empty;
    public int TotalSessions { get; set; }
    public int AttendedSessions { get; set; }
    public double AttendanceRate { get; set; }
}
