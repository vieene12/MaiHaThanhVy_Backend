package com.ltw.studentattendance.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.activities.admin.AdminDashboardActivity;
import com.ltw.studentattendance.activities.student.StudentDashboardActivity;
import com.ltw.studentattendance.activities.teacher.TeacherDashboardActivity;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.api.LoginResponse;
import com.ltw.studentattendance.models.FaceLoginRequest;
import com.ltw.studentattendance.utils.Constants;
import com.ltw.studentattendance.utils.SessionManager;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceLoginActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView tvFaceStatus;
    private Button btnFaceCheckIn;
    private ProgressBar faceProgressBar;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private ApiService apiService;
    private SessionManager sessionManager;
    private boolean isFaceDetected = false;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Ứng dụng cần quyền Camera để nhận diện khuôn mặt", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_login);

        apiService = ApiClient.getApiService(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupFaceDetector();
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        tvFaceStatus = findViewById(R.id.tvFaceStatus);
        btnFaceCheckIn = findViewById(R.id.btnFaceCheckIn);
        faceProgressBar = findViewById(R.id.faceProgressBar);

        btnFaceCheckIn.setOnClickListener(v -> performFaceLogin());
    }

    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();
        faceDetector = FaceDetection.getClient(options);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Không thể khởi động Camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() != null) {
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            faceDetector.process(image)
                    .addOnSuccessListener(faces -> {
                        boolean detected = faces != null && !faces.isEmpty();
                        runOnUiThread(() -> updateFaceStatus(detected));
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private void updateFaceStatus(boolean detected) {
        if (isFaceDetected != detected) {
            isFaceDetected = detected;
            if (detected) {
                tvFaceStatus.setText(R.string.face_detected);
                tvFaceStatus.setTextColor(ContextCompat.getColor(this, R.color.present));
                tvFaceStatus.setBackgroundResource(R.drawable.badge_present);
                btnFaceCheckIn.setEnabled(true);
            } else {
                tvFaceStatus.setText(R.string.face_not_detected);
                tvFaceStatus.setTextColor(ContextCompat.getColor(this, R.color.warning));
                tvFaceStatus.setBackgroundResource(R.drawable.badge_late);
                btnFaceCheckIn.setEnabled(false);
            }
        }
    }

    private void performFaceLogin() {
        setLoading(true);

        FaceLoginRequest request = new FaceLoginRequest("SIMULATED_FACE_FEATURE_VECTOR", "");
        apiService.faceLogin(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
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

                    Toast.makeText(FaceLoginActivity.this, "Đăng nhập khuôn mặt thành công!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard(loginData.getRole());
                } else {
                    Toast.makeText(FaceLoginActivity.this, "Không nhận diện được khuôn mặt trong hệ thống", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(FaceLoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (faceProgressBar != null) {
            faceProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnFaceCheckIn.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();
    }
}
