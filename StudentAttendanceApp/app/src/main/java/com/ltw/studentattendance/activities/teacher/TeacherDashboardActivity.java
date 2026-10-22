package com.ltw.studentattendance.activities.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ltw.studentattendance.R;
import com.ltw.studentattendance.activities.LoginActivity;
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

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalClasses, tvTodaySessions, tvAttendanceRate;
    private RecyclerView rvClasses;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SessionManager sessionManager;
    private ClassAdapter classAdapter;
    private List<ClassRoom> classList = new ArrayList<>();
    private int teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        apiService = ApiClient.getApiService(this);
        sessionManager = SessionManager.getInstance(this);
        teacherId = sessionManager.getTeacherId();

        initViews();
        setupRecyclerView();
        setupEvents();
        loadData();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalClasses = findViewById(R.id.tvTotalClasses);
        tvTodaySessions = findViewById(R.id.tvTodaySessions);
        tvAttendanceRate = findViewById(R.id.tvAttendanceRate);
        rvClasses = findViewById(R.id.rvClasses);
        progressBar = findViewById(R.id.progressBar);

        String name = sessionManager.getUserName();
        tvWelcome.setText("Xin chào, GV " + (name != null ? name : "") + "!");

        // Logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
    }

    private void setupRecyclerView() {
        classAdapter = new ClassAdapter(classList, classRoom -> {
            // Chọn lớp → đi đến màn hình điểm danh
            Intent intent = new Intent(this, AttendanceActivity.class);
            intent.putExtra(Constants.EXTRA_CLASS_ID, classRoom.getId());
            intent.putExtra(Constants.EXTRA_CLASS_NAME, classRoom.getClassName());
            intent.putExtra(Constants.EXTRA_TEACHER_ID, teacherId);
            startActivity(intent);
        });
        rvClasses.setLayoutManager(new LinearLayoutManager(this));
        rvClasses.setAdapter(classAdapter);
    }

    private void setupEvents() {
        // Intentionally empty - events set in initViews
    }

    private void loadData() {
        loadStats();
        loadMyClasses();
    }

    private void loadStats() {
        if (teacherId <= 0) return;
        apiService.getTeacherDashboard(teacherId).enqueue(new Callback<ApiResponse<DashboardStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardStats>> call, Response<ApiResponse<DashboardStats>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DashboardStats stats = response.body().getData();
                    if (stats != null) {
                        tvTotalClasses.setText(String.valueOf(stats.getTotalClasses()));
                        tvTodaySessions.setText(String.valueOf(stats.getTodaySessions()));
                        tvAttendanceRate.setText(String.format("%.1f%%", stats.getAttendanceRate()));
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<DashboardStats>> call, Throwable t) {}
        });
    }

    private void loadMyClasses() {
        if (teacherId <= 0) return;
        progressBar.setVisibility(View.VISIBLE);
        apiService.getClassRoomsByTeacher(teacherId).enqueue(new Callback<ApiResponse<List<ClassRoom>>>() {
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
                Toast.makeText(TeacherDashboardActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
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
