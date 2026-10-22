using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Auth;

public class RegisterDto
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    [MinLength(6, ErrorMessage = "Mật khẩu phải có ít nhất 6 ký tự")]
    public string Password { get; set; } = string.Empty;

    [Required]
    [MaxLength(100)]
    public string FullName { get; set; } = string.Empty;

    [Required]
    public string Role { get; set; } = "STUDENT"; // ADMIN, TEACHER, STUDENT

    [MaxLength(20)]
    public string? PhoneNumber { get; set; }
}
