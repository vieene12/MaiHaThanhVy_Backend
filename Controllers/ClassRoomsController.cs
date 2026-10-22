using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MaiHaThanhVy_Backend.Data;
using MaiHaThanhVy_Backend.DTOs.Classes;
using MaiHaThanhVy_Backend.DTOs.Common;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ClassRoomsController : ControllerBase
{
    private readonly AppDbContext _context;

    public ClassRoomsController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll(
        [FromQuery] int? teacherId,
        [FromQuery] int? subjectId,
        [FromQuery] string? keyword,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        try
        {
            if (page < 1) page = 1;
            if (pageSize < 1) pageSize = 10;
            if (pageSize > 100) pageSize = 100;

            var query = _context.ClassRooms
                .AsNoTracking()
                .Include(c => c.Subject)
                .Include(c => c.Teacher)
                .Include(c => c.ClassEnrollments)
                .Where(c => c.IsActive);

            if (teacherId.HasValue)
            {
                query = query.Where(c => c.TeacherId == teacherId.Value);
            }

            if (subjectId.HasValue)
            {
                query = query.Where(c => c.SubjectId == subjectId.Value);
            }

            if (!string.IsNullOrWhiteSpace(keyword))
            {
                var kw = keyword.Trim().ToLower();
                query = query.Where(c =>
                    c.ClassName.ToLower().Contains(kw) ||
                    (c.Room != null && c.Room.ToLower().Contains(kw)) ||
                    (c.Semester != null && c.Semester.ToLower().Contains(kw)) ||
                    c.Subject!.SubjectName.ToLower().Contains(kw) ||
                    c.Teacher!.FullName.ToLower().Contains(kw));
            }

            var totalCount = await query.CountAsync();

            var classes = await query
                .OrderByDescending(c => c.CreatedAt)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .Select(c => new ClassRoomResponse
                {
                    Id = c.Id,
                    ClassName = c.ClassName,
                    SubjectId = c.SubjectId,
                    SubjectCode = c.Subject != null ? c.Subject.SubjectCode : string.Empty,
                    SubjectName = c.Subject != null ? c.Subject.SubjectName : string.Empty,
                    Credits = c.Subject != null ? c.Subject.Credits : 0,
                    TeacherId = c.TeacherId,
                    TeacherName = c.Teacher != null ? c.Teacher.FullName : string.Empty,
                    TeacherEmail = c.Teacher != null ? c.Teacher.Email : string.Empty,
                    Room = c.Room,
                    Semester = c.Semester,
                    Schedule = c.Schedule,
                    MaxStudents = c.MaxStudents,
                    EnrolledStudentsCount = c.ClassEnrollments.Count(e => e.IsActive),
                    IsActive = c.IsActive,
                    CreatedAt = c.CreatedAt,
                    UpdatedAt = c.UpdatedAt
                })
                .ToListAsync();

            var result = new PagedResult<ClassRoomResponse>
            {
                Items = classes,
                TotalCount = totalCount,
                PageNumber = page,
                PageSize = pageSize
            };

            return Ok(ApiResponse<PagedResult<ClassRoomResponse>>.SuccessResponse(result, "Lấy danh sách lớp học thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpGet("{id}")]
    public async Task<IActionResult> GetById(int id)
    {
        try
        {
            var c = await _context.ClassRooms
                .AsNoTracking()
                .Include(cr => cr.Subject)
                .Include(cr => cr.Teacher)
                .Include(cr => cr.ClassEnrollments)
                .FirstOrDefaultAsync(cr => cr.Id == id);

            if (c == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy lớp học"));
            }

            var response = new ClassRoomResponse
            {
                Id = c.Id,
                ClassName = c.ClassName,
                SubjectId = c.SubjectId,
                SubjectCode = c.Subject != null ? c.Subject.SubjectCode : string.Empty,
                SubjectName = c.Subject != null ? c.Subject.SubjectName : string.Empty,
                Credits = c.Subject != null ? c.Subject.Credits : 0,
                TeacherId = c.TeacherId,
                TeacherName = c.Teacher != null ? c.Teacher.FullName : string.Empty,
                TeacherEmail = c.Teacher != null ? c.Teacher.Email : string.Empty,
                Room = c.Room,
                Semester = c.Semester,
                Schedule = c.Schedule,
                MaxStudents = c.MaxStudents,
                EnrolledStudentsCount = c.ClassEnrollments.Count(e => e.IsActive),
                IsActive = c.IsActive,
                CreatedAt = c.CreatedAt,
                UpdatedAt = c.UpdatedAt
            };

            return Ok(ApiResponse<ClassRoomResponse>.SuccessResponse(response));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Create([FromBody] ClassRoomDto dto)
    {
        try
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
            }

            var subjectExists = await _context.Subjects.AnyAsync(s => s.Id == dto.SubjectId && s.IsActive);
            if (!subjectExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Môn học không tồn tại hoặc đã bị vô hiệu hóa"));
            }

            var teacherExists = await _context.Teachers.AnyAsync(t => t.Id == dto.TeacherId && t.IsActive);
            if (!teacherExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Giảng viên không tồn tại hoặc đã bị vô hiệu hóa"));
            }

            var classRoom = new ClassRoom
            {
                ClassName = dto.ClassName.Trim(),
                SubjectId = dto.SubjectId,
                TeacherId = dto.TeacherId,
                Room = dto.Room?.Trim(),
                Semester = dto.Semester?.Trim(),
                Schedule = dto.Schedule?.Trim(),
                MaxStudents = dto.MaxStudents,
                IsActive = dto.IsActive,
                CreatedAt = DateTime.UtcNow
            };

            _context.ClassRooms.Add(classRoom);
            await _context.SaveChangesAsync();

            var subject = await _context.Subjects.FindAsync(classRoom.SubjectId);
            var teacher = await _context.Teachers.FindAsync(classRoom.TeacherId);

            var response = new ClassRoomResponse
            {
                Id = classRoom.Id,
                ClassName = classRoom.ClassName,
                SubjectId = classRoom.SubjectId,
                SubjectCode = subject?.SubjectCode ?? string.Empty,
                SubjectName = subject?.SubjectName ?? string.Empty,
                Credits = subject?.Credits ?? 0,
                TeacherId = classRoom.TeacherId,
                TeacherName = teacher?.FullName ?? string.Empty,
                TeacherEmail = teacher?.Email ?? string.Empty,
                Room = classRoom.Room,
                Semester = classRoom.Semester,
                Schedule = classRoom.Schedule,
                MaxStudents = classRoom.MaxStudents,
                EnrolledStudentsCount = 0,
                IsActive = classRoom.IsActive,
                CreatedAt = classRoom.CreatedAt
            };

            return CreatedAtAction(nameof(GetById), new { id = classRoom.Id }, ApiResponse<ClassRoomResponse>.SuccessResponse(response, "Tạo lớp học thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPut("{id}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Update(int id, [FromBody] ClassRoomDto dto)
    {
        try
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
            }

            var classRoom = await _context.ClassRooms.FindAsync(id);
            if (classRoom == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy lớp học"));
            }

            var subjectExists = await _context.Subjects.AnyAsync(s => s.Id == dto.SubjectId && s.IsActive);
            if (!subjectExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Môn học không tồn tại"));
            }

            var teacherExists = await _context.Teachers.AnyAsync(t => t.Id == dto.TeacherId && t.IsActive);
            if (!teacherExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Giảng viên không tồn tại"));
            }

            classRoom.ClassName = dto.ClassName.Trim();
            classRoom.SubjectId = dto.SubjectId;
            classRoom.TeacherId = dto.TeacherId;
            classRoom.Room = dto.Room?.Trim();
            classRoom.Semester = dto.Semester?.Trim();
            classRoom.Schedule = dto.Schedule?.Trim();
            classRoom.MaxStudents = dto.MaxStudents;
            classRoom.IsActive = dto.IsActive;
            classRoom.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            var subject = await _context.Subjects.FindAsync(classRoom.SubjectId);
            var teacher = await _context.Teachers.FindAsync(classRoom.TeacherId);
            var enrolledCount = await _context.ClassEnrollments.CountAsync(e => e.ClassRoomId == id && e.IsActive);

            var response = new ClassRoomResponse
            {
                Id = classRoom.Id,
                ClassName = classRoom.ClassName,
                SubjectId = classRoom.SubjectId,
                SubjectCode = subject?.SubjectCode ?? string.Empty,
                SubjectName = subject?.SubjectName ?? string.Empty,
                Credits = subject?.Credits ?? 0,
                TeacherId = classRoom.TeacherId,
                TeacherName = teacher?.FullName ?? string.Empty,
                TeacherEmail = teacher?.Email ?? string.Empty,
                Room = classRoom.Room,
                Semester = classRoom.Semester,
                Schedule = classRoom.Schedule,
                MaxStudents = classRoom.MaxStudents,
                EnrolledStudentsCount = enrolledCount,
                IsActive = classRoom.IsActive,
                CreatedAt = classRoom.CreatedAt,
                UpdatedAt = classRoom.UpdatedAt
            };

            return Ok(ApiResponse<ClassRoomResponse>.SuccessResponse(response, "Cập nhật lớp học thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpDelete("{id}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(int id)
    {
        try
        {
            var classRoom = await _context.ClassRooms.FindAsync(id);
            if (classRoom == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy lớp học"));
            }

            classRoom.IsActive = false;
            classRoom.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return Ok(ApiResponse.SuccessResponse("Xóa lớp học thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPost("{id}/enroll")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> EnrollStudent(int id, [FromBody] EnrollStudentDto dto)
    {
        try
        {
            var classRoom = await _context.ClassRooms.FindAsync(id);
            if (classRoom == null || !classRoom.IsActive)
            {
                return NotFound(ApiResponse.ErrorResponse("Lớp học không tồn tại hoặc đã kết thúc"));
            }

            var student = await _context.Students.FindAsync(dto.StudentId);
            if (student == null || !student.IsActive)
            {
                return NotFound(ApiResponse.ErrorResponse("Sinh viên không tồn tại hoặc đã bị vô hiệu hóa"));
            }

            var currentEnrolled = await _context.ClassEnrollments.CountAsync(e => e.ClassRoomId == id && e.IsActive);
            if (currentEnrolled >= classRoom.MaxStudents)
            {
                return BadRequest(ApiResponse.ErrorResponse($"Lớp học đã đạt sĩ số tối đa ({classRoom.MaxStudents} sinh viên)"));
            }

            var existingEnrollment = await _context.ClassEnrollments
                .FirstOrDefaultAsync(e => e.ClassRoomId == id && e.StudentId == dto.StudentId);

            if (existingEnrollment != null)
            {
                if (existingEnrollment.IsActive)
                {
                    return BadRequest(ApiResponse.ErrorResponse("Sinh viên đã có trong danh sách lớp học này"));
                }
                else
                {
                    existingEnrollment.IsActive = true;
                    existingEnrollment.EnrolledAt = DateTime.UtcNow;
                    await _context.SaveChangesAsync();

                    return Ok(ApiResponse.SuccessResponse("Đã kích hoạt lại ghi danh sinh viên vào lớp thành công"));
                }
            }

            var enrollment = new ClassEnrollment
            {
                ClassRoomId = id,
                StudentId = dto.StudentId,
                EnrolledAt = DateTime.UtcNow,
                IsActive = true
            };

            _context.ClassEnrollments.Add(enrollment);
            await _context.SaveChangesAsync();

            return Ok(ApiResponse.SuccessResponse("Thêm sinh viên vào lớp học thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpDelete("{id}/enroll/{studentId}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> RemoveStudentFromClass(int id, int studentId)
    {
        try
        {
            var enrollment = await _context.ClassEnrollments
                .FirstOrDefaultAsync(e => e.ClassRoomId == id && e.StudentId == studentId && e.IsActive);

            if (enrollment == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy thông tin ghi danh của sinh viên trong lớp này"));
            }

            enrollment.IsActive = false;
            await _context.SaveChangesAsync();

            return Ok(ApiResponse.SuccessResponse("Đã xóa sinh viên khỏi lớp học"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }
}
