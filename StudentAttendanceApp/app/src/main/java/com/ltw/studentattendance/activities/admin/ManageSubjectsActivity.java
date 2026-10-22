package com.ltw.studentattendance.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.ltw.studentattendance.adapters.SubjectAdapter;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.Subject;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageSubjectsActivity extends AppCompatActivity {

    private ImageButton btnBackSubjects;
    private SwipeRefreshLayout swipeRefreshSubjects;
    private RecyclerView rvSubjects;
    private TextView tvEmptySubjects;
    private FloatingActionButton fabAddSubject;

    private SubjectAdapter adapter;
    private ApiService apiService;
    private List<Subject> subjectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_subjects);

        apiService = ApiClient.getApiService(this);

        initViews();
        setupRecyclerView();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSubjects();
    }

    private void initViews() {
        btnBackSubjects = findViewById(R.id.btnBackSubjects);
        swipeRefreshSubjects = findViewById(R.id.swipeRefreshSubjects);
        rvSubjects = findViewById(R.id.rvSubjects);
        tvEmptySubjects = findViewById(R.id.tvEmptySubjects);
        fabAddSubject = findViewById(R.id.fabAddSubject);
    }

    private void setupRecyclerView() {
        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(this, new SubjectAdapter.OnSubjectClickListener() {
            @Override
            public void onSubjectClick(Subject subject) {
                showSubjectDialog(subject);
            }

            @Override
            public void onEditClick(Subject subject) {
                showSubjectDialog(subject);
            }

            @Override
            public void onDeleteClick(Subject subject) {
                confirmDeleteSubject(subject);
            }
        });
        rvSubjects.setAdapter(adapter);
    }

    private void setupEvents() {
        btnBackSubjects.setOnClickListener(v -> finish());
        swipeRefreshSubjects.setOnRefreshListener(this::loadSubjects);
        fabAddSubject.setOnClickListener(v -> showSubjectDialog(null));
    }

    private void loadSubjects() {
        swipeRefreshSubjects.setRefreshing(true);
        apiService.getSubjects().enqueue(new Callback<ApiResponse<List<Subject>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Subject>>> call, Response<ApiResponse<List<Subject>>> response) {
                swipeRefreshSubjects.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    subjectList = response.body().getData();
                    if (subjectList == null) subjectList = new ArrayList<>();
                    adapter.setSubjects(subjectList);
                    updateEmptyView();
                } else {
                    updateEmptyView();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subject>>> call, Throwable t) {
                swipeRefreshSubjects.setRefreshing(false);
                updateEmptyView();
            }
        });
    }

    private void showSubjectDialog(Subject subject) {
        boolean isEdit = subject != null;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.item_subject, null);

        // We can create a simple programmatical form dialog or custom view
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etName = new EditText(this);
        etName.setHint("Tên môn học");
        if (isEdit) etName.setText(subject.getSubjectName());
        layout.addView(etName);

        EditText etCode = new EditText(this);
        etCode.setHint("Mã môn học (VD: IT3040)");
        if (isEdit) etCode.setText(subject.getSubjectCode());
        layout.addView(etCode);

        EditText etCredits = new EditText(this);
        etCredits.setHint("Số tín chỉ");
        etCredits.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (isEdit) etCredits.setText(String.valueOf(subject.getCredits()));
        layout.addView(etCredits);

        EditText etDesc = new EditText(this);
        etDesc.setHint("Mô tả");
        if (isEdit) etDesc.setText(subject.getDescription());
        layout.addView(etDesc);

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa môn học" : "Thêm môn học mới")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String code = etCode.getText().toString().trim();
                    String creditsStr = etCredits.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(code)) {
                        Toast.makeText(this, "Vui lòng nhập tên và mã môn học", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int credits = 3;
                    try {
                        credits = Integer.parseInt(creditsStr);
                    } catch (NumberFormatException ignored) {}

                    Subject item = isEdit ? subject : new Subject();
                    item.setSubjectName(name);
                    item.setSubjectCode(code);
                    item.setCredits(credits);
                    item.setDescription(desc);

                    saveSubject(item, isEdit);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveSubject(Subject subject, boolean isEdit) {
        if (isEdit) {
            apiService.updateSubject(subject.getId(), subject).enqueue(new Callback<ApiResponse<Subject>>() {
                @Override
                public void onResponse(Call<ApiResponse<Subject>> call, Response<ApiResponse<Subject>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ManageSubjectsActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        loadSubjects();
                    } else {
                        Toast.makeText(ManageSubjectsActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Subject>> call, Throwable t) {
                    Toast.makeText(ManageSubjectsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.createSubject(subject).enqueue(new Callback<ApiResponse<Subject>>() {
                @Override
                public void onResponse(Call<ApiResponse<Subject>> call, Response<ApiResponse<Subject>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ManageSubjectsActivity.this, "Thêm môn học thành công!", Toast.LENGTH_SHORT).show();
                        loadSubjects();
                    } else {
                        Toast.makeText(ManageSubjectsActivity.this, "Lỗi tạo môn học", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Subject>> call, Throwable t) {
                    Toast.makeText(ManageSubjectsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void confirmDeleteSubject(Subject subject) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa môn học")
                .setMessage("Bạn có chắc chắn muốn xóa môn \"" + subject.getSubjectName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    apiService.deleteSubject(subject.getId()).enqueue(new Callback<ApiResponse<Boolean>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(ManageSubjectsActivity.this, "Đã xóa môn học", Toast.LENGTH_SHORT).show();
                                loadSubjects();
                            } else {
                                Toast.makeText(ManageSubjectsActivity.this, "Không thể xóa môn học", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                            Toast.makeText(ManageSubjectsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            tvEmptySubjects.setVisibility(View.VISIBLE);
        } else {
            tvEmptySubjects.setVisibility(View.GONE);
        }
    }
}
