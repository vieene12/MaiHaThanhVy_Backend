
using MailKit.Net.Smtp;
using MailKit.Security;
using MimeKit;

namespace MaiHaThanhVy_Backend.Services;

public class EmailService : IEmailService
{
	private readonly IConfiguration _configuration;
	private readonly ILogger<EmailService> _logger;

	public EmailService(
		IConfiguration configuration,
		ILogger<EmailService> logger)
	{
		_configuration = configuration;
		_logger = logger;
	}

	public async Task SendOtpEmailAsync(
		string toEmail,
		string otpCode,
		string purpose)
	{
		string subject = $"Mã xác thực OTP - {purpose}";

		string body = $@"
        <div style='font-family: Arial, sans-serif; padding: 20px;'>
            <h2>Hệ Thống Điểm Danh Sinh Viên</h2>
            <p>Xin chào,</p>
            <p>Bạn đã yêu cầu mã OTP để <strong>{purpose}</strong>.</p>
            <h1 style='color: #107c41; letter-spacing: 6px;'>
                {otpCode}
            </h1>
            <p>Mã xác thực có hiệu lực trong vòng 5 phút.</p>
            <p>Không chia sẻ mã này cho bất kỳ ai.</p>
        </div>";

		await SendEmailAsync(toEmail, subject, body);
	}

	public async Task SendEmailAsync(
		string toEmail,
		string subject,
		string bodyHtml)
	{
		try
		{
			var host = _configuration["EmailSettings:SmtpHost"]
				?? "smtp.gmail.com";

			var port = int.TryParse(
				_configuration["EmailSettings:SmtpPort"],
				out var p) ? p : 587;

			var senderEmail =
				_configuration["EmailSettings:SenderEmail"] ?? "";

			var senderPassword =
				_configuration["EmailSettings:SenderPassword"] ?? "";

			var senderName =
				_configuration["EmailSettings:SenderName"]
				?? "Student Attendance System";

			if (string.IsNullOrWhiteSpace(senderEmail))
			{
				throw new InvalidOperationException(
					"Chưa cấu hình SenderEmail.");
			}

			if (string.IsNullOrWhiteSpace(senderPassword)
				|| senderPassword == "your-gmail-app-password")
			{
				throw new InvalidOperationException(
					"Chưa cấu hình Gmail App Password.");
			}

			var message = new MimeMessage();

			message.From.Add(
				new MailboxAddress(senderName, senderEmail));

			message.To.Add(
				new MailboxAddress(toEmail, toEmail));

			message.Subject = subject;

			message.Body = new BodyBuilder
			{
				HtmlBody = bodyHtml
			}.ToMessageBody();

			using var client = new SmtpClient();

			await client.ConnectAsync(
				host,
				port,
				SecureSocketOptions.StartTls);

			await client.AuthenticateAsync(
				senderEmail,
				senderPassword);

			await client.SendAsync(message);

			await client.DisconnectAsync(true);

			_logger.LogInformation(
				"Email sent successfully to {ToEmail}",
				toEmail);
		}
		catch (Exception ex)
		{
			_logger.LogError(
				ex,
				"Failed to send email to {ToEmail}",
				toEmail);

			throw;
		}
	}
}
