package com.ltw.studentattendance.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.ClassRoom;
import com.ltw.studentattendance.models.Subject;
import com.ltw.studentattendance.models.Teacher;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditClassActivity extends AppCompatActivity {

    private ImageButton btnBackAddClass;
    private TextView tvAddEditClassTitle;
    private TextInputEditText etClassName, etRoom, etSemester, etSchedule, etMaxStudents;
    private Spinner spSubject, spTeacher;
    private Button btnCancelClass, btnSaveClass;
    private ProgressBar pbClassForm;

    private ApiService apiService;
    private ClassRoom editingClass = null;
    private boolean isEditMode = false;

    private List<Subject> subjectList = new ArrayList<>();
    private List<Teacher> teacherList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_class);

        apiService = ApiClient.getApiService(this);

        initViews();
        loadSpinners();
        checkEditMode();
        setupEvents();
    }

    private void initViews() {
        btnBackAddClass = findViewById(R.id.btnBackAddClass);
        tvAddEditClassTitle = findViewById(R.id.tvAddEditClassTitle);
        etClassName = findViewById(R.id.etClassName);
        etRoom = findViewById(R.id.etRoom);
        etSemester = findViewById(R.id.etSemester);
        etSchedule = findViewById(R.id.etSchedule);
        etMaxStudents = findViewById(R.id.etMaxStudents);
        spSubject = findViewById(R.id.spSubject);
        spTeacher = findViewById(R.id.spTeacher);
        btnCancelClass = findViewById(R.id.btnCancelClass);
        btnSaveClass = findViewById(R.id.btnSaveClass);
        pbClassForm = findViewById(R.id.pbClassForm);
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("classRoom")) {
            editingClass = (ClassRoom) getIntent().getSerializableExtra("classRoom");
            if (editingClass != null) {
                isEditMode = true;
                tvAddEditClassTitle.setText(R.string.edit_class);
                etClassName.setText(editingClass.getClassName());
                etRoom.setText(editingClass.getRoom());
                etSemester.setText(editingClass.getSemester());
                etSchedule.setText(editingClass.getSchedule());
                etMaxStudents.setText(String.valueOf(editingClass.getMaxStudents()));
            }
        }
    }

    private void loadSpinners() {
        // Load subjects
        apiService.getSubjects().enqueue(new Callback<ApiResponse<List<Subject>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Subject>>> call, Response<ApiResponse<List<Subject>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    subjectList = response.body().getData();
                    if (subjectList == null) subjectList = new ArrayList<>();
                    List<String> names = new ArrayList<>();
                    int selectedIdx = 0;
                    for (int i = 0; i < subjectList.size(); i++) {
                        Subject s = subjectList.get(i);
                        names.add(s.getSubjectName() + " (" + s.getSubjectCode() + ")");
                        if (isEditMode && editingClass != null && editingClass.getSubjectId() == s.getId()) {
                            selectedIdx = i;
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEditClassActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
                    spSubject.setAdapter(adapter);
                    if (isEditMode && !names.isEmpty()) spSubject.setSelection(selectedIdx);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subject>>> call, Throwable t) {}
        });

        // Load teachers
        apiService.getTeachers().enqueue(new Callback<ApiResponse<List<Teacher>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Teacher>>> call, Response<ApiResponse<List<Teacher>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    teacherList = response.body().getData();
                    if (teacherList == null) teacherList = new ArrayList<>();
                    List<String> names = new ArrayList<>();
                    int selectedIdx = 0;
                    for (int i = 0; i < teacherList.size(); i++) {
                        Teacher t = teacherList.get(i);
                        names.add(t.getFullName() + " (" + (t.getTitle() != null ? t.getTitle() : "GV") + ")");
                        if (isEditMode && editingClass != null && editingClass.getTeacherId() == t.getId()) {
                            selectedIdx = i;
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEditClassActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
                    spTeacher.setAdapter(adapter);
                    if (isEditMode && !names.isEmpty()) spTeacher.setSelection(selectedIdx);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Teacher>>> call, Throwable t) {}
        });
    }

    private void setupEvents() {
        btnBackAddClass.setOnClickListener(v -> finish());
        btnCancelClass.setOnClickListener(v -> finish());
        btnSaveClass.setOnClickListener(v -> performSave());
    }

    private void performSave() {
        String className = etClassName.getText() != null ? etClassName.getText().toString().trim() : "";
        String room = etRoom.getText() != null ? etRoom.getText().toString().trim() : "";
        String semester = etSemester.getText() != null ? etSemester.getText().toString().trim() : "";
        String schedule = etSchedule.getText() != null ? etSchedule.getText().toString().trim() : "";
        String maxStudentsStr = etMaxStudents.getText() != null ? etMaxStudents.getText().toString().trim() : "50";

        if (TextUtils.isEmpty(className)) {
            etClassName.setError(getString(R.string.error_field_required));
            etClassName.requestFocus();
            return;
        }

        int maxStudents = 50;
        try {
            maxStudents = Integer.parseInt(maxStudentsStr);
        } catch (NumberFormatException ignored) {}

        int subjectId = 0;
        if (!subjectList.isEmpty() && spSubject.getSelectedItemPosition() >= 0 && spSubject.getSelectedItemPosition() < subjectList.size()) {
            subjectId = subjectList.get(spSubject.getSelectedItemPosition()).getId();
        }

        int teacherId = 0;
        if (!teacherList.isEmpty() && spTeacher.getSelectedItemPosition() >= 0 && spTeacher.getSelectedItemPosition() < teacherList.size()) {
            teacherId = teacherList.get(spTeacher.getSelectedItemPosition()).getId();
        }

        setLoading(true);

        ClassRoom item = isEditMode ? editingClass : new ClassRoom();
        item.setClassName(className);
        item.setSubjectId(subjectId);
        item.setTeacherId(teacherId);
        item.setRoom(room);
        item.setSemester(semester);
        item.setSchedule(schedule);
        item.setMaxStudents(maxStudents);

        if (isEditMode) {
            apiService.updateClassRoom(item.getId(), item).enqueue(new Callback<ApiResponse<ClassRoom>>() {
                @Override
                public void onResponse(Call<ApiResponse<ClassRoom>> call, Response<ApiResponse<ClassRoom>> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddEditClassActivity.this, "Cập nhật lớp thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddEditClassActivity.this, "Không thể cập nhật lớp", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ClassRoom>> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(AddEditClassActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.createClassRoom(item).enqueue(new Callback<ApiResponse<ClassRoom>>() {
                @Override
                public void onResponse(Call<ApiResponse<ClassRoom>> call, Response<ApiResponse<ClassRoom>> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddEditClassActivity.this, "Tạo lớp học thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddEditClassActivity.this, "Không thể tạo lớp học", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ClassRoom>> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(AddEditClassActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setLoading(boolean isLoading) {
        if (pbClassForm != null) {
            pbClassForm.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnSaveClass.setEnabled(!isLoading);
        btnCancelClass.setEnabled(!isLoading);
    }
}
