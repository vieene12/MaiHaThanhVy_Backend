using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MaiHaThanhVy_Backend.Data;
using MaiHaThanhVy_Backend.DTOs.Common;
using MaiHaThanhVy_Backend.DTOs.Subjects;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class SubjectsController : ControllerBase
{
    private readonly AppDbContext _context;

    public SubjectsController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] string? keyword, [FromQuery] int page = 1, [FromQuery] int pageSize = 20)
    {
        try
        {
            if (page < 1) page = 1;
            if (pageSize < 1) pageSize = 20;

            var query = _context.Subjects
                .AsNoTracking()
                .Include(s => s.ClassRooms)
                .Where(s => s.IsActive);

            if (!string.IsNullOrWhiteSpace(keyword))
            {
                var kw = keyword.Trim().ToLower();
                query = query.Where(s =>
                    s.SubjectName.ToLower().Contains(kw) ||
                    s.SubjectCode.ToLower().Contains(kw));
            }

            var totalCount = await query.CountAsync();

            var subjects = await query
                .OrderBy(s => s.SubjectCode)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .Select(s => new SubjectResponse
                {
                    Id = s.Id,
                    SubjectCode = s.SubjectCode,
                    SubjectName = s.SubjectName,
                    Credits = s.Credits,
                    Description = s.Description,
                    IsActive = s.IsActive,
                    CreatedAt = s.CreatedAt,
                    UpdatedAt = s.UpdatedAt,
                    TotalClassesCount = s.ClassRooms.Count(c => c.IsActive)
                })
                .ToListAsync();

            var result = new PagedResult<SubjectResponse>
            {
                Items = subjects,
                TotalCount = totalCount,
                PageNumber = page,
                PageSize = pageSize
            };

            return Ok(ApiResponse<PagedResult<SubjectResponse>>.SuccessResponse(result, "Lấy danh sách môn học thành công"));
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
            var subject = await _context.Subjects
                .AsNoTracking()
                .Include(s => s.ClassRooms)
                .FirstOrDefaultAsync(s => s.Id == id);

            if (subject == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy môn học"));
            }

            var response = new SubjectResponse
            {
                Id = subject.Id,
                SubjectCode = subject.SubjectCode,
                SubjectName = subject.SubjectName,
                Credits = subject.Credits,
                Description = subject.Description,
                IsActive = subject.IsActive,
                CreatedAt = subject.CreatedAt,
                UpdatedAt = subject.UpdatedAt,
                TotalClassesCount = subject.ClassRooms.Count(c => c.IsActive)
            };

            return Ok(ApiResponse<SubjectResponse>.SuccessResponse(response));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Create([FromBody] SubjectDto dto)
    {
        try
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
            }

            var existingCode = await _context.Subjects.AnyAsync(s => s.SubjectCode.ToLower() == dto.SubjectCode.ToLower());
            if (existingCode)
            {
                return BadRequest(ApiResponse.ErrorResponse("Mã môn học đã tồn tại"));
            }

            var subject = new Subject
            {
                SubjectCode = dto.SubjectCode.Trim().ToUpper(),
                SubjectName = dto.SubjectName.Trim(),
                Credits = dto.Credits,
                Description = dto.Description?.Trim(),
                IsActive = dto.IsActive,
                CreatedAt = DateTime.UtcNow
            };

            _context.Subjects.Add(subject);
            await _context.SaveChangesAsync();

            var response = new SubjectResponse
            {
                Id = subject.Id,
                SubjectCode = subject.SubjectCode,
                SubjectName = subject.SubjectName,
                Credits = subject.Credits,
                Description = subject.Description,
                IsActive = subject.IsActive,
                CreatedAt = subject.CreatedAt,
                TotalClassesCount = 0
            };

            return CreatedAtAction(nameof(GetById), new { id = subject.Id }, ApiResponse<SubjectResponse>.SuccessResponse(response, "Thêm môn học thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }

    [HttpPut("{id}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Update(int id, [FromBody] SubjectDto dto)
    {
        try
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ApiResponse.ErrorResponse("Dữ liệu không hợp lệ"));
            }

            var subject = await _context.Subjects.FindAsync(id);
            if (subject == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy môn học"));
            }

            var codeExists = await _context.Subjects.AnyAsync(s => s.Id != id && s.SubjectCode.ToLower() == dto.SubjectCode.ToLower());
            if (codeExists)
            {
                return BadRequest(ApiResponse.ErrorResponse("Mã môn học đã thuộc môn học khác"));
            }

            subject.SubjectCode = dto.SubjectCode.Trim().ToUpper();
            subject.SubjectName = dto.SubjectName.Trim();
            subject.Credits = dto.Credits;
            subject.Description = dto.Description?.Trim();
            subject.IsActive = dto.IsActive;
            subject.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            var response = new SubjectResponse
            {
                Id = subject.Id,
                SubjectCode = subject.SubjectCode,
                SubjectName = subject.SubjectName,
                Credits = subject.Credits,
                Description = subject.Description,
                IsActive = subject.IsActive,
                CreatedAt = subject.CreatedAt,
                UpdatedAt = subject.UpdatedAt
            };

            return Ok(ApiResponse<SubjectResponse>.SuccessResponse(response, "Cập nhật môn học thành công"));
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
            var subject = await _context.Subjects.FindAsync(id);
            if (subject == null)
            {
                return NotFound(ApiResponse.ErrorResponse("Không tìm thấy môn học"));
            }

            subject.IsActive = false;
            subject.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return Ok(ApiResponse.SuccessResponse("Xóa môn học thành công"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse.ErrorResponse("Lỗi máy chủ: " + ex.Message));
        }
    }
}
