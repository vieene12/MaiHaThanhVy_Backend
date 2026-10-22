using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Auth;

public class OtpVerifyDto
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    [StringLength(10, MinimumLength = 4)]
    public string Code { get; set; } = string.Empty;

    [Required]
    public string Type { get; set; } = "LOGIN"; // LOGIN, REGISTER, RESET_PASSWORD
}
