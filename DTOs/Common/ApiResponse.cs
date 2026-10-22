namespace MaiHaThanhVy_Backend.DTOs.Common;

public class ApiResponse<T>
{
    public bool Success { get; set; }
    public string Message { get; set; } = string.Empty;
    public T? Data { get; set; }

    public static ApiResponse<T> SuccessResponse(T data, string message = "Success")
    {
        return new ApiResponse<T>
        {
            Success = true,
            Message = message,
            Data = data
        };
    }

    public static ApiResponse<T> ErrorResponse(string message, T? data = default)
    {
        return new ApiResponse<T>
        {
            Success = false,
            Message = message,
            Data = data
        };
    }
}

public class ApiResponse : ApiResponse<object>
{
    public static ApiResponse SuccessResponse(string message = "Success", object? data = null)
    {
        return new ApiResponse
        {
            Success = true,
            Message = message,
            Data = data
        };
    }

    public static new ApiResponse ErrorResponse(string message, object? data = null)
    {
        return new ApiResponse
        {
            Success = false,
            Message = message,
            Data = data
        };
    }
}

public class PagedResult<T>
{
    public List<T> Items { get; set; } = new List<T>();
    public int TotalCount { get; set; }
    public int PageNumber { get; set; }
    public int PageSize { get; set; }
    public int TotalPages => PageSize > 0 ? (int)Math.Ceiling((double)TotalCount / PageSize) : 0;
}
