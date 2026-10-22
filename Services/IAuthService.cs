using MaiHaThanhVy_Backend.DTOs.Auth;
using MaiHaThanhVy_Backend.DTOs.Common;

namespace MaiHaThanhVy_Backend.Services;

public interface IAuthService
{
    Task<ApiResponse<LoginResponse>> RegisterAsync(RegisterDto dto);
    Task<ApiResponse<LoginResponse>> LoginAsync(LoginDto dto);
    Task<ApiResponse<LoginResponse>> VerifyOtpAsync(OtpVerifyDto dto);
    Task<ApiResponse<string>> ResendOtpAsync(ResendOtpDto dto);
    Task<ApiResponse<LoginResponse>> GoogleLoginAsync(GoogleAuthDto dto);
    Task<ApiResponse<bool>> SaveFaceDataAsync(int userId, SaveFaceDto dto);
    Task<ApiResponse<LoginResponse>> FaceLoginAsync(FaceLoginDto dto);
    Task<ApiResponse<UserInfoDto>> GetProfileAsync(int userId);
    Task<ApiResponse<UserInfoDto>> UpdateProfileAsync(int userId, UpdateProfileDto dto);
}
