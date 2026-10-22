package com.ltw.studentattendance.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.activities.LoginActivity;
import com.ltw.studentattendance.activities.LocationShareActivity;
import com.ltw.studentattendance.activities.QRScannerActivity;
import com.ltw.studentattendance.adapters.ClassAdapter;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.ClassRoom;
import com.ltw.studentattendance.models.DashboardStats;
import com.ltw.studentattendance.utils.Constants;
import com.ltw.studentattendance.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalClasses, tvAttendanceRate, tvPresentCount, tvAbsentCount;
    private RecyclerView rvClasses;
    private ProgressBar progressBar;
    private MaterialButton btnQrScan, btnLocation, btnMyAttendance, btnLogout;
    private LinearLayout layoutStats;

    private ApiService apiService;
    private SessionManager sessionManager;
    private ClassAdapter classAdapter;
    private List<ClassRoom> classList = new ArrayList<>();
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        apiService = ApiClient.getApiService(this);
        sessionManager = SessionManager.getInstance(this);
        studentId = sessionManager.getStudentId();

        initViews();
        setupRecyclerView();
        setupEvents();
        loadData();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalClasses = findViewById(R.id.tvTotalClasses);
        tvAttendanceRate = findViewById(R.id.tvAttendanceRate);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        rvClasses = findViewById(R.id.rvClasses);
        progressBar = findViewById(R.id.progressBar);
        btnQrScan = findViewById(R.id.btnQrScan);
        btnLocation = findViewById(R.id.btnLocation);
        btnMyAttendance = findViewById(R.id.btnMyAttendance);
        btnLogout = findViewById(R.id.btnLogout);
        layoutStats = findViewById(R.id.layoutStats);

        String name = sessionManager.getUserName();
        tvWelcome.setText("Xin chào, " + (name != null ? name : "Sinh viên") + "!");
    }

    private void setupRecyclerView() {
        classAdapter = new ClassAdapter(classList, classRoom -> {
            // Click vào lớp → xem điểm danh
            Intent intent = new Intent(this, MyAttendanceActivity.class);
            intent.putExtra(Constants.EXTRA_CLASS_ID, classRoom.getId());
            intent.putExtra(Constants.EXTRA_CLASS_NAME, classRoom.getClassName());
            startActivity(intent);
        });
        rvClasses.setLayoutManager(new LinearLayoutManager(this));
        rvClasses.setAdapter(classAdapter);
    }

    private void setupEvents() {
        btnQrScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRScannerActivity.class);
            startActivity(intent);
        });

        btnLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationShareActivity.class);
            startActivity(intent);
        });

        btnMyAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyAttendanceActivity.class);
            intent.putExtra(Constants.EXTRA_STUDENT_ID, studentId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadData() {
        loadDashboardStats();
        loadMyClasses();
    }

    private void loadDashboardStats() {
        if (studentId <= 0) return;
        apiService.getStudentDashboard(studentId).enqueue(new Callback<ApiResponse<DashboardStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardStats>> call, Response<ApiResponse<DashboardStats>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DashboardStats stats = response.body().getData();
                    if (stats != null) {
                        tvTotalClasses.setText(String.valueOf(stats.getTotalClasses()));
                        tvAttendanceRate.setText(String.format("%.1f%%", stats.getAttendanceRate()));
                        tvPresentCount.setText(String.valueOf(stats.getTotalPresent()));
                        tvAbsentCount.setText(String.valueOf(stats.getTotalAbsent()));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardStats>> call, Throwable t) {
                // Ignore - stats không bắt buộc
            }
        });
    }

    private void loadMyClasses() {
        if (studentId <= 0) return;
        progressBar.setVisibility(View.VISIBLE);
        apiService.getClassRoomsByStudent(studentId).enqueue(new Callback<ApiResponse<List<ClassRoom>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ClassRoom>>> call, Response<ApiResponse<List<ClassRoom>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ClassRoom> data = response.body().getData();
                    classList.clear();
                    if (data != null) classList.addAll(data);
                    classAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ClassRoom>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StudentDashboardActivity.this, "Lỗi tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    sessionManager.clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
