using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Services;

public class JwtService : IJwtService
{
    private readonly IConfiguration _configuration;

    public JwtService(IConfiguration configuration)
    {
        _configuration = configuration;
    }

    public string GenerateToken(User user, int? studentId = null, int? teacherId = null)
    {
        var secret = _configuration["JwtSettings:Secret"] ?? "DefaultSuperSecretKey123456789012345678901234567890!";
        var issuer = _configuration["JwtSettings:Issuer"] ?? "StudentAttendanceApi";
        var audience = _configuration["JwtSettings:Audience"] ?? "StudentAttendanceApp";
        var expiryDays = int.TryParse(_configuration["JwtSettings:ExpiryDays"], out var days) ? days : 7;

        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var claims = new List<Claim>
        {
            new Claim("userId", user.Id.ToString()),
            new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
            new Claim("email", user.Email),
            new Claim(ClaimTypes.Email, user.Email),
            new Claim("role", user.Role),
            new Claim(ClaimTypes.Role, user.Role),
            new Claim("name", user.FullName),
            new Claim(ClaimTypes.Name, user.FullName)
        };

        if (studentId.HasValue)
        {
            claims.Add(new Claim("studentId", studentId.Value.ToString()));
        }

        if (teacherId.HasValue)
        {
            claims.Add(new Claim("teacherId", teacherId.Value.ToString()));
        }

        var token = new JwtSecurityToken(
            issuer: issuer,
            audience: audience,
            claims: claims,
            expires: DateTime.UtcNow.AddDays(expiryDays),
            signingCredentials: creds
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    public ClaimsPrincipal? ValidateToken(string token)
    {
        var secret = _configuration["JwtSettings:Secret"] ?? "DefaultSuperSecretKey123456789012345678901234567890!";
        var issuer = _configuration["JwtSettings:Issuer"] ?? "StudentAttendanceApi";
        var audience = _configuration["JwtSettings:Audience"] ?? "StudentAttendanceApp";

        var tokenHandler = new JwtSecurityTokenHandler();
        var key = Encoding.UTF8.GetBytes(secret);

        try
        {
            var principal = tokenHandler.ValidateToken(token, new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(key),
                ValidateIssuer = true,
                ValidIssuer = issuer,
                ValidateAudience = true,
                ValidAudience = audience,
                ValidateLifetime = true,
                ClockSkew = TimeSpan.Zero
            }, out SecurityToken validatedToken);

            return principal;
        }
        catch
        {
            return null;
        }
    }
}
