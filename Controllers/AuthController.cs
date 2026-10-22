using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MaiHaThanhVy_Backend.DTOs.Auth;
using MaiHaThanhVy_Backend.DTOs.Common;
using MaiHaThanhVy_Backend.Services;

namespace MaiHaThanhVy_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    public AuthController(IAuthService authService)
    {
        _authService = authService;
    }

    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterDto dto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse.ErrorResponse("Dữ liệu gửi lên không hợp lệ"));
        }

        var result = await _authService.RegisterAsync(dto);
        if (!result.Success)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginDto dto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse.ErrorResponse("Dữ liệu gửi lên không hợp lệ"));
        }

        var result = await _authService.LoginAsync(dto);
        if (!result.Success)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    [HttpPost("verify-otp")]
    public async Task<IActionResult> VerifyOtp([FromBody] OtpVerifyDto dto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse.ErrorResponse("Dữ liệu gửi lên không hợp lệ"));
        }

        var result = await _authService.VerifyOtpAsync(dto);
        if (!result.Success)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    [HttpPost("google-login")]
    public async Task<IActionResult> GoogleLogin([FromBody] GoogleAuthDto dto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse.ErrorResponse("Google token không hợp lệ"));
        }

        var result = await _authService.GoogleLoginAsync(dto);
        if (!result.Success)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    [HttpPost("resend-otp")]
    public async Task<IActionResult> ResendOtp([FromBody] ResendOtpDto dto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse.ErrorResponse("Email không hợp lệ"));
        }

        var result = await _authService.ResendOtpAsync(dto);
        if (!result.Success)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    [Authorize]
    [HttpPost("save-face")]
    public async Task<IActionResult> SaveFace([FromBody] SaveFaceDto dto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse.ErrorResponse("Dữ liệu khuôn mặt không hợp lệ"));
        }

        var userId = GetCurrentUserId();
        if (userId == null)
        {
            return Unauthorized(ApiResponse.ErrorResponse("Không tìm thấy thông tin đăng nhập"));
        }

        var result = await _authService.SaveFaceDataAsync(userId.Value, dto);
        if (!result.Success)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    [HttpPost("face-login")]
    public async Task<IActionResult> FaceLogin([FromBody] FaceLoginDto dto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse.ErrorResponse("Dữ liệu đăng nhập không hợp lệ"));
        }

        var result = await _authService.FaceLoginAsync(dto);
        if (!result.Success)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    [Authorize]
    [HttpGet("profile")]
    public async Task<IActionResult> GetProfile()
    {
        var userId = GetCurrentUserId();
        if (userId == null)
        {
            return Unauthorized(ApiResponse.ErrorResponse("Không tìm thấy thông tin đăng nhập"));
        }

        var result = await _authService.GetProfileAsync(userId.Value);
        if (!result.Success)
        {
            return NotFound(result);
        }

        return Ok(result);
    }

    [Authorize]
    [HttpPut("profile")]
    public async Task<IActionResult> UpdateProfile([FromBody] UpdateProfileDto dto)
    {
        var userId = GetCurrentUserId();
        if (userId == null)
        {
            return Unauthorized(ApiResponse.ErrorResponse("Không tìm thấy thông tin đăng nhập"));
        }

        var result = await _authService.UpdateProfileAsync(userId.Value, dto);
        if (!result.Success)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    private int? GetCurrentUserId()
    {
        var claim = User.FindFirst("userId")?.Value ?? User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (int.TryParse(claim, out var userId))
        {
            return userId;
        }
        return null;
    }
}
