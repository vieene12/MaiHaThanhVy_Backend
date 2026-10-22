using System.ComponentModel.DataAnnotations;

namespace MaiHaThanhVy_Backend.DTOs.Auth;

public class GoogleAuthDto
{
    [Required]
    public string IdToken { get; set; } = string.Empty;
}
