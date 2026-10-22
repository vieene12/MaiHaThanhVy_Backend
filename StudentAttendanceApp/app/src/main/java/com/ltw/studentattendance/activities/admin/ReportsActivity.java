package com.ltw.studentattendance.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.adapters.ReportStudentAdapter;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.ClassRoom;
import com.ltw.studentattendance.models.ReportSummary;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private ImageButton btnBackReports;
    private Spinner spReportClass;
    private MaterialCardView cardReportSummary;
    private TextView tvReportClassName, tvReportOverallRate, tvReportTotalSessions, tvReportTotalStudents, tvEmptyReports;
    private RecyclerView rvReportStudents;
    private ProgressBar pbReports;

    private ReportStudentAdapter adapter;
    private ApiService apiService;
    private List<ClassRoom> classList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        apiService = ApiClient.getApiService(this);

        initViews();
        setupRecyclerView();
        setupEvents();
        loadClasses();
    }

    private void initViews() {
        btnBackReports = findViewById(R.id.btnBackReports);
        spReportClass = findViewById(R.id.spReportClass);
        cardReportSummary = findViewById(R.id.cardReportSummary);
        tvReportClassName = findViewById(R.id.tvReportClassName);
        tvReportOverallRate = findViewById(R.id.tvReportOverallRate);
        tvReportTotalSessions = findViewById(R.id.tvReportTotalSessions);
        tvReportTotalStudents = findViewById(R.id.tvReportTotalStudents);
        tvEmptyReports = findViewById(R.id.tvEmptyReports);
        rvReportStudents = findViewById(R.id.rvReportStudents);
        pbReports = findViewById(R.id.pbReports);
    }

    private void setupRecyclerView() {
        rvReportStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportStudentAdapter(this);
        rvReportStudents.setAdapter(adapter);
    }

    private void setupEvents() {
        btnBackReports.setOnClickListener(v -> finish());

        spReportClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!classList.isEmpty() && position < classList.size()) {
                    loadReport(classList.get(position).getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadClasses() {
        apiService.getClassRooms().enqueue(new Callback<ApiResponse<List<ClassRoom>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ClassRoom>>> call, Response<ApiResponse<List<ClassRoom>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    classList = response.body().getData();
                    if (classList == null) classList = new ArrayList<>();

                    List<String> names = new ArrayList<>();
                    for (ClassRoom c : classList) {
                        names.add(c.getClassName());
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(ReportsActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
                    spReportClass.setAdapter(spinnerAdapter);

                    if (!classList.isEmpty()) {
                        loadReport(classList.get(0).getId());
                    } else {
                        tvEmptyReports.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ClassRoom>>> call, Throwable t) {
                Toast.makeText(ReportsActivity.this, "Không thể tải danh sách lớp: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReport(int classId) {
        pbReports.setVisibility(View.VISIBLE);
        apiService.getReport(classId).enqueue(new Callback<ApiResponse<ReportSummary>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReportSummary>> call, Response<ApiResponse<ReportSummary>> response) {
                pbReports.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ReportSummary report = response.body().getData();
                    if (report != null) {
                        tvReportClassName.setText(report.getClassName() != null ? report.getClassName() : "Báo cáo điểm danh");
                        int rate = (int) Math.round(report.getAttendanceRate());
                        tvReportOverallRate.setText(rate + "%");
                        tvReportTotalSessions.setText("Tổng buổi: " + report.getTotalSessions());
                        tvReportTotalStudents.setText("Tổng SV: " + report.getTotalStudents());

                        List<ReportSummary.StudentReportItem> items = report.getStudentReports();
                        if (items != null && !items.isEmpty()) {
                            adapter.setReports(items);
                            tvEmptyReports.setVisibility(View.GONE);
                        } else {
                            adapter.setReports(new ArrayList<>());
                            tvEmptyReports.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    tvEmptyReports.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReportSummary>> call, Throwable t) {
                pbReports.setVisibility(View.GONE);
                tvEmptyReports.setVisibility(View.VISIBLE);
            }
        });
    }
}
