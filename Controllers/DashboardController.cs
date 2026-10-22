using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MaiHaThanhVy_Backend.Data;
using MaiHaThanhVy_Backend.DTOs.Common;
using MaiHaThanhVy_Backend.DTOs.Dashboard;

namespace MaiHaThanhVy_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class DashboardController : ControllerBase
{
    private readonly AppDbContext _context;

    public DashboardController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet("admin")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> GetAdminDashboard()
    {
        try
        {
            var today = DateTime.UtcNow.Date;

            var totalUsers = await _context.Users.CountAsync(u => u.IsActive);
            var totalStudents = await _context.Students.CountAsync(s => s.IsActive);
            var totalTeachers = await _context.Teachers.CountAsync(t => t.IsActive);
            var totalSubjects = await _context.Subjects.CountAsync(s => s.IsActive);
            var totalClasses = await _context.ClassRooms.CountAsync(c => c.IsActive);

            var totalSessionsToday = await _context.AttendanceSessions.CountAsync(s => s.SessionDate.Date == today);
            var totalAttendanceRecordsToday = await _context.AttendanceRecords.CountAsync(r => r.CheckInTime.Date == today);

            var totalRecords = await _context.AttendanceRecords.CountAsync();
            var presentRecords = await _context.AttendanceRecords.CountAsync(r => r.Status == "PRESENT" || r.Status == "LATE");
            double avgRate = totalRecords > 0 ? Math.Round(((double)presentRecords / totalRecords) * 100, 2) : 100.0;

            var recentRecords = await _context.AttendanceRecords
                .AsNoTracking()
                .Include(r => r.Student)
                .Include(r => r.Session)
                    .ThenInclude(s => s!.ClassRoom)
                .OrderByDescending(r => r.CheckInTime)
                .Take(5)
                .Select(r => new RecentActivityDto
                {
                    Type = "ATTENDANCE",
                    Description = $"Sinh viên {r.Student!.FullName} đã điểm danh ({r.Status}) lớp {r.Session!.ClassRoom!.ClassName} qua {r.Method}",
                    Timestamp = r.CheckInTime
                })
                .ToListAsync();

            var result = new AdminDashboardDto
            {
                TotalUsers = totalUsers,
                TotalStudents = totalStudents,
                TotalTeachers = totalTeachers,
                TotalSubjects = totalSubjects,
                TotalClasses = totalClasses,
                TotalSessionsToday = totalSessionsToday,
                TotalAttendanceRecordsToday = totalAttendanceRecordsToday,
                AverageAttendanceRate = avgRate,
                RecentActivities = recentRecords
            };

            return Ok(ApiResponse<AdminDashboardDto>.SuccessResponse(result, "Thống kê tổng quan Admin"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpGet("teacher/{teacherId}")]
    [Authorize(Roles = "TEACHER,ADMIN")]
    public async Task<IActionResult> GetTeacherDashboard(int teacherId)
    {
        try
        {
            var teacher = await _context.Teachers.FindAsync(teacherId);
            if (teacher == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy giảng viên"));
            }

            var today = DateTime.UtcNow.Date;

            var classes = await _context.ClassRooms
                .AsNoTracking()
                .Where(c => c.TeacherId == teacherId && c.IsActive)
                .Include(c => c.Subject)
                .Include(c => c.ClassEnrollments)
                .Include(c => c.AttendanceSessions)
                .ToListAsync();

            var classIds = classes.Select(c => c.Id).ToList();

            var totalStudents = await _context.ClassEnrollments
                .Where(e => classIds.Contains(e.ClassRoomId) && e.IsActive)
                .Select(e => e.StudentId)
                .Distinct()
                .CountAsync();

            var totalSessions = await _context.AttendanceSessions
                .CountAsync(s => s.TeacherId == teacherId);

            var todaySessionsCount = await _context.AttendanceSessions
                .CountAsync(s => s.TeacherId == teacherId && s.SessionDate.Date == today);

            var sessionIds = await _context.AttendanceSessions
                .Where(s => s.TeacherId == teacherId)
                .Select(s => s.Id)
                .ToListAsync();

            var teacherRecords = await _context.AttendanceRecords
                .Where(r => sessionIds.Contains(r.SessionId))
                .ToListAsync();

            var totalRecordsCount = teacherRecords.Count;
            var presentCount = teacherRecords.Count(r => r.Status == "PRESENT" || r.Status == "LATE");
            double overallRate = totalRecordsCount > 0 ? Math.Round(((double)presentCount / totalRecordsCount) * 100, 2) : 100.0;

            var classesSummary = classes.Select(c => new TeacherClassSummaryDto
            {
                ClassRoomId = c.Id,
                ClassName = c.ClassName,
                SubjectName = c.Subject?.SubjectName ?? string.Empty,
                StudentCount = c.ClassEnrollments.Count(e => e.IsActive),
                SessionCount = c.AttendanceSessions.Count
            }).ToList();

            var recentSessions = await _context.AttendanceSessions
                .AsNoTracking()
                .Where(s => s.TeacherId == teacherId)
                .Include(s => s.ClassRoom)
                .Include(s => s.AttendanceRecords)
                .OrderByDescending(s => s.SessionDate)
                .Take(5)
                .Select(s => new RecentSessionDto
                {
                    SessionId = s.Id,
                    ClassName = s.ClassRoom != null ? s.ClassRoom.ClassName : string.Empty,
                    SessionDate = s.SessionDate,
                    PresentCount = s.AttendanceRecords.Count(r => r.Status == "PRESENT" || r.Status == "LATE"),
                    TotalStudents = s.ClassRoom != null ? s.ClassRoom.ClassEnrollments.Count(e => e.IsActive) : 0
                })
                .ToListAsync();

            var result = new TeacherDashboardDto
            {
                TeacherId = teacher.Id,
                TeacherName = teacher.FullName,
                TotalClasses = classes.Count,
                TotalStudents = totalStudents,
                TotalSessions = totalSessions,
                TodaySessionsCount = todaySessionsCount,
                OverallAttendanceRate = overallRate,
                Classes = classesSummary,
                RecentSessions = recentSessions
            };

            return Ok(ApiResponse<TeacherDashboardDto>.SuccessResponse(result, "Dashboard giảng viên"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpGet("student/{studentId}")]
    [Authorize(Roles = "STUDENT,ADMIN")]
    public async Task<IActionResult> GetStudentDashboard(int studentId)
    {
        try
        {
            var student = await _context.Students.FindAsync(studentId);
            if (student == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy sinh viên"));
            }

            var enrolledClasses = await _context.ClassEnrollments
                .AsNoTracking()
                .Where(e => e.StudentId == studentId && e.IsActive)
                .Include(e => e.ClassRoom)
                    .ThenInclude(c => c!.Subject)
                .Include(e => e.ClassRoom)
                    .ThenInclude(c => c!.AttendanceSessions)
                .ToListAsync();

            var studentRecords = await _context.AttendanceRecords
                .AsNoTracking()
                .Where(r => r.StudentId == studentId)
                .ToListAsync();

            int presentCount = studentRecords.Count(r => r.Status == "PRESENT");
            int lateCount = studentRecords.Count(r => r.Status == "LATE");
            int excusedCount = studentRecords.Count(r => r.Status == "EXCUSED");

            int totalConductedSessions = 0;
            var classAttendanceList = new List<StudentClassAttendanceDto>();

            foreach (var enrollment in enrolledClasses)
            {
                if (enrollment.ClassRoom == null) continue;

                var roomSessions = enrollment.ClassRoom.AttendanceSessions.Select(s => s.Id).ToList();
                totalConductedSessions += roomSessions.Count;

                var attendedInClass = studentRecords.Count(r => roomSessions.Contains(r.SessionId) && (r.Status == "PRESENT" || r.Status == "LATE"));
                double classRate = roomSessions.Count > 0 ? Math.Round(((double)attendedInClass / roomSessions.Count) * 100, 2) : 100.0;

                classAttendanceList.Add(new StudentClassAttendanceDto
                {
                    ClassRoomId = enrollment.ClassRoom.Id,
                    ClassName = enrollment.ClassRoom.ClassName,
                    SubjectName = enrollment.ClassRoom.Subject?.SubjectName ?? string.Empty,
                    TotalSessions = roomSessions.Count,
                    AttendedSessions = attendedInClass,
                    AttendanceRate = classRate
                });
            }

            int absentCount = totalConductedSessions - (presentCount + lateCount + excusedCount);
            if (absentCount < 0) absentCount = 0;

            double overallAttendanceRate = totalConductedSessions > 0
                ? Math.Round(((double)(presentCount + lateCount) / totalConductedSessions) * 100, 2)
                : 100.0;

            var result = new StudentDashboardDto
            {
                StudentId = student.Id,
                StudentName = student.FullName,
                StudentCode = student.StudentCode,
                EnrolledClassesCount = enrolledClasses.Count,
                TotalSessions = totalConductedSessions,
                PresentCount = presentCount,
                LateCount = lateCount,
                AbsentCount = absentCount,
                ExcusedCount = excusedCount,
                AttendanceRate = overallAttendanceRate,
                EnrolledClasses = classAttendanceList
            };

            return Ok(ApiResponse<StudentDashboardDto>.SuccessResponse(result, "Dashboard sinh viên"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }
}
