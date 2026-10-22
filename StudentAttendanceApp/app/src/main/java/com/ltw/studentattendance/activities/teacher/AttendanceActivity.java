package com.ltw.studentattendance.activities.teacher;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.adapters.AttendanceRecordAdapter;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.AttendanceRecord;
import com.ltw.studentattendance.models.AttendanceSession;
import com.ltw.studentattendance.utils.Constants;
import com.ltw.studentattendance.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity extends AppCompatActivity {

    private TextView tvClassName, tvSessionStatus, tvStudentCount;
    private ImageView ivQrCode;
    private MaterialButton btnCreateSession, btnRefresh, btnBack;
    private RecyclerView rvAttendance;
    private ProgressBar progressBar;
    private View layoutQr, layoutNoSession;

    private ApiService apiService;
    private SessionManager sessionManager;
    private AttendanceRecordAdapter recordAdapter;
    private List<AttendanceRecord> records = new ArrayList<>();

    private int classId, teacherId;
    private String className;
    private AttendanceSession activeSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        classId = getIntent().getIntExtra(Constants.EXTRA_CLASS_ID, -1);
        className = getIntent().getStringExtra(Constants.EXTRA_CLASS_NAME);
        teacherId = getIntent().getIntExtra(Constants.EXTRA_TEACHER_ID, -1);

        apiService = ApiClient.getApiService(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupRecyclerView();
        setupEvents();
        loadActiveSession();
    }

    private void initViews() {
        tvClassName = findViewById(R.id.tvClassName);
        tvSessionStatus = findViewById(R.id.tvSessionStatus);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        ivQrCode = findViewById(R.id.ivQrCode);
        btnCreateSession = findViewById(R.id.btnCreateSession);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnBack = findViewById(R.id.btnBack);
        rvAttendance = findViewById(R.id.rvAttendance);
        progressBar = findViewById(R.id.progressBar);
        layoutQr = findViewById(R.id.layoutQr);
        layoutNoSession = findViewById(R.id.layoutNoSession);

        tvClassName.setText(className != null ? className : "Lớp học");
        tvSessionStatus.setText("Chưa có phiên điểm danh");
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recordAdapter = new AttendanceRecordAdapter(records);
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        rvAttendance.setAdapter(recordAdapter);
    }

    private void setupEvents() {
        btnCreateSession.setOnClickListener(v -> createNewSession());
        btnRefresh.setOnClickListener(v -> {
            if (activeSession != null) {
                loadSessionRecords(activeSession.getId());
            }
        });
    }

    private void loadActiveSession() {
        if (classId <= 0) return;
        progressBar.setVisibility(View.VISIBLE);
        apiService.getActiveSession(classId).enqueue(new Callback<ApiResponse<AttendanceSession>>() {
            @Override
            public void onResponse(Call<ApiResponse<AttendanceSession>> call, Response<ApiResponse<AttendanceSession>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    activeSession = response.body().getData();
                    showActiveSession(activeSession);
                } else {
                    showNoSession();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<AttendanceSession>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showNoSession();
            }
        });
    }

    private void createNewSession() {
        AttendanceSession session = new AttendanceSession();
        session.setClassRoomId(classId);
        session.setTeacherId(teacherId);

        btnCreateSession.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        apiService.createSession(session).enqueue(new Callback<ApiResponse<AttendanceSession>>() {
            @Override
            public void onResponse(Call<ApiResponse<AttendanceSession>> call, Response<ApiResponse<AttendanceSession>> response) {
                btnCreateSession.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    activeSession = response.body().getData();
                    showActiveSession(activeSession);
                    Toast.makeText(AttendanceActivity.this, "Tạo phiên điểm danh thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AttendanceActivity.this, "Không thể tạo phiên điểm danh", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<AttendanceSession>> call, Throwable t) {
                btnCreateSession.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AttendanceActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showActiveSession(AttendanceSession session) {
        tvSessionStatus.setText("Phiên đang hoạt động - ID: " + session.getId());
        layoutQr.setVisibility(View.VISIBLE);
        layoutNoSession.setVisibility(View.GONE);
        btnCreateSession.setVisibility(View.GONE);

        // Tạo QR code chứa session ID
        String qrContent = "attendance_session:" + session.getId() + ":class:" + classId;
        generateQRCode(qrContent);
        loadSessionRecords(session.getId());
    }

    private void showNoSession() {
        tvSessionStatus.setText("Chưa có phiên điểm danh");
        layoutQr.setVisibility(View.GONE);
        layoutNoSession.setVisibility(View.VISIBLE);
        btnCreateSession.setVisibility(View.VISIBLE);
    }

    private void generateQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            ivQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "Không thể tạo QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSessionRecords(int sessionId) {
        apiService.getSessionRecords(sessionId).enqueue(new Callback<ApiResponse<List<AttendanceRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AttendanceRecord>>> call, Response<ApiResponse<List<AttendanceRecord>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<AttendanceRecord> data = response.body().getData();
                    records.clear();
                    if (data != null) records.addAll(data);
                    recordAdapter.notifyDataSetChanged();
                    tvStudentCount.setText("Đã điểm danh: " + records.size() + " sinh viên");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<AttendanceRecord>>> call, Throwable t) {}
        });
    }
}
