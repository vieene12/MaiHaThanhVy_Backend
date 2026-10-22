using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Auth;

public class SaveFaceDto
{
    [Required]
    public string FaceData { get; set; } = string.Empty; // JSON vector or Base64 feature vector
}

public class FaceLoginDto
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    public string FaceData { get; set; } = string.Empty;
}

public class UpdateProfileDto
{
    public string? FullName { get; set; }
    public string? PhoneNumber { get; set; }
    public string? Avatar { get; set; }
    public string? CurrentPassword { get; set; }
    public string? NewPassword { get; set; }
}
