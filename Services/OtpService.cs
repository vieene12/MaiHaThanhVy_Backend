using System.Security.Cryptography;
using Microsoft.EntityFrameworkCore;
using MaiHaThanhVy_Backend.Data;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Services;

public class OtpService : IOtpService
{
    private readonly AppDbContext _context;

    public OtpService(AppDbContext context)
    {
        _context = context;
    }

    public async Task<string> GenerateAndSaveOtpAsync(string email, string type)
    {
        // Invalidate any previous unused OTPs for this email and type
        var existingOtps = await _context.OtpCodes
            .Where(o => o.Email == email && o.Type == type && !o.IsUsed)
            .ToListAsync();

        foreach (var oldOtp in existingOtps)
        {
            oldOtp.IsUsed = true;
        }

        // Generate secure 6-digit OTP
        var randomBytes = new byte[4];
        using (var rng = RandomNumberGenerator.Create())
        {
            rng.GetBytes(randomBytes);
        }
        int randomNumber = BitConverter.ToInt32(randomBytes, 0) & 0x7FFFFFFF;
        string otpCode = (randomNumber % 900000 + 100000).ToString();

        var otp = new OtpCode
        {
            Email = email,
            Code = otpCode,
            Type = type,
            ExpiresAt = DateTime.UtcNow.AddMinutes(5), // Expires in 5 minutes
            IsUsed = false,
            CreatedAt = DateTime.UtcNow
        };

        _context.OtpCodes.Add(otp);
        await _context.SaveChangesAsync();

        return otpCode;
    }

    public async Task<bool> ValidateOtpAsync(string email, string code, string type)
    {
        var now = DateTime.UtcNow;

        var otpRecord = await _context.OtpCodes
            .Where(o => o.Email == email && o.Code == code && o.Type == type && !o.IsUsed && o.ExpiresAt > now)
            .OrderByDescending(o => o.CreatedAt)
            .FirstOrDefaultAsync();

        if (otpRecord == null)
        {
            return false;
        }

        otpRecord.IsUsed = true;
        await _context.SaveChangesAsync();
        return true;
    }
}
