package com.ltw.studentattendance.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.adapters.StudentAdapter;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.Student;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageStudentsActivity extends AppCompatActivity {

    private ImageButton btnBackStudents;
    private EditText etSearchStudent;
    private SwipeRefreshLayout swipeRefreshStudents;
    private RecyclerView rvStudents;
    private TextView tvEmptyStudents;
    private FloatingActionButton fabAddStudent;

    private StudentAdapter adapter;
    private ApiService apiService;
    private List<Student> fullStudentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        apiService = ApiClient.getApiService(this);

        initViews();
        setupRecyclerView();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudents();
    }

    private void initViews() {
        btnBackStudents = findViewById(R.id.btnBackStudents);
        etSearchStudent = findViewById(R.id.etSearchStudent);
        swipeRefreshStudents = findViewById(R.id.swipeRefreshStudents);
        rvStudents = findViewById(R.id.rvStudents);
        tvEmptyStudents = findViewById(R.id.tvEmptyStudents);
        fabAddStudent = findViewById(R.id.fabAddStudent);
    }

    private void setupRecyclerView() {
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(this, new StudentAdapter.OnStudentClickListener() {
            @Override
            public void onStudentClick(Student student) {
                // Open edit
                openEditStudent(student);
            }

            @Override
            public void onEditClick(Student student) {
                openEditStudent(student);
            }

            @Override
            public void onDeleteClick(Student student) {
                confirmDeleteStudent(student);
            }
        });
        rvStudents.setAdapter(adapter);
    }

    private void setupEvents() {
        btnBackStudents.setOnClickListener(v -> finish());
        swipeRefreshStudents.setOnRefreshListener(this::loadStudents);

        fabAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditStudentActivity.class);
            startActivity(intent);
        });

        etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadStudents() {
        swipeRefreshStudents.setRefreshing(true);
        apiService.getStudents(1, 100, "").enqueue(new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Student>>> call, Response<ApiResponse<List<Student>>> response) {
                swipeRefreshStudents.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    fullStudentList = response.body().getData();
                    if (fullStudentList == null) fullStudentList = new ArrayList<>();
                    adapter.setStudents(fullStudentList);
                    updateEmptyView();
                } else {
                    Toast.makeText(ManageStudentsActivity.this, "Không thể tải danh sách sinh viên", Toast.LENGTH_SHORT).show();
                    updateEmptyView();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Student>>> call, Throwable t) {
                swipeRefreshStudents.setRefreshing(false);
                Toast.makeText(ManageStudentsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyView();
            }
        });
    }

    private void filterStudents(String query) {
        if (query.isEmpty()) {
            adapter.setStudents(fullStudentList);
        } else {
            List<Student> filtered = new ArrayList<>();
            for (Student s : fullStudentList) {
                if ((s.getFullName() != null && s.getFullName().toLowerCase().contains(query.toLowerCase())) ||
                    (s.getStudentCode() != null && s.getStudentCode().toLowerCase().contains(query.toLowerCase())) ||
                    (s.getMajor() != null && s.getMajor().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(s);
                }
            }
            adapter.setStudents(filtered);
        }
    }

    private void openEditStudent(Student student) {
        Intent intent = new Intent(this, AddEditStudentActivity.class);
        intent.putExtra("student", student);
        startActivity(intent);
    }

    private void confirmDeleteStudent(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sinh viên")
                .setMessage("Bạn có chắc chắn muốn xóa sinh viên \"" + student.getFullName() + "\" (" + student.getStudentCode() + ")?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteStudent(student))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteStudent(Student student) {
        apiService.deleteStudent(student.getId()).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ManageStudentsActivity.this, "Đã xóa sinh viên", Toast.LENGTH_SHORT).show();
                    loadStudents();
                } else {
                    Toast.makeText(ManageStudentsActivity.this, "Không thể xóa sinh viên", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(ManageStudentsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            tvEmptyStudents.setVisibility(View.VISIBLE);
        } else {
            tvEmptyStudents.setVisibility(View.GONE);
        }
    }
}
