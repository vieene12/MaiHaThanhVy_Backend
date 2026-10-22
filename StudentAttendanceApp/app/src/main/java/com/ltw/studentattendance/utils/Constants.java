package com.ltw.studentattendance.utils;

public class Constants {
    // =====================================================================
    // ĐỔI IP này thành IP WiFi máy tính của bạn khi test trên điện thoại thật
    // IP hiện tại: 10.12.77.124 (WiFi của máy tính)
    // =====================================================================
    public static final String BASE_URL = "http://10.12.77.124:5000/api/";

    // Google Web Client ID – lấy từ Google Cloud Console > Credentials > Web Client
    // Điền vào đây sau khi tạo OAuth credentials
    public static final String GOOGLE_WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";

    public static final String PREF_NAME = "StudentAttendancePrefs";
    public static final String KEY_TOKEN = "auth_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_STUDENT_ID = "student_id";
    public static final String KEY_TEACHER_ID = "teacher_id";
    public static final String KEY_FACE_DATA = "face_data";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRY_MINUTES = 5;

    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_OTP_TYPE = "extra_otp_type";
    public static final String EXTRA_STUDENT_ID = "extra_student_id";
    public static final String EXTRA_TEACHER_ID = "extra_teacher_id";
    public static final String EXTRA_CLASS_ID = "extra_class_id";
    public static final String EXTRA_CLASS_NAME = "extra_class_name";
    public static final String EXTRA_SESSION_ID = "extra_session_id";
    public static final String EXTRA_IS_EDIT = "extra_is_edit";
    public static final String EXTRA_QR_DATA = "extra_qr_data";

    public static final int REQUEST_CAMERA_PERMISSION = 100;
    public static final int REQUEST_LOCATION_PERMISSION = 101;
    public static final int REQUEST_BIOMETRIC = 102;
}
