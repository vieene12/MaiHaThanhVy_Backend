    using System.Security.Claims;
    using Microsoft.AspNetCore.Authorization;
    using Microsoft.AspNetCore.Mvc;
    using Microsoft.EntityFrameworkCore;
    using MaiHaThanhVy_Backend.Data;
    using MaiHaThanhVy_Backend.DTOs.Attendance;
    using MaiHaThanhVy_Backend.DTOs.Common;
    using MaiHaThanhVy_Backend.Models;

    namespace MaiHaThanhVy_Backend.Controllers;

    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class AttendanceController : ControllerBase
    {
	    private const double MaxDistanceMeters = 100.0;

	    private static double CalculateDistanceMeters(
		    double lat1, double lon1,
		    double lat2, double lon2)
	    {
		    const double earthRadius = 6371000.0;

		    double ToRadians(double degrees) =>
			    degrees * Math.PI / 180.0;

		    double dLat = ToRadians(lat2 - lat1);
		    double dLon = ToRadians(lon2 - lon1);

		    double a =
			    Math.Pow(Math.Sin(dLat / 2), 2) +
			    Math.Cos(ToRadians(lat1)) *
			    Math.Cos(ToRadians(lat2)) *
			    Math.Pow(Math.Sin(dLon / 2), 2);

		    a = Math.Clamp(a, 0.0, 1.0);

		    return 2 * earthRadius *
			    Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
	    }
	    private readonly AppDbContext _context;

        public AttendanceController(AppDbContext context)
        {
            _context = context;
        }

        [HttpPost("sessions")]
        [Authorize(Roles = "TEACHER,ADMIN")]
        public async Task<IActionResult> CreateSession([FromBody] AttendanceSessionDto dto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
                }

                var classRoom = await _context.ClassRooms
                    .Include(c => c.Teacher)
                    .Include(c => c.Subject)
                    .FirstOrDefaultAsync(c => c.Id == dto.ClassRoomId && c.IsActive);

                if (classRoom == null)
                {
                    return NotFound(ApiResponse.ErrorResponse("Lớp học không tồn tại"));
                }

                var teacherExists = await _context.Teachers.AnyAsync(t => t.Id == dto.TeacherId && t.IsActive);
                if (!teacherExists)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Giảng viên không tồn tại"));
                }

                if (!TimeSpan.TryParse(dto.StartTime, out var startTime))
                {
                    startTime = new TimeSpan(8, 0, 0);
                }

                if (!TimeSpan.TryParse(dto.EndTime, out var endTime))
                {
                    endTime = new TimeSpan(11, 30, 0);
                }

                // Generate unique QR code if not provided
                var qrCode = !string.IsNullOrWhiteSpace(dto.QRCode)
                    ? dto.QRCode.Trim()
                    : $"ATT-{classRoom.Id}-{DateTime.UtcNow.Ticks}-{Guid.NewGuid().ToString("N")[..8]}";

                var session = new AttendanceSession
                {
                    ClassRoomId = dto.ClassRoomId,
                    TeacherId = dto.TeacherId,
                    SessionDate = dto.SessionDate.Date,
                    StartTime = startTime,
                    EndTime = endTime,
                    QRCode = qrCode,
                    Latitude = dto.Latitude,
                    Longitude = dto.Longitude,
                    WifiSSID = dto.WifiSSID?.Trim(),
                    Note = dto.Note?.Trim(),
                    IsActive = dto.IsActive,
                    CreatedAt = DateTime.UtcNow
                };

                _context.AttendanceSessions.Add(session);
                await _context.SaveChangesAsync();

                var totalStudents = await _context.ClassEnrollments.CountAsync(e => e.ClassRoomId == dto.ClassRoomId && e.IsActive);

                var response = new AttendanceSessionResponse
                {
                    Id = session.Id,
                    ClassRoomId = session.ClassRoomId,
                    ClassName = classRoom.ClassName,
                    SubjectName = classRoom.Subject?.SubjectName ?? string.Empty,
                    TeacherId = session.TeacherId,
                    TeacherName = classRoom.Teacher?.FullName ?? string.Empty,
                    SessionDate = session.SessionDate,
                    StartTime = session.StartTime.ToString(@"hh\:mm"),
                    EndTime = session.EndTime.ToString(@"hh\:mm"),
                    QRCode = session.QRCode,
                    Latitude = session.Latitude,
                    Longitude = session.Longitude,
                    WifiSSID = session.WifiSSID,
                    Note = session.Note,
                    IsActive = session.IsActive,
                    CreatedAt = session.CreatedAt,
                    TotalStudents = totalStudents,
                    PresentCount = 0,
                    LateCount = 0,
                    AbsentCount = 0,
                    ExcusedCount = 0
                };

                return CreatedAtAction(nameof(GetSessionById), new { sessionId = session.Id }, ApiResponse<AttendanceSessionResponse>.SuccessResponse(response, "Tạo phiên điểm danh thành công"));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }

        [HttpGet("sessions/{sessionId}")]
        public async Task<IActionResult> GetSessionById(int sessionId)
        {
            try
            {
                var session = await _context.AttendanceSessions
                    .AsNoTracking()
                    .Include(s => s.ClassRoom)
                        .ThenInclude(c => c!.Subject)
                    .Include(s => s.Teacher)
                    .Include(s => s.AttendanceRecords)
                    .FirstOrDefaultAsync(s => s.Id == sessionId);

                if (session == null)
                {
                    return NotFound(ApiResponse.ErrorResponse("Không tìm thấy phiên điểm danh"));
                }

                var totalStudents = await _context.ClassEnrollments.CountAsync(e => e.ClassRoomId == session.ClassRoomId && e.IsActive);

                var response = new AttendanceSessionResponse
                {
                    Id = session.Id,
                    ClassRoomId = session.ClassRoomId,
                    ClassName = session.ClassRoom?.ClassName ?? string.Empty,
                    SubjectName = session.ClassRoom?.Subject?.SubjectName ?? string.Empty,
                    TeacherId = session.TeacherId,
                    TeacherName = session.Teacher?.FullName ?? string.Empty,
                    SessionDate = session.SessionDate,
                    StartTime = session.StartTime.ToString(@"hh\:mm"),
                    EndTime = session.EndTime.ToString(@"hh\:mm"),
                    QRCode = session.QRCode,
                    Latitude = session.Latitude,
                    Longitude = session.Longitude,
                    WifiSSID = session.WifiSSID,
                    Note = session.Note,
                    IsActive = session.IsActive,
                    CreatedAt = session.CreatedAt,
                    TotalStudents = totalStudents,
                    PresentCount = session.AttendanceRecords.Count(r => r.Status == "PRESENT"),
                    LateCount = session.AttendanceRecords.Count(r => r.Status == "LATE"),
                    AbsentCount = session.AttendanceRecords.Count(r => r.Status == "ABSENT"),
                    ExcusedCount = session.AttendanceRecords.Count(r => r.Status == "EXCUSED")
                };

                return Ok(ApiResponse<AttendanceSessionResponse>.SuccessResponse(response));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }

        [HttpGet("sessions/by-class/{classId}")]
        [Authorize(Roles = "TEACHER,ADMIN")]
        public async Task<IActionResult> GetSessionsByClass(int classId)
        {
            try
            {
                var classRoom = await _context.ClassRooms
                    .Include(c => c.Subject)
                    .FirstOrDefaultAsync(c => c.Id == classId);

                if (classRoom == null)
                {
                    return NotFound(ApiResponse.ErrorResponse("Không tìm thấy lớp học"));
                }

                var totalEnrolled = await _context.ClassEnrollments.CountAsync(e => e.ClassRoomId == classId && e.IsActive);

                var sessions = await _context.AttendanceSessions
                    .AsNoTracking()
                    .Where(s => s.ClassRoomId == classId)
                    .Include(s => s.Teacher)
                    .Include(s => s.AttendanceRecords)
                    .OrderByDescending(s => s.SessionDate)
                    .ThenByDescending(s => s.StartTime)
                    .Select(s => new AttendanceSessionResponse
                    {
                        Id = s.Id,
                        ClassRoomId = s.ClassRoomId,
                        ClassName = classRoom.ClassName,
                        SubjectName = classRoom.Subject != null ? classRoom.Subject.SubjectName : string.Empty,
                        TeacherId = s.TeacherId,
                        TeacherName = s.Teacher != null ? s.Teacher.FullName : string.Empty,
                        SessionDate = s.SessionDate,
                        StartTime = s.StartTime.ToString(@"hh\:mm"),
                        EndTime = s.EndTime.ToString(@"hh\:mm"),
                        QRCode = s.QRCode,
                        Latitude = s.Latitude,
                        Longitude = s.Longitude,
                        WifiSSID = s.WifiSSID,
                        Note = s.Note,
                        IsActive = s.IsActive,
                        CreatedAt = s.CreatedAt,
                        TotalStudents = totalEnrolled,
                        PresentCount = s.AttendanceRecords.Count(r => r.Status == "PRESENT"),
                        LateCount = s.AttendanceRecords.Count(r => r.Status == "LATE"),
                        AbsentCount = s.AttendanceRecords.Count(r => r.Status == "ABSENT"),
                        ExcusedCount = s.AttendanceRecords.Count(r => r.Status == "EXCUSED")
                    })
                    .ToListAsync();

                return Ok(ApiResponse<List<AttendanceSessionResponse>>.SuccessResponse(sessions, $"Danh sách các phiên điểm danh lớp {classRoom.ClassName}"));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }

        [HttpPost("sessions/{sessionId}/check-in")]
        [Authorize(Roles = "TEACHER,ADMIN")]
        public async Task<IActionResult> ManualCheckIn(int sessionId, [FromBody] ManualCheckInDto dto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
                }

                var session = await _context.AttendanceSessions.FindAsync(sessionId);
                if (session == null || !session.IsActive)
                {
                    return NotFound(ApiResponse.ErrorResponse("Phiên điểm danh không tồn tại hoặc đã kết thúc"));
                }

                var isEnrolled = await _context.ClassEnrollments.AnyAsync(e => e.ClassRoomId == session.ClassRoomId && e.StudentId == dto.StudentId && e.IsActive);
                if (!isEnrolled)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Sinh viên không thuộc lớp học này"));
                }

                var existingRecord = await _context.AttendanceRecords
                    .FirstOrDefaultAsync(r => r.SessionId == sessionId && r.StudentId == dto.StudentId);

                if (existingRecord != null)
                {
                    existingRecord.Status = dto.Status.ToUpper();
                    existingRecord.Method = "MANUAL";
                    existingRecord.Note = dto.Note;
                    existingRecord.CheckInTime = DateTime.UtcNow;
                    existingRecord.UpdatedAt = DateTime.UtcNow;
                }
                else
                {
                    var record = new AttendanceRecord
                    {
                        SessionId = sessionId,
                        StudentId = dto.StudentId,
                        CheckInTime = DateTime.UtcNow,
                        Method = "MANUAL",
                        Status = dto.Status.ToUpper(),
                        Note = dto.Note,
                        CreatedAt = DateTime.UtcNow
                    };
                    _context.AttendanceRecords.Add(record);
                }

                await _context.SaveChangesAsync();
                return Ok(ApiResponse.SuccessResponse("Ghi nhận điểm danh thủ công thành công"));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }

        [HttpPost("sessions/{sessionId}/qr-checkin")]
        [Authorize(Roles = "STUDENT,ADMIN")]
        public async Task<IActionResult> QrCheckIn(int sessionId, [FromBody] QrCheckInDto dto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
                }

                var session = await _context.AttendanceSessions.FindAsync(sessionId);
                if (session == null || !session.IsActive)
                {
                    return NotFound(ApiResponse.ErrorResponse("Phiên điểm danh không tồn tại hoặc đã đóng"));
                }

                if (string.IsNullOrEmpty(session.QRCode) || !string.Equals(session.QRCode.Trim(), dto.QRCode.Trim(), StringComparison.OrdinalIgnoreCase))
                {
                    return BadRequest(ApiResponse.ErrorResponse("Mã QR không đúng hoặc đã hết hạn"));
                }
			    if (session.Latitude == null ||
				    session.Longitude == null)
			    {
				    return BadRequest(
					    ApiResponse.ErrorResponse(
						    "Buổi học chưa có tọa độ GPS."));
			    }

			    if (dto.Latitude == null ||
				    dto.Longitude == null)
			    {
				    return BadRequest(
					    ApiResponse.ErrorResponse(
						    "Vui lòng bật GPS để điểm danh."));
			    }

			    double schoolLat =
				    Convert.ToDouble(session.Latitude);

			    double schoolLon =
				    Convert.ToDouble(session.Longitude);

			    double studentLat =
				    Convert.ToDouble(dto.Latitude);

			    double studentLon =
				    Convert.ToDouble(dto.Longitude);

			    if (!double.IsFinite(schoolLat) ||
				    !double.IsFinite(schoolLon) ||
				    !double.IsFinite(studentLat) ||
				    !double.IsFinite(studentLon) ||
				    Math.Abs(schoolLat) > 90 ||
				    Math.Abs(studentLat) > 90 ||
				    Math.Abs(schoolLon) > 180 ||
				    Math.Abs(studentLon) > 180)
			    {
				    return BadRequest(
					    ApiResponse.ErrorResponse(
						    "Tọa độ GPS không hợp lệ."));
			    }

			    double distance = CalculateDistanceMeters(
				    schoolLat,
				    schoolLon,
				    studentLat,
				    studentLon
			    );

			    if (distance > MaxDistanceMeters)
			    {
				    return BadRequest(
					    ApiResponse.ErrorResponse(
						    $"Bạn cách lớp học {distance:F1} mét. " +
						    "Chỉ được điểm danh trong bán kính 100 mét."));
			    }


			    var isEnrolled = await _context.ClassEnrollments.AnyAsync(e => e.ClassRoomId == session.ClassRoomId && e.StudentId == dto.StudentId && e.IsActive);
                if (!isEnrolled)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Bạn không có trong danh sách lớp học này"));
                }

                var existingRecord = await _context.AttendanceRecords
                    .FirstOrDefaultAsync(r => r.SessionId == sessionId && r.StudentId == dto.StudentId);

                if (existingRecord != null)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Bạn đã điểm danh trong phiên này rồi"));
                }

                // Determine if Late based on current time and start time
                var currentTime = DateTime.UtcNow.TimeOfDay;
                var status = "PRESENT";
                if (session.StartTime != TimeSpan.Zero && (currentTime - session.StartTime).TotalMinutes > 15)
                {
                    status = "LATE";
                }

                var record = new AttendanceRecord
                {
                    SessionId = sessionId,
                    StudentId = dto.StudentId,
                    CheckInTime = DateTime.UtcNow,
                    Method = "QR",
                    Status = status,
                    Latitude = dto.Latitude,
                    Longitude = dto.Longitude,
                    Note = "Điểm danh qua mã QR",
                    CreatedAt = DateTime.UtcNow
                };

                _context.AttendanceRecords.Add(record);
                await _context.SaveChangesAsync();

                var student = await _context.Students.FindAsync(dto.StudentId);

                var response = new AttendanceRecordResponse
                {
                    Id = record.Id,
                    SessionId = record.SessionId,
                    StudentId = record.StudentId,
                    StudentCode = student?.StudentCode ?? string.Empty,
                    StudentName = student?.FullName ?? string.Empty,
                    CheckInTime = record.CheckInTime,
                    Method = record.Method,
                    Status = record.Status,
                    Latitude = record.Latitude,
                    Longitude = record.Longitude,
                    Note = record.Note,
                    CreatedAt = record.CreatedAt
                };

                return Ok(ApiResponse<AttendanceRecordResponse>.SuccessResponse(response, $"Điểm danh thành công! Trạng thái: {(status == "PRESENT" ? "Có mặt" : "Đi muộn")}"));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }

        [HttpPost("sessions/{sessionId}/wifi-checkin")]
        [Authorize(Roles = "STUDENT,ADMIN")]
        public async Task<IActionResult> WifiCheckIn(int sessionId, [FromBody] WifiCheckInDto dto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
                }

                var session = await _context.AttendanceSessions.FindAsync(sessionId);
                if (session == null || !session.IsActive)
                {
                    return NotFound(ApiResponse.ErrorResponse("Phiên điểm danh không tồn tại hoặc đã đóng"));
                }

                if (string.IsNullOrEmpty(session.WifiSSID) ||
                    !session.WifiSSID.Trim().Equals(dto.WifiSSID.Trim(), StringComparison.OrdinalIgnoreCase))
                {
                    return BadRequest(ApiResponse.ErrorResponse($"Tên mạng WiFi không khớp (Yêu cầu: {session.WifiSSID})"));
                }

                var isEnrolled = await _context.ClassEnrollments.AnyAsync(e => e.ClassRoomId == session.ClassRoomId && e.StudentId == dto.StudentId && e.IsActive);
                if (!isEnrolled)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Bạn không có trong danh sách lớp học này"));
                }

                var existingRecord = await _context.AttendanceRecords
                    .FirstOrDefaultAsync(r => r.SessionId == sessionId && r.StudentId == dto.StudentId);

                if (existingRecord != null)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Bạn đã điểm danh trong phiên này rồi"));
                }

                var currentTime = DateTime.UtcNow.TimeOfDay;
                var status = "PRESENT";
                if (session.StartTime != TimeSpan.Zero && (currentTime - session.StartTime).TotalMinutes > 15)
                {
                    status = "LATE";
                }

                var record = new AttendanceRecord
                {
                    SessionId = sessionId,
                    StudentId = dto.StudentId,
                    CheckInTime = DateTime.UtcNow,
                    Method = "WIFI",
                    Status = status,
                    Latitude = dto.Latitude,
                    Longitude = dto.Longitude,
                    Note = $"Điểm danh qua mạng WiFi: {dto.WifiSSID}",
                    CreatedAt = DateTime.UtcNow
                };

                _context.AttendanceRecords.Add(record);
                await _context.SaveChangesAsync();

                var student = await _context.Students.FindAsync(dto.StudentId);

                var response = new AttendanceRecordResponse
                {
                    Id = record.Id,
                    SessionId = record.SessionId,
                    StudentId = record.StudentId,
                    StudentCode = student?.StudentCode ?? string.Empty,
                    StudentName = student?.FullName ?? string.Empty,
                    CheckInTime = record.CheckInTime,
                    Method = record.Method,
                    Status = record.Status,
                    Latitude = record.Latitude,
                    Longitude = record.Longitude,
                    Note = record.Note,
                    CreatedAt = record.CreatedAt
                };

                return Ok(ApiResponse<AttendanceRecordResponse>.SuccessResponse(response, $"Điểm danh qua WiFi thành công! Trạng thái: {(status == "PRESENT" ? "Có mặt" : "Đi muộn")}"));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }

        [HttpGet("student/{studentId}")]
        public async Task<IActionResult> GetStudentAttendanceHistory(int studentId)
        {
            try
            {
                var student = await _context.Students.FindAsync(studentId);
                if (student == null)
                {
                    return NotFound(ApiResponse.ErrorResponse("Không tìm thấy sinh viên"));
                }

                var records = await _context.AttendanceRecords
                    .AsNoTracking()
                    .Where(r => r.StudentId == studentId)
                    .Include(r => r.Session)
                        .ThenInclude(s => s!.ClassRoom)
                            .ThenInclude(c => c!.Subject)
                    .OrderByDescending(r => r.CheckInTime)
                    .Select(r => new
                    {
                        RecordId = r.Id,
                        SessionId = r.SessionId,
                        ClassName = r.Session != null && r.Session.ClassRoom != null ? r.Session.ClassRoom.ClassName : string.Empty,
                        SubjectName = r.Session != null && r.Session.ClassRoom != null && r.Session.ClassRoom.Subject != null ? r.Session.ClassRoom.Subject.SubjectName : string.Empty,
                        SessionDate = r.Session != null ? r.Session.SessionDate : DateTime.MinValue,
                        CheckInTime = r.CheckInTime,
                        Method = r.Method,
                        Status = r.Status,
                        Note = r.Note
                    })
                    .ToListAsync();

                return Ok(ApiResponse<object>.SuccessResponse(records, "Lấy lịch sử điểm danh của sinh viên thành công"));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }

        [HttpGet("report/{classId}")]
        [Authorize(Roles = "TEACHER,ADMIN")]
        public async Task<IActionResult> GetClassAttendanceReport(int classId)
        {
            try
            {
                var classRoom = await _context.ClassRooms
                    .Include(c => c.Subject)
                    .FirstOrDefaultAsync(c => c.Id == classId);

                if (classRoom == null)
                {
                    return NotFound(ApiResponse.ErrorResponse("Không tìm thấy lớp học"));
                }

                var sessions = await _context.AttendanceSessions
                    .Where(s => s.ClassRoomId == classId)
                    .Select(s => s.Id)
                    .ToListAsync();

                var totalSessions = sessions.Count;

                var enrolledStudents = await _context.ClassEnrollments
                    .AsNoTracking()
                    .Where(e => e.ClassRoomId == classId && e.IsActive)
                    .Include(e => e.Student)
                    .ToListAsync();

                var records = await _context.AttendanceRecords
                    .AsNoTracking()
                    .Where(r => sessions.Contains(r.SessionId))
                    .ToListAsync();

                var studentsSummary = new List<StudentAttendanceSummaryDto>();

                foreach (var enrollment in enrolledStudents)
                {
                    if (enrollment.Student == null) continue;

                    var studentRecords = records.Where(r => r.StudentId == enrollment.StudentId).ToList();
                    var present = studentRecords.Count(r => r.Status == "PRESENT");
                    var late = studentRecords.Count(r => r.Status == "LATE");
                    var excused = studentRecords.Count(r => r.Status == "EXCUSED");
                    var absent = totalSessions - (present + late + excused);
                    if (absent < 0) absent = 0;

                    double rate = totalSessions > 0
                        ? Math.Round(((double)(present + late) / totalSessions) * 100, 2)
                        : 100.0;

                    studentsSummary.Add(new StudentAttendanceSummaryDto
                    {
                        StudentId = enrollment.Student.Id,
                        StudentCode = enrollment.Student.StudentCode,
                        StudentName = enrollment.Student.FullName,
                        TotalSessions = totalSessions,
                        PresentSessions = present,
                        LateSessions = late,
                        AbsentSessions = absent,
                        ExcusedSessions = excused,
                        AttendanceRate = rate
                    });
                }

                var report = new ClassAttendanceReportResponse
                {
                    ClassRoomId = classRoom.Id,
                    ClassName = classRoom.ClassName,
                    SubjectName = classRoom.Subject?.SubjectName ?? string.Empty,
                    TotalSessions = totalSessions,
                    TotalStudents = enrolledStudents.Count,
                    StudentsSummary = studentsSummary
                };

                return Ok(ApiResponse<ClassAttendanceReportResponse>.SuccessResponse(report, "Báo cáo điểm danh"));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }

        [HttpPut("records/{recordId}")]
        [Authorize(Roles = "TEACHER,ADMIN")]
        public async Task<IActionResult> UpdateRecordStatus(int recordId, [FromBody] UpdateRecordStatusDto dto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
                }

                var record = await _context.AttendanceRecords
                    .Include(r => r.Student)
                    .FirstOrDefaultAsync(r => r.Id == recordId);

                if (record == null)
                {
                    return NotFound(ApiResponse.ErrorResponse("Không tìm thấy bản ghi điểm danh"));
                }

                record.Status = dto.Status.ToUpper();
                if (!string.IsNullOrWhiteSpace(dto.Note))
                {
                    record.Note = dto.Note.Trim();
                }
                record.UpdatedAt = DateTime.UtcNow;

                await _context.SaveChangesAsync();

                var response = new AttendanceRecordResponse
                {
                    Id = record.Id,
                    SessionId = record.SessionId,
                    StudentId = record.StudentId,
                    StudentCode = record.Student?.StudentCode ?? string.Empty,
                    StudentName = record.Student?.FullName ?? string.Empty,
                    CheckInTime = record.CheckInTime,
                    Method = record.Method,
                    Status = record.Status,
                    Latitude = record.Latitude,
                    Longitude = record.Longitude,
                    Note = record.Note,
                    CreatedAt = record.CreatedAt,
                    UpdatedAt = record.UpdatedAt
                };

                return Ok(ApiResponse<AttendanceRecordResponse>.SuccessResponse(response, "Cập nhật trạng thái điểm danh thành công"));
            }
            catch (Exception ex)
            {
                return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
            }
        }
    }
