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
import com.ltw.studentattendance.adapters.ClassAdapter;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.ClassRoom;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageClassesActivity extends AppCompatActivity {

    private ImageButton btnBackClasses;
    private EditText etSearchClass;
    private SwipeRefreshLayout swipeRefreshClasses;
    private RecyclerView rvClasses;
    private TextView tvEmptyClasses;
    private FloatingActionButton fabAddClass;

    private ClassAdapter adapter;
    private ApiService apiService;
    private List<ClassRoom> fullClassList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_classes);

        apiService = ApiClient.getApiService(this);

        initViews();
        setupRecyclerView();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
    }

    private void initViews() {
        btnBackClasses = findViewById(R.id.btnBackClasses);
        etSearchClass = findViewById(R.id.etSearchClass);
        swipeRefreshClasses = findViewById(R.id.swipeRefreshClasses);
        rvClasses = findViewById(R.id.rvClasses);
        tvEmptyClasses = findViewById(R.id.tvEmptyClasses);
        fabAddClass = findViewById(R.id.fabAddClass);
    }

    private void setupRecyclerView() {
        rvClasses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(this, new ClassAdapter.OnClassClickListener() {
            @Override
            public void onClassClick(ClassRoom classRoom) {
                openEditClass(classRoom);
            }

            @Override
            public void onEditClick(ClassRoom classRoom) {
                openEditClass(classRoom);
            }

            @Override
            public void onDeleteClick(ClassRoom classRoom) {
                confirmDeleteClass(classRoom);
            }
        });
        rvClasses.setAdapter(adapter);
    }

    private void setupEvents() {
        btnBackClasses.setOnClickListener(v -> finish());
        swipeRefreshClasses.setOnRefreshListener(this::loadClasses);

        fabAddClass.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditClassActivity.class);
            startActivity(intent);
        });

        etSearchClass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClasses(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadClasses() {
        swipeRefreshClasses.setRefreshing(true);
        apiService.getClassRooms().enqueue(new Callback<ApiResponse<List<ClassRoom>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ClassRoom>>> call, Response<ApiResponse<List<ClassRoom>>> response) {
                swipeRefreshClasses.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    fullClassList = response.body().getData();
                    if (fullClassList == null) fullClassList = new ArrayList<>();
                    adapter.setClasses(fullClassList);
                    updateEmptyView();
                } else {
                    Toast.makeText(ManageClassesActivity.this, "Không thể tải danh sách lớp học", Toast.LENGTH_SHORT).show();
                    updateEmptyView();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ClassRoom>>> call, Throwable t) {
                swipeRefreshClasses.setRefreshing(false);
                Toast.makeText(ManageClassesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyView();
            }
        });
    }

    private void filterClasses(String query) {
        if (query.isEmpty()) {
            adapter.setClasses(fullClassList);
        } else {
            List<ClassRoom> filtered = new ArrayList<>();
            for (ClassRoom c : fullClassList) {
                if ((c.getClassName() != null && c.getClassName().toLowerCase().contains(query.toLowerCase())) ||
                    (c.getSubjectName() != null && c.getSubjectName().toLowerCase().contains(query.toLowerCase())) ||
                    (c.getRoom() != null && c.getRoom().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(c);
                }
            }
            adapter.setClasses(filtered);
        }
    }

    private void openEditClass(ClassRoom classRoom) {
        Intent intent = new Intent(this, AddEditClassActivity.class);
        intent.putExtra("classRoom", classRoom);
        startActivity(intent);
    }

    private void confirmDeleteClass(ClassRoom classRoom) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lớp học")
                .setMessage("Bạn có chắc chắn muốn xóa lớp \"" + classRoom.getClassName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteClass(classRoom))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteClass(ClassRoom classRoom) {
        apiService.deleteClassRoom(classRoom.getId()).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ManageClassesActivity.this, "Đã xóa lớp học", Toast.LENGTH_SHORT).show();
                    loadClasses();
                } else {
                    Toast.makeText(ManageClassesActivity.this, "Không thể xóa lớp học", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(ManageClassesActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            tvEmptyClasses.setVisibility(View.VISIBLE);
        } else {
            tvEmptyClasses.setVisibility(View.GONE);
        }
    }
}
