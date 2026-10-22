using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MaiHaThanhVy_Backend.Data;
using MaiHaThanhVy_Backend.DTOs.Common;
using MaiHaThanhVy_Backend.DTOs.Students;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class StudentsController : ControllerBase
{
    private readonly AppDbContext _context;

    public StudentsController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    [Authorize(Roles = "ADMIN,TEACHER")]
    public async Task<IActionResult> GetAll([FromQuery] string? keyword, [FromQuery] int page = 1, [FromQuery] int pageSize = 10)
    {
        try
        {
            if (page < 1) page = 1;
            if (pageSize < 1) pageSize = 10;
            if (pageSize > 100) pageSize = 100;

            var query = _context.Students
                .AsNoTracking()
                .Include(s => s.ClassEnrollments)
                .Include(s => s.User)
                .Where(s => s.IsActive);

            if (!string.IsNullOrWhiteSpace(keyword))
            {
                var kw = keyword.Trim().ToLower();
                query = query.Where(s =>
                    s.FullName.ToLower().Contains(kw) ||
                    s.StudentCode.ToLower().Contains(kw) ||
                    s.Email.ToLower().Contains(kw) ||
                    (s.PhoneNumber != null && s.PhoneNumber.Contains(kw)) ||
                    (s.Major != null && s.Major.ToLower().Contains(kw)));
            }

            var totalCount = await query.CountAsync();

            var students = await query
                .OrderByDescending(s => s.CreatedAt)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .Select(s => new StudentResponse
                {
                    Id = s.Id,
                    StudentCode = s.StudentCode,
                    FullName = s.FullName,
                    Email = s.Email,
                    DateOfBirth = s.DateOfBirth,
                    Gender = s.Gender,
                    PhoneNumber = s.PhoneNumber,
                    Address = s.Address,
                    Avatar = s.Avatar,
                    Major = s.Major,
                    UserId = s.UserId,
                    IsActive = s.IsActive,
                    CreatedAt = s.CreatedAt,
                    UpdatedAt = s.UpdatedAt,
                    EnrolledClassesCount = s.ClassEnrollments.Count(e => e.IsActive),
                    HasFaceData = s.User != null && !string.IsNullOrEmpty(s.User.FaceData)
                })
                .ToListAsync();

            var result = new PagedResult<StudentResponse>
            {
                Items = students,
                TotalCount = totalCount,
                PageNumber = page,
                PageSize = pageSize
            };

            return Ok(ApiResponse<PagedResult<StudentResponse>>.SuccessResponse(result, "Lấy danh sách sinh viên thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpGet("{id}")]
    [Authorize(Roles = "ADMIN,TEACHER,STUDENT")]
    public async Task<IActionResult> GetById(int id)
    {
        try
        {
            var student = await _context.Students
                .AsNoTracking()
                .Include(s => s.ClassEnrollments)
                .Include(s => s.User)
                .FirstOrDefaultAsync(s => s.Id == id);

            if (student == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy sinh viên"));
            }

            var response = new StudentResponse
            {
                Id = student.Id,
                StudentCode = student.StudentCode,
                FullName = student.FullName,
                Email = student.Email,
                DateOfBirth = student.DateOfBirth,
                Gender = student.Gender,
                PhoneNumber = student.PhoneNumber,
                Address = student.Address,
                Avatar = student.Avatar,
                Major = student.Major,
                UserId = student.UserId,
                IsActive = student.IsActive,
                CreatedAt = student.CreatedAt,
                UpdatedAt = student.UpdatedAt,
                EnrolledClassesCount = student.ClassEnrollments.Count(e => e.IsActive),
                HasFaceData = student.User != null && !string.IsNullOrEmpty(student.User.FaceData)
            };

            return Ok(ApiResponse<StudentResponse>.SuccessResponse(response));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Create([FromBody] StudentDto dto)
    {
        try
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
            }

            var existingCode = await _context.Students.AnyAsync(s => s.StudentCode.ToLower() == dto.StudentCode.ToLower());
            if (existingCode)
            {
                return BadRequest(ApiResponse.ErrorResponse("Mã sinh viên đã tồn tại"));
            }

            var existingEmail = await _context.Students.AnyAsync(s => s.Email.ToLower() == dto.Email.ToLower());
            if (existingEmail)
            {
                return BadRequest(ApiResponse.ErrorResponse("Email sinh viên đã tồn tại"));
            }

            // If userId is not explicitly provided, try to find user with matching email
            int? userId = dto.UserId;
            if (userId == null)
            {
                var matchedUser = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == dto.Email.ToLower());
                if (matchedUser != null)
                {
                    userId = matchedUser.Id;
                }
            }

            var student = new Student
            {
                StudentCode = dto.StudentCode.Trim().ToUpper(),
                FullName = dto.FullName.Trim(),
                Email = dto.Email.Trim().ToLower(),
                DateOfBirth = dto.DateOfBirth,
                Gender = dto.Gender,
                PhoneNumber = dto.PhoneNumber?.Trim(),
                Address = dto.Address?.Trim(),
                Avatar = dto.Avatar,
                Major = dto.Major?.Trim(),
                UserId = userId,
                IsActive = dto.IsActive,
                CreatedAt = DateTime.UtcNow
            };

            _context.Students.Add(student);
            await _context.SaveChangesAsync();

            var response = new StudentResponse
            {
                Id = student.Id,
                StudentCode = student.StudentCode,
                FullName = student.FullName,
                Email = student.Email,
                DateOfBirth = student.DateOfBirth,
                Gender = student.Gender,
                PhoneNumber = student.PhoneNumber,
                Address = student.Address,
                Avatar = student.Avatar,
                Major = student.Major,
                UserId = student.UserId,
                IsActive = student.IsActive,
                CreatedAt = student.CreatedAt,
                EnrolledClassesCount = 0,
                HasFaceData = false
            };

            return CreatedAtAction(nameof(GetById), new { id = student.Id }, ApiResponse<StudentResponse>.SuccessResponse(response, "Thêm sinh viên thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPut("{id}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Update(int id, [FromBody] StudentDto dto)
    {
        try
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
            }

            var student = await _context.Students.FindAsync(id);
            if (student == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy sinh viên"));
            }

            var codeExists = await _context.Students.AnyAsync(s => s.Id != id && s.StudentCode.ToLower() == dto.StudentCode.ToLower());
            if (codeExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Mã sinh viên đã thuộc về sinh viên khác"));
            }

            var emailExists = await _context.Students.AnyAsync(s => s.Id != id && s.Email.ToLower() == dto.Email.ToLower());
            if (emailExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Email đã thuộc về sinh viên khác"));
            }

            student.StudentCode = dto.StudentCode.Trim().ToUpper();
            student.FullName = dto.FullName.Trim();
            student.Email = dto.Email.Trim().ToLower();
            student.DateOfBirth = dto.DateOfBirth;
            student.Gender = dto.Gender;
            student.PhoneNumber = dto.PhoneNumber?.Trim();
            student.Address = dto.Address?.Trim();
            student.Avatar = dto.Avatar;
            student.Major = dto.Major?.Trim();
            student.UserId = dto.UserId ?? student.UserId;
            student.IsActive = dto.IsActive;
            student.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            var response = new StudentResponse
            {
                Id = student.Id,
                StudentCode = student.StudentCode,
                FullName = student.FullName,
                Email = student.Email,
                DateOfBirth = student.DateOfBirth,
                Gender = student.Gender,
                PhoneNumber = student.PhoneNumber,
                Address = student.Address,
                Avatar = student.Avatar,
                Major = student.Major,
                UserId = student.UserId,
                IsActive = student.IsActive,
                CreatedAt = student.CreatedAt,
                UpdatedAt = student.UpdatedAt
            };

            return Ok(ApiResponse<StudentResponse>.SuccessResponse(response, "Cập nhật sinh viên thành công"));
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
            var student = await _context.Students.FindAsync(id);
            if (student == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy sinh viên"));
            }

            // Soft delete
            student.IsActive = false;
            student.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return Ok(ApiResponse.SuccessResponse("Xóa sinh viên thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpGet("by-class/{classId}")]
    [Authorize(Roles = "ADMIN,TEACHER")]
    public async Task<IActionResult> GetByClass(int classId)
    {
        try
        {
            var classRoom = await _context.ClassRooms.FindAsync(classId);
            if (classRoom == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy lớp học"));
            }

            var students = await _context.ClassEnrollments
                .AsNoTracking()
                .Where(e => e.ClassRoomId == classId && e.IsActive)
                .Include(e => e.Student)
                    .ThenInclude(s => s!.User)
                .Select(e => new StudentResponse
                {
                    Id = e.Student!.Id,
                    StudentCode = e.Student.StudentCode,
                    FullName = e.Student.FullName,
                    Email = e.Student.Email,
                    DateOfBirth = e.Student.DateOfBirth,
                    Gender = e.Student.Gender,
                    PhoneNumber = e.Student.PhoneNumber,
                    Address = e.Student.Address,
                    Avatar = e.Student.Avatar,
                    Major = e.Student.Major,
                    UserId = e.Student.UserId,
                    IsActive = e.Student.IsActive,
                    CreatedAt = e.Student.CreatedAt,
                    UpdatedAt = e.Student.UpdatedAt,
                    HasFaceData = e.Student.User != null && !string.IsNullOrEmpty(e.Student.User.FaceData)
                })
                .ToListAsync();

            return Ok(ApiResponse<List<StudentResponse>>.SuccessResponse(students, $"Danh sách sinh viên lớp {classRoom.ClassName}"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }
}
