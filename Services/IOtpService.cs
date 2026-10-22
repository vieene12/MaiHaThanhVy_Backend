namespace MaiHaThanhVy_Backend.Services;

public interface IOtpService
{
    Task<string> GenerateAndSaveOtpAsync(string email, string type);
    Task<bool> ValidateOtpAsync(string email, string code, string type);
}
