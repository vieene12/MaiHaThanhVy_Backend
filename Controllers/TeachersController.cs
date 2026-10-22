using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MaiHaThanhVy_Backend.Data;
using MaiHaThanhVy_Backend.DTOs.Common;
using MaiHaThanhVy_Backend.DTOs.Teachers;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class TeachersController : ControllerBase
{
    private readonly AppDbContext _context;

    public TeachersController(AppDbContext context)
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

            var query = _context.Teachers
                .AsNoTracking()
                .Include(t => t.ClassRooms)
                .Where(t => t.IsActive);

            if (!string.IsNullOrWhiteSpace(keyword))
            {
                var kw = keyword.Trim().ToLower();
                query = query.Where(t =>
                    t.FullName.ToLower().Contains(kw) ||
                    t.TeacherCode.ToLower().Contains(kw) ||
                    t.Email.ToLower().Contains(kw) ||
                    (t.Department != null && t.Department.ToLower().Contains(kw)));
            }

            var totalCount = await query.CountAsync();

            var teachers = await query
                .OrderByDescending(t => t.CreatedAt)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .Select(t => new TeacherResponse
                {
                    Id = t.Id,
                    TeacherCode = t.TeacherCode,
                    FullName = t.FullName,
                    Email = t.Email,
                    Department = t.Department,
                    UserId = t.UserId,
                    IsActive = t.IsActive,
                    CreatedAt = t.CreatedAt,
                    UpdatedAt = t.UpdatedAt,
                    AssignedClassesCount = t.ClassRooms.Count(c => c.IsActive)
                })
                .ToListAsync();

            var result = new PagedResult<TeacherResponse>
            {
                Items = teachers,
                TotalCount = totalCount,
                PageNumber = page,
                PageSize = pageSize
            };

            return Ok(ApiResponse<PagedResult<TeacherResponse>>.SuccessResponse(result, "Lấy danh sách giảng viên thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpGet("{id}")]
    [Authorize(Roles = "ADMIN,TEACHER")]
    public async Task<IActionResult> GetById(int id)
    {
        try
        {
            var teacher = await _context.Teachers
                .AsNoTracking()
                .Include(t => t.ClassRooms)
                .FirstOrDefaultAsync(t => t.Id == id);

            if (teacher == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy giảng viên"));
            }

            var response = new TeacherResponse
            {
                Id = teacher.Id,
                TeacherCode = teacher.TeacherCode,
                FullName = teacher.FullName,
                Email = teacher.Email,
                Department = teacher.Department,
                UserId = teacher.UserId,
                IsActive = teacher.IsActive,
                CreatedAt = teacher.CreatedAt,
                UpdatedAt = teacher.UpdatedAt,
                AssignedClassesCount = teacher.ClassRooms.Count(c => c.IsActive)
            };

            return Ok(ApiResponse<TeacherResponse>.SuccessResponse(response));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Create([FromBody] TeacherDto dto)
    {
        try
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
            }

            var existingCode = await _context.Teachers.AnyAsync(t => t.TeacherCode.ToLower() == dto.TeacherCode.ToLower());
            if (existingCode)
            {
                return BadRequest(ApiResponse.ErrorResponse("Mã giảng viên đã tồn tại"));
            }

            var existingEmail = await _context.Teachers.AnyAsync(t => t.Email.ToLower() == dto.Email.ToLower());
            if (existingEmail)
            {
                return BadRequest(ApiResponse.ErrorResponse("Email giảng viên đã tồn tại"));
            }

            int? userId = dto.UserId;
            if (userId == null)
            {
                var matchedUser = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == dto.Email.ToLower());
                if (matchedUser != null)
                {
                    userId = matchedUser.Id;
                }
            }

            var teacher = new Teacher
            {
                TeacherCode = dto.TeacherCode.Trim().ToUpper(),
                FullName = dto.FullName.Trim(),
                Email = dto.Email.Trim().ToLower(),
                Department = dto.Department?.Trim(),
                UserId = userId,
                IsActive = dto.IsActive,
                CreatedAt = DateTime.UtcNow
            };

            _context.Teachers.Add(teacher);
            await _context.SaveChangesAsync();

            var response = new TeacherResponse
            {
                Id = teacher.Id,
                TeacherCode = teacher.TeacherCode,
                FullName = teacher.FullName,
                Email = teacher.Email,
                Department = teacher.Department,
                UserId = teacher.UserId,
                IsActive = teacher.IsActive,
                CreatedAt = teacher.CreatedAt,
                AssignedClassesCount = 0
            };

            return CreatedAtAction(nameof(GetById), new { id = teacher.Id }, ApiResponse<TeacherResponse>.SuccessResponse(response, "Thêm giảng viên thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPut("{id}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Update(int id, [FromBody] TeacherDto dto)
    {
        try
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
            }

            var teacher = await _context.Teachers.FindAsync(id);
            if (teacher == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy giảng viên"));
            }

            var codeExists = await _context.Teachers.AnyAsync(t => t.Id != id && t.TeacherCode.ToLower() == dto.TeacherCode.ToLower());
            if (codeExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Mã giảng viên đã thuộc về người khác"));
            }

            var emailExists = await _context.Teachers.AnyAsync(t => t.Id != id && t.Email.ToLower() == dto.Email.ToLower());
            if (emailExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Email đã thuộc về người khác"));
            }

            teacher.TeacherCode = dto.TeacherCode.Trim().ToUpper();
            teacher.FullName = dto.FullName.Trim();
            teacher.Email = dto.Email.Trim().ToLower();
            teacher.Department = dto.Department?.Trim();
            teacher.UserId = dto.UserId ?? teacher.UserId;
            teacher.IsActive = dto.IsActive;
            teacher.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            var response = new TeacherResponse
            {
                Id = teacher.Id,
                TeacherCode = teacher.TeacherCode,
                FullName = teacher.FullName,
                Email = teacher.Email,
                Department = teacher.Department,
                UserId = teacher.UserId,
                IsActive = teacher.IsActive,
                CreatedAt = teacher.CreatedAt,
                UpdatedAt = teacher.UpdatedAt
            };

            return Ok(ApiResponse<TeacherResponse>.SuccessResponse(response, "Cập nhật giảng viên thành công"));
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
            var teacher = await _context.Teachers.FindAsync(id);
            if (teacher == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy giảng viên"));
            }

            teacher.IsActive = false;
            teacher.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return Ok(ApiResponse.SuccessResponse("Xóa giảng viên thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }
}
