package com.ltw.studentattendance.api;

import com.ltw.studentattendance.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // === AUTH ===
    @POST("auth/register")
    Call<ApiResponse<LoginResponse>> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/verify-otp")
    Call<ApiResponse<LoginResponse>> verifyOtp(@Body OtpVerifyRequest request);

    @POST("auth/resend-otp")
    Call<ApiResponse<String>> resendOtp(@Body ResendOtpRequest request);

    @POST("auth/google-login")
    Call<ApiResponse<LoginResponse>> googleLogin(@Body GoogleLoginRequest request);

    @POST("auth/save-face")
    Call<ApiResponse<Boolean>> saveFace(@Body FaceLoginRequest request);

    @POST("auth/face-login")
    Call<ApiResponse<LoginResponse>> faceLogin(@Body FaceLoginRequest request);

    @GET("auth/profile")
    Call<ApiResponse<User>> getProfile();

    @PUT("auth/profile")
    Call<ApiResponse<User>> updateProfile(@Body User user);

    // === STUDENTS ===
    @GET("students")
    Call<ApiResponse<List<Student>>> getStudents(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("search") String search
    );

    @GET("students/{id}")
    Call<ApiResponse<Student>> getStudentById(@Path("id") int id);

    @POST("students")
    Call<ApiResponse<Student>> createStudent(@Body Student student);

    @PUT("students/{id}")
    Call<ApiResponse<Student>> updateStudent(@Path("id") int id, @Body Student student);

    @DELETE("students/{id}")
    Call<ApiResponse<Boolean>> deleteStudent(@Path("id") int id);

    @GET("students/class/{classId}")
    Call<ApiResponse<List<Student>>> getStudentsByClass(@Path("classId") int classId);

    // === TEACHERS ===
    @GET("teachers")
    Call<ApiResponse<List<Teacher>>> getTeachers();

    @GET("teachers/{id}")
    Call<ApiResponse<Teacher>> getTeacherById(@Path("id") int id);

    @POST("teachers")
    Call<ApiResponse<Teacher>> createTeacher(@Body Teacher teacher);

    @PUT("teachers/{id}")
    Call<ApiResponse<Teacher>> updateTeacher(@Path("id") int id, @Body Teacher teacher);

    @DELETE("teachers/{id}")
    Call<ApiResponse<Boolean>> deleteTeacher(@Path("id") int id);

    // === SUBJECTS ===
    @GET("subjects")
    Call<ApiResponse<List<Subject>>> getSubjects();

    @POST("subjects")
    Call<ApiResponse<Subject>> createSubject(@Body Subject subject);

    @PUT("subjects/{id}")
    Call<ApiResponse<Subject>> updateSubject(@Path("id") int id, @Body Subject subject);

    @DELETE("subjects/{id}")
    Call<ApiResponse<Boolean>> deleteSubject(@Path("id") int id);

    // === CLASS ROOMS ===
    @GET("classrooms")
    Call<ApiResponse<List<ClassRoom>>> getClassRooms();

    @GET("classrooms/{id}")
    Call<ApiResponse<ClassRoom>> getClassRoomById(@Path("id") int id);

    @POST("classrooms")
    Call<ApiResponse<ClassRoom>> createClassRoom(@Body ClassRoom classRoom);

    @PUT("classrooms/{id}")
    Call<ApiResponse<ClassRoom>> updateClassRoom(@Path("id") int id, @Body ClassRoom classRoom);

    @DELETE("classrooms/{id}")
    Call<ApiResponse<Boolean>> deleteClassRoom(@Path("id") int id);

    @POST("classrooms/{id}/enroll")
    Call<ApiResponse<Boolean>> enrollStudent(@Path("id") int classId, @Query("studentId") int studentId);

    @DELETE("classrooms/{id}/remove-student")
    Call<ApiResponse<Boolean>> removeStudent(@Path("id") int classId, @Query("studentId") int studentId);

    @GET("classrooms/teacher/{teacherId}")
    Call<ApiResponse<List<ClassRoom>>> getClassRoomsByTeacher(@Path("teacherId") int teacherId);

    @GET("classrooms/student/{studentId}")
    Call<ApiResponse<List<ClassRoom>>> getClassRoomsByStudent(@Path("studentId") int studentId);

    // === ATTENDANCE ===
    @POST("attendance/sessions")
    Call<ApiResponse<AttendanceSession>> createSession(@Body AttendanceSession session);

    @GET("attendance/sessions/class/{classId}")
    Call<ApiResponse<List<AttendanceSession>>> getSessionsByClass(@Path("classId") int classId);

    @GET("attendance/sessions/active/{classId}")
    Call<ApiResponse<AttendanceSession>> getActiveSession(@Path("classId") int classId);

    @POST("attendance/checkin")
    Call<ApiResponse<AttendanceRecord>> checkIn(@Body CheckInRequest request);

    @POST("attendance/qr-checkin")
    Call<ApiResponse<AttendanceRecord>> qrCheckIn(@Body CheckInRequest request);

    @POST("attendance/wifi-checkin")
    Call<ApiResponse<AttendanceRecord>> wifiCheckIn(@Body CheckInRequest request);

    @GET("attendance/student/{studentId}")
    Call<ApiResponse<List<AttendanceRecord>>> getStudentAttendance(
            @Path("studentId") int studentId,
            @Query("classId") Integer classId
    );

    @GET("attendance/session/{sessionId}/records")
    Call<ApiResponse<List<AttendanceRecord>>> getSessionRecords(@Path("sessionId") int sessionId);

    @PUT("attendance/records/{id}")
    Call<ApiResponse<AttendanceRecord>> updateRecord(@Path("id") int id, @Body AttendanceRecord record);

    @GET("attendance/report/{classId}")
    Call<ApiResponse<ReportSummary>> getReport(@Path("classId") int classId);

    // === DASHBOARD ===
    @GET("dashboard/admin")
    Call<ApiResponse<DashboardStats>> getAdminDashboard();

    @GET("dashboard/teacher/{teacherId}")
    Call<ApiResponse<DashboardStats>> getTeacherDashboard(@Path("teacherId") int teacherId);

    @GET("dashboard/student/{studentId}")
    Call<ApiResponse<DashboardStats>> getStudentDashboard(@Path("studentId") int studentId);
}
