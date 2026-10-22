using System.Security.Claims;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Services;

public interface IJwtService
{
    string GenerateToken(User user, int? studentId = null, int? teacherId = null);
    ClaimsPrincipal? ValidateToken(string token);
}
