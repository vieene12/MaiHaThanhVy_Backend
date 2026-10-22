namespace MaiHaThanhVy_Backend.Services;

public interface IEmailService
{
    Task SendOtpEmailAsync(string toEmail, string otpCode, string purpose);
    Task SendEmailAsync(string toEmail, string subject, string bodyHtml);
}
