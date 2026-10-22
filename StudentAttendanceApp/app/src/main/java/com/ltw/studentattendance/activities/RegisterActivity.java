package com.ltw.studentattendance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.RegisterRequest;
import com.ltw.studentattendance.models.User;
import com.ltw.studentattendance.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etRegisterEmail, etPhone, etRegisterPassword, etConfirmPassword;
    private RadioGroup rgRole;
    private RadioButton rbStudent;
    private Button btnRegisterSubmit;
    private TextView tvLoginLink;
    private ProgressBar registerProgressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getApiService(this);

        initViews();
        setupEvents();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etPhone = findViewById(R.id.etPhone);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        rbStudent = findViewById(R.id.rbStudent);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        registerProgressBar = findViewById(R.id.registerProgressBar);
    }

    private void setupEvents() {
        btnRegisterSubmit.setOnClickListener(v -> performRegister());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etRegisterEmail.getText() != null ? etRegisterEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etRegisterPassword.getText() != null ? etRegisterPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.error_field_required));
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etRegisterEmail.setError(getString(R.string.error_field_required));
            etRegisterEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etRegisterPassword.setError(getString(R.string.error_field_required));
            etRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etRegisterPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            etRegisterPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_match));
            etConfirmPassword.requestFocus();
            return;
        }

        String role = rbStudent.isChecked() ? Constants.ROLE_STUDENT : Constants.ROLE_TEACHER;

        setLoading(true);

        RegisterRequest request = new RegisterRequest(fullName, email, password, phone, role);
        apiService.register(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> body = response.body();
                    if (body.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng xác thực OTP.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, body.getMessage() != null ? body.getMessage() : "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Lỗi đăng ký: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (registerProgressBar != null) {
            registerProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnRegisterSubmit.setEnabled(!isLoading);
    }
}
