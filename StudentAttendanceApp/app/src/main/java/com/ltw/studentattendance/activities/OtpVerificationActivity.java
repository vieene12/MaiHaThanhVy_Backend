package com.ltw.studentattendance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.activities.admin.AdminDashboardActivity;
import com.ltw.studentattendance.activities.student.StudentDashboardActivity;
import com.ltw.studentattendance.activities.teacher.TeacherDashboardActivity;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.api.LoginResponse;
import com.ltw.studentattendance.models.OtpVerifyRequest;
import com.ltw.studentattendance.utils.Constants;
import com.ltw.studentattendance.utils.SessionManager;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {

    private TextView tvOtpUserEmail, tvTimer;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerifyOtp, btnResendOtp;
    private ProgressBar otpProgressBar;
    private String email = "";
    private CountDownTimer countDownTimer;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        apiService = ApiClient.getApiService(this);
        sessionManager = SessionManager.getInstance(this);

        email = getIntent().getStringExtra("email");
        if (email == null) email = "";

        initViews();
        setupOtpInputs();
        setupTimer();
        setupEvents();
    }

    private void initViews() {
        tvOtpUserEmail = findViewById(R.id.tvOtpUserEmail);
        tvTimer = findViewById(R.id.tvTimer);
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        otpProgressBar = findViewById(R.id.otpProgressBar);

        tvOtpUserEmail.setText(email);
    }

    private void setupOtpInputs() {
        EditText[] otpFields = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpFields[index].getText().length() == 0 && index > 0) {
                        otpFields[index - 1].requestFocus();
                        otpFields[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void setupTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        btnResendOtp.setEnabled(false);

        countDownTimer = new CountDownTimer(300000, 1000) { // 5 minutes
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    private void setupEvents() {
        btnVerifyOtp.setOnClickListener(v -> performVerifyOtp());
        btnResendOtp.setOnClickListener(v -> performResendOtp());
    }

    private void performVerifyOtp() {
        String otp = etOtp1.getText().toString().trim() +
                etOtp2.getText().toString().trim() +
                etOtp3.getText().toString().trim() +
                etOtp4.getText().toString().trim() +
                etOtp5.getText().toString().trim() +
                etOtp6.getText().toString().trim();

        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        OtpVerifyRequest request = new OtpVerifyRequest(email, otp);
        apiService.verifyOtp(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LoginResponse loginData = response.body().getData();
                    sessionManager.saveAuthToken(loginData.getToken());
                    if (loginData.getUser() != null) {
                        sessionManager.saveUserDetails(loginData.getUser());
                    } else if (loginData.getRole() != null) {
                        sessionManager.saveRole(loginData.getRole());
                    }
                    if (loginData.getStudentId() != null) {
                        sessionManager.saveStudentId(loginData.getStudentId());
                    }
                    if (loginData.getTeacherId() != null) {
                        sessionManager.saveTeacherId(loginData.getTeacherId());
                    }

                    Toast.makeText(OtpVerificationActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard(loginData.getRole());
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null)
                            ? response.body().getMessage() : "Mã OTP không đúng hoặc đã hết hạn";
                    Toast.makeText(OtpVerificationActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(OtpVerificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performResendOtp() {
        setLoading(true);
        apiService.resendOtp(email).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(OtpVerificationActivity.this, "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
                    setupTimer();
                } else {
                    Toast.makeText(OtpVerificationActivity.this, "Không thể gửi lại OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(OtpVerificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if (Constants.ROLE_TEACHER.equalsIgnoreCase(role)) {
            intent = new Intent(this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(this, StudentDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (otpProgressBar != null) {
            otpProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnVerifyOtp.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
