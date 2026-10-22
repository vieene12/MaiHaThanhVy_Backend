using BCrypt.Net;
using Google.Apis.Auth;
using Microsoft.EntityFrameworkCore;
using MaiHaThanhVy_Backend.Data;
using MaiHaThanhVy_Backend.DTOs.Auth;
using MaiHaThanhVy_Backend.DTOs.Common;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Services;

public class AuthService : IAuthService
{
    private readonly AppDbContext _context;
    private readonly IJwtService _jwtService;
    private readonly IOtpService _otpService;
    private readonly IEmailService _emailService;
    private readonly IConfiguration _configuration;
    private readonly ILogger<AuthService> _logger;

    public AuthService(
        AppDbContext context,
        IJwtService jwtService,
        IOtpService otpService,
        IEmailService emailService,
        IConfiguration configuration,
        ILogger<AuthService> logger)
    {
        _context = context;
        _jwtService = jwtService;
        _otpService = otpService;
        _emailService = emailService;
        _configuration = configuration;
        _logger = logger;
    }

    public async Task<ApiResponse<LoginResponse>> RegisterAsync(RegisterDto dto)
    {
        var normalizedEmail = dto.Email.Trim().ToLower();

        var existingUser = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == normalizedEmail);
        if (existingUser != null)
        {
            return ApiResponse<LoginResponse>.ErrorResponse("Email này đã được sử dụng");
        }

        var role = dto.Role.ToUpper();
        if (role != "ADMIN" && role != "TEACHER" && role != "STUDENT")
        {
            role = "STUDENT";
        }

        var user = new User
        {
            Email = normalizedEmail,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
            FullName = dto.FullName.Trim(),
            Role = role,
            PhoneNumber = dto.PhoneNumber?.Trim(),
            IsEmailVerified = false,
            IsActive = true,
            CreatedAt = DateTime.UtcNow
        };

        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        // Tạo hồ sơ liên kết theo vai trò
        if (role == "STUDENT")
        {
            var student = new Student
            {
                StudentCode = "SV" + user.Id.ToString("D5"),
                FullName = user.FullName,
                Email = user.Email,
                PhoneNumber = user.PhoneNumber,
                UserId = user.Id,
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            };
            _context.Students.Add(student);
            await _context.SaveChangesAsync();
        }
        else if (role == "TEACHER")
        {
            var teacher = new Teacher
            {
                TeacherCode = "GV" + user.Id.ToString("D4"),
                FullName = user.FullName,
                Email = user.Email,
                UserId = user.Id,
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            };
            _context.Teachers.Add(teacher);
            await _context.SaveChangesAsync();
        }

        // Gửi OTP xác thực email
        var otpCode = await _otpService.GenerateAndSaveOtpAsync(user.Email, "REGISTER");
        await _emailService.SendOtpEmailAsync(user.Email, otpCode, "Xác thực đăng ký tài khoản");

        var response = new LoginResponse
        {
            RequiresOtp = true,
            OtpType = "REGISTER",
            Role = user.Role,
            User = new UserInfoDto
            {
                Id = user.Id,
                Email = user.Email,
                FullName = user.FullName,
                Role = user.Role,
                PhoneNumber = user.PhoneNumber,
                IsEmailVerified = user.IsEmailVerified
            }
        };

        return ApiResponse<LoginResponse>.SuccessResponse(response, "Đăng ký thành công. Mã OTP đã được gửi về email của bạn để kích hoạt tài khoản.");
    }

    public async Task<ApiResponse<LoginResponse>> LoginAsync(LoginDto dto)
    {
        var normalizedEmail = dto.Email.Trim().ToLower();

        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == normalizedEmail);
        if (user == null || !user.IsActive)
        {
            return ApiResponse<LoginResponse>.ErrorResponse("Email hoặc mật khẩu không chính xác");
        }

        if (string.IsNullOrEmpty(user.PasswordHash) || !BCrypt.Net.BCrypt.Verify(dto.Password, user.PasswordHash))
        {
            return ApiResponse<LoginResponse>.ErrorResponse("Email hoặc mật khẩu không chính xác");
        }

        var student = await _context.Students.FirstOrDefaultAsync(s => s.UserId == user.Id || s.Email.ToLower() == normalizedEmail);
        var teacher = await _context.Teachers.FirstOrDefaultAsync(t => t.UserId == user.Id || t.Email.ToLower() == normalizedEmail);

        // Gửi OTP xác thực 2 bước
        var otpCode = await _otpService.GenerateAndSaveOtpAsync(user.Email, "LOGIN");
        await _emailService.SendOtpEmailAsync(user.Email, otpCode, "Đăng nhập hệ thống");

        var response = new LoginResponse
        {
            RequiresOtp = true,
            OtpType = "LOGIN",
            Role = user.Role,
            User = new UserInfoDto
            {
                Id = user.Id,
                Email = user.Email,
                FullName = user.FullName,
                Role = user.Role,
                PhoneNumber = user.PhoneNumber,
                Avatar = user.Avatar,
                IsEmailVerified = user.IsEmailVerified,
                HasFaceData = !string.IsNullOrEmpty(user.FaceData),
                StudentId = student?.Id,
                TeacherId = teacher?.Id
            }
        };

        return ApiResponse<LoginResponse>.SuccessResponse(response, "Mật khẩu chính xác. Vui lòng nhập mã OTP đã gửi đến email của bạn để hoàn tất đăng nhập.");
    }

    public async Task<ApiResponse<LoginResponse>> VerifyOtpAsync(OtpVerifyDto dto)
    {
        var normalizedEmail = dto.Email.Trim().ToLower();

        var isValidOtp = await _otpService.ValidateOtpAsync(normalizedEmail, dto.Code.Trim(), dto.Type);
        if (!isValidOtp)
        {
            return ApiResponse<LoginResponse>.ErrorResponse("Mã OTP không chính xác hoặc đã hết hạn (5 phút)");
        }

        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == normalizedEmail);
        if (user == null || !user.IsActive)
        {
            return ApiResponse<LoginResponse>.ErrorResponse("Tài khoản không tồn tại hoặc đã bị khóa");
        }

        if (!user.IsEmailVerified)
        {
            user.IsEmailVerified = true;
            user.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();
        }

        var student = await _context.Students.FirstOrDefaultAsync(s => s.UserId == user.Id || s.Email.ToLower() == normalizedEmail);
        if (student != null && student.UserId == null)
        {
            student.UserId = user.Id;
            await _context.SaveChangesAsync();
        }

        var teacher = await _context.Teachers.FirstOrDefaultAsync(t => t.UserId == user.Id || t.Email.ToLower() == normalizedEmail);
        if (teacher != null && teacher.UserId == null)
        {
            teacher.UserId = user.Id;
            await _context.SaveChangesAsync();
        }

        var token = _jwtService.GenerateToken(user, student?.Id, teacher?.Id);

        var response = new LoginResponse
        {
            Token = token,
            Role = user.Role,
            RequiresOtp = false,
            User = new UserInfoDto
            {
                Id = user.Id,
                Email = user.Email,
                FullName = user.FullName,
                Role = user.Role,
                PhoneNumber = user.PhoneNumber,
                Avatar = user.Avatar,
                IsEmailVerified = user.IsEmailVerified,
                HasFaceData = !string.IsNullOrEmpty(user.FaceData),
                StudentId = student?.Id,
                TeacherId = teacher?.Id
            }
        };

        return ApiResponse<LoginResponse>.SuccessResponse(response, "Xác thực OTP thành công. Đăng nhập thành công!");
    }

    public async Task<ApiResponse<string>> ResendOtpAsync(ResendOtpDto dto)
    {
        var normalizedEmail = dto.Email.Trim().ToLower();

        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == normalizedEmail);
        if (user == null || !user.IsActive)
        {
            return ApiResponse<string>.ErrorResponse("Tài khoản không tồn tại");
        }

        var purpose = dto.Type == "REGISTER" ? "Xác thực đăng ký tài khoản" : "Đăng nhập hệ thống";
        var otpCode = await _otpService.GenerateAndSaveOtpAsync(user.Email, dto.Type);
        await _emailService.SendOtpEmailAsync(user.Email, otpCode, purpose);

        return ApiResponse<string>.SuccessResponse(user.Email, "Mã OTP mới đã được gửi đến email của bạn.");
    }

    public async Task<ApiResponse<LoginResponse>> GoogleLoginAsync(GoogleAuthDto dto)
    {
        try
        {
            // Kiểm tra cấu hình Google Client ID
            var googleClientId = _configuration["GoogleAuth:ClientId"];

            if (string.IsNullOrEmpty(googleClientId) || googleClientId.Contains("your-google-client-id"))
            {
                _logger.LogError("Google OAuth chưa được cấu hình. Hãy thiết lập GoogleAuth:ClientId trong appsettings.json");
                return ApiResponse<LoginResponse>.ErrorResponse(
                    "Hệ thống chưa cấu hình Google OAuth. Vui lòng liên hệ admin để cấu hình Google Client ID.");
            }

            // Xác thực Google ID Token với Client ID thực
            var settings = new GoogleJsonWebSignature.ValidationSettings
            {
                Audience = new[] { googleClientId }
            };

            GoogleJsonWebSignature.Payload payload;
            try
            {
                payload = await GoogleJsonWebSignature.ValidateAsync(dto.IdToken, settings);
            }
            catch (InvalidJwtException jwtEx)
            {
                _logger.LogWarning(jwtEx, "Google ID token không hợp lệ hoặc đã hết hạn");
                return ApiResponse<LoginResponse>.ErrorResponse(
                    "Google token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại bằng Google.");
            }

            if (payload == null || string.IsNullOrEmpty(payload.Email))
            {
                return ApiResponse<LoginResponse>.ErrorResponse("Không lấy được thông tin từ tài khoản Google");
            }

            var normalizedEmail = payload.Email.ToLower();

            // Tìm user theo email hoặc Google ID
            var user = await _context.Users.FirstOrDefaultAsync(
                u => u.Email.ToLower() == normalizedEmail || u.GoogleId == payload.Subject);

            if (user == null)
            {
                // Đăng ký tài khoản mới từ Google
                user = new User
                {
                    Email = normalizedEmail,
                    FullName = payload.Name ?? payload.Email,
                    GoogleId = payload.Subject,
                    Avatar = payload.Picture,
                    Role = "STUDENT",
                    IsEmailVerified = true,
                    IsActive = true,
                    CreatedAt = DateTime.UtcNow
                };

                _context.Users.Add(user);
                await _context.SaveChangesAsync();

                // Tạo hồ sơ sinh viên tự động
                var newStudent = new Student
                {
                    StudentCode = "SV" + user.Id.ToString("D5"),
                    FullName = user.FullName,
                    Email = user.Email,
                    Avatar = user.Avatar,
                    UserId = user.Id,
                    IsActive = true,
                    CreatedAt = DateTime.UtcNow
                };
                _context.Students.Add(newStudent);
                await _context.SaveChangesAsync();

                _logger.LogInformation("Tạo tài khoản mới từ Google: {Email}", normalizedEmail);
            }
            else
            {
                // Kiểm tra tài khoản có bị khóa không
                if (!user.IsActive)
                {
                    return ApiResponse<LoginResponse>.ErrorResponse("Tài khoản đã bị khóa. Vui lòng liên hệ admin.");
                }

                // Cập nhật thông tin Google
                bool hasChanges = false;
                if (string.IsNullOrEmpty(user.GoogleId))
                {
                    user.GoogleId = payload.Subject;
                    hasChanges = true;
                }
                if (string.IsNullOrEmpty(user.Avatar) && !string.IsNullOrEmpty(payload.Picture))
                {
                    user.Avatar = payload.Picture;
                    hasChanges = true;
                }
                if (!user.IsEmailVerified)
                {
                    user.IsEmailVerified = true;
                    hasChanges = true;
                }
                if (hasChanges)
                {
                    user.UpdatedAt = DateTime.UtcNow;
                    await _context.SaveChangesAsync();
                }
            }

            // Tìm thông tin sinh viên / giáo viên liên kết
            var linkedStudent = await _context.Students.FirstOrDefaultAsync(
                s => s.UserId == user.Id || s.Email.ToLower() == normalizedEmail);
            var linkedTeacher = await _context.Teachers.FirstOrDefaultAsync(
                t => t.UserId == user.Id || t.Email.ToLower() == normalizedEmail);

            // Tạo JWT token
            var token = _jwtService.GenerateToken(user, linkedStudent?.Id, linkedTeacher?.Id);

            var response = new LoginResponse
            {
                Token = token,
                Role = user.Role,
                RequiresOtp = false,
                User = new UserInfoDto
                {
                    Id = user.Id,
                    Email = user.Email,
                    FullName = user.FullName,
                    Role = user.Role,
                    PhoneNumber = user.PhoneNumber,
                    Avatar = user.Avatar,
                    IsEmailVerified = user.IsEmailVerified,
                    HasFaceData = !string.IsNullOrEmpty(user.FaceData),
                    StudentId = linkedStudent?.Id,
                    TeacherId = linkedTeacher?.Id
                }
            };

            return ApiResponse<LoginResponse>.SuccessResponse(response, "Đăng nhập Google thành công!");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Lỗi không xác định khi xác thực Google token");
            return ApiResponse<LoginResponse>.ErrorResponse("Xác thực Google thất bại. Vui lòng thử lại sau.");
        }
    }

    public async Task<ApiResponse<bool>> SaveFaceDataAsync(int userId, SaveFaceDto dto)
    {
        var user = await _context.Users.FindAsync(userId);
        if (user == null || !user.IsActive)
        {
            return ApiResponse<bool>.ErrorResponse("Người dùng không tồn tại");
        }

        user.FaceData = dto.FaceData;
        user.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();
        return ApiResponse<bool>.SuccessResponse(true, "Lưu dữ liệu khuôn mặt thành công");
    }

    public async Task<ApiResponse<LoginResponse>> FaceLoginAsync(FaceLoginDto dto)
    {
        var normalizedEmail = dto.Email.Trim().ToLower();

        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == normalizedEmail && u.IsActive);
        if (user == null)
        {
            return ApiResponse<LoginResponse>.ErrorResponse("Tài khoản không tồn tại");
        }

        if (string.IsNullOrEmpty(user.FaceData))
        {
			return ApiResponse<LoginResponse>.ErrorResponse(
				 "Đăng nhập khuôn mặt chưa được cấu hình an toàn.");
		}

        var student = await _context.Students.FirstOrDefaultAsync(s => s.UserId == user.Id || s.Email.ToLower() == normalizedEmail);
        var teacher = await _context.Teachers.FirstOrDefaultAsync(t => t.UserId == user.Id || t.Email.ToLower() == normalizedEmail);

        var token = _jwtService.GenerateToken(user, student?.Id, teacher?.Id);

        var response = new LoginResponse
        {
            Token = token,
            Role = user.Role,
            RequiresOtp = false,
            User = new UserInfoDto
            {
                Id = user.Id,
                Email = user.Email,
                FullName = user.FullName,
                Role = user.Role,
                PhoneNumber = user.PhoneNumber,
                Avatar = user.Avatar,
                IsEmailVerified = user.IsEmailVerified,
                HasFaceData = true,
                StudentId = student?.Id,
                TeacherId = teacher?.Id
            }
        };

        return ApiResponse<LoginResponse>.SuccessResponse(response, "Đăng nhập bằng khuôn mặt thành công!");
    }

    public async Task<ApiResponse<UserInfoDto>> GetProfileAsync(int userId)
    {
        var user = await _context.Users.FindAsync(userId);
        if (user == null || !user.IsActive)
        {
            return ApiResponse<UserInfoDto>.ErrorResponse("Người dùng không tồn tại");
        }

        var student = await _context.Students.FirstOrDefaultAsync(s => s.UserId == user.Id || s.Email.ToLower() == user.Email.ToLower());
        var teacher = await _context.Teachers.FirstOrDefaultAsync(t => t.UserId == user.Id || t.Email.ToLower() == user.Email.ToLower());

        var profile = new UserInfoDto
        {
            Id = user.Id,
            Email = user.Email,
            FullName = user.FullName,
            Role = user.Role,
            PhoneNumber = user.PhoneNumber,
            Avatar = user.Avatar,
            IsEmailVerified = user.IsEmailVerified,
            HasFaceData = !string.IsNullOrEmpty(user.FaceData),
            StudentId = student?.Id,
            TeacherId = teacher?.Id
        };

        return ApiResponse<UserInfoDto>.SuccessResponse(profile, "Lấy thông tin người dùng thành công");
    }

    public async Task<ApiResponse<UserInfoDto>> UpdateProfileAsync(int userId, UpdateProfileDto dto)
    {
        var user = await _context.Users.FindAsync(userId);
        if (user == null || !user.IsActive)
        {
            return ApiResponse<UserInfoDto>.ErrorResponse("Người dùng không tồn tại");
        }

        if (!string.IsNullOrWhiteSpace(dto.FullName))
        {
            user.FullName = dto.FullName.Trim();
        }

        if (dto.PhoneNumber != null)
        {
            user.PhoneNumber = dto.PhoneNumber.Trim();
        }

        if (!string.IsNullOrWhiteSpace(dto.Avatar))
        {
            user.Avatar = dto.Avatar.Trim();
        }

        if (!string.IsNullOrWhiteSpace(dto.NewPassword))
        {
            if (string.IsNullOrEmpty(dto.CurrentPassword) ||
                string.IsNullOrEmpty(user.PasswordHash) ||
                !BCrypt.Net.BCrypt.Verify(dto.CurrentPassword, user.PasswordHash))
            {
                return ApiResponse<UserInfoDto>.ErrorResponse("Mật khẩu hiện tại không chính xác");
            }

            if (dto.NewPassword.Length < 6)
            {
                return ApiResponse<UserInfoDto>.ErrorResponse("Mật khẩu mới phải có ít nhất 6 ký tự");
            }

            user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);
        }

        user.UpdatedAt = DateTime.UtcNow;

        // Cập nhật thông tin sinh viên / giáo viên liên kết
        var student = await _context.Students.FirstOrDefaultAsync(s => s.UserId == user.Id || s.Email.ToLower() == user.Email.ToLower());
        if (student != null)
        {
            student.FullName = user.FullName;
            student.PhoneNumber = user.PhoneNumber;
            student.Avatar = user.Avatar;
            student.UpdatedAt = DateTime.UtcNow;
        }

        var teacher = await _context.Teachers.FirstOrDefaultAsync(t => t.UserId == user.Id || t.Email.ToLower() == user.Email.ToLower());
        if (teacher != null)
        {
            teacher.FullName = user.FullName;
            teacher.UpdatedAt = DateTime.UtcNow;
        }

        await _context.SaveChangesAsync();

        var profile = new UserInfoDto
        {
            Id = user.Id,
            Email = user.Email,
            FullName = user.FullName,
            Role = user.Role,
            PhoneNumber = user.PhoneNumber,
            Avatar = user.Avatar,
            IsEmailVerified = user.IsEmailVerified,
            HasFaceData = !string.IsNullOrEmpty(user.FaceData),
            StudentId = student?.Id,
            TeacherId = teacher?.Id
        };

        return ApiResponse<UserInfoDto>.SuccessResponse(profile, "Cập nhật thông tin thành công");
    }
}
