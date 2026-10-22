package com.ltw.studentattendance.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.Result;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.AttendanceRecord;
import com.ltw.studentattendance.models.CheckInRequest;
import com.ltw.studentattendance.utils.Constants;
import com.ltw.studentattendance.utils.SessionManager;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private TextView tvStatus;
    private ApiService apiService;
    private SessionManager sessionManager;
    private boolean scanned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        apiService = ApiClient.getApiService(this);
        sessionManager = SessionManager.getInstance(this);

        tvStatus = findViewById(R.id.tvStatus);
        scannerView = findViewById(R.id.scannerView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanned = false;
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        if (scanned) return;
        scanned = true;

        String qrData = result.getText();
        tvStatus.setText("Đã quét: " + qrData);

        // Parse QR: "attendance_session:{sessionId}:class:{classId}"
        if (qrData.startsWith("attendance_session:")) {
            try {
                String[] parts = qrData.split(":");
                int sessionId = Integer.parseInt(parts[1]);
                int classId = Integer.parseInt(parts[3]);
                performCheckIn(sessionId, classId);
            } catch (Exception e) {
                Toast.makeText(this, "Mã QR không hợp lệ", Toast.LENGTH_SHORT).show();
                scanned = false;
                scannerView.resumeCameraPreview(this);
            }
        } else {
            Toast.makeText(this, "QR không phải mã điểm danh", Toast.LENGTH_SHORT).show();
            scanned = false;
            scannerView.resumeCameraPreview(this);
        }
    }

    private void performCheckIn(int sessionId, int classId) {
        int studentId = sessionManager.getStudentId();
        if (studentId <= 0) {
            Toast.makeText(this, "Không tìm thấy thông tin sinh viên", Toast.LENGTH_SHORT).show();
            return;
        }

        CheckInRequest request = new CheckInRequest();
        request.setSessionId(sessionId);
        request.setStudentId(studentId);
        request.setClassId(classId);
        request.setMethod("QR");

        tvStatus.setText("Đang xác nhận điểm danh...");

        apiService.qrCheckIn(request).enqueue(new Callback<ApiResponse<AttendanceRecord>>() {
            @Override
            public void onResponse(Call<ApiResponse<AttendanceRecord>> call, Response<ApiResponse<AttendanceRecord>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvStatus.setText("✅ Điểm danh thành công!");
                    Toast.makeText(QRScannerActivity.this, getString(R.string.check_in_success), Toast.LENGTH_LONG).show();
                    // Đợi 2 giây rồi quay lại
                    scannerView.postDelayed(() -> finish(), 2000);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Điểm danh thất bại";
                    tvStatus.setText("❌ " + msg);
                    Toast.makeText(QRScannerActivity.this, msg, Toast.LENGTH_LONG).show();
                    scanned = false;
                    scannerView.resumeCameraPreview(QRScannerActivity.this);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AttendanceRecord>> call, Throwable t) {
                tvStatus.setText("❌ Lỗi kết nối");
                Toast.makeText(QRScannerActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                scanned = false;
                scannerView.resumeCameraPreview(QRScannerActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scannerView.startCamera();
            } else {
                Toast.makeText(this, "Cần cấp quyền camera để quét QR", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
