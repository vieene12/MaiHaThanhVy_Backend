package com.ltw.studentattendance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.activities.admin.AdminDashboardActivity;
import com.ltw.studentattendance.activities.student.StudentDashboardActivity;
import com.ltw.studentattendance.activities.teacher.TeacherDashboardActivity;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.api.LoginResponse;
import com.ltw.studentattendance.models.GoogleLoginRequest;
import com.ltw.studentattendance.models.LoginRequest;
import com.ltw.studentattendance.utils.Constants;
import com.ltw.studentattendance.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private MaterialButton btnGoogleLogin, btnFaceLogin;
    private TextView tvRegisterLink;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiClient.getApiService(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupGoogleSignIn();
        setupEvents();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnFaceLogin = findViewById(R.id.btnFaceLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupEvents() {
        btnLogin.setOnClickListener(v -> performLogin());

        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        btnFaceLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, FaceLoginActivity.class);
            startActivity(intent);
        });

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_field_required));
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_field_required));
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> body = response.body();
                    if (body.isSuccess() && body.getData() != null) {
                        LoginResponse loginData = body.getData();
                        handleLoginSuccess(loginData, email);
                    } else {
                        Toast.makeText(LoginActivity.this, body.getMessage() != null ? body.getMessage() : "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Lỗi đăng nhập: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                setLoading(true);
                GoogleLoginRequest request = new GoogleLoginRequest(
                        account.getIdToken() != null ? account.getIdToken() : "",
                        account.getEmail() != null ? account.getEmail() : "",
                        account.getDisplayName() != null ? account.getDisplayName() : ""
                );

                apiService.googleLogin(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            handleLoginSuccess(response.body().getData(), account.getEmail());
                        } else {
                            Toast.makeText(LoginActivity.this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLoginSuccess(LoginResponse loginData, String email) {
        if (loginData.getToken() != null && !loginData.getToken().isEmpty()) {
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

            navigateToDashboard(loginData.getRole());
        } else {
            // Need OTP verification
            Intent intent = new Intent(this, OtpVerificationActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
        }
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
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!isLoading);
        btnGoogleLogin.setEnabled(!isLoading);
        btnFaceLogin.setEnabled(!isLoading);
    }
}
