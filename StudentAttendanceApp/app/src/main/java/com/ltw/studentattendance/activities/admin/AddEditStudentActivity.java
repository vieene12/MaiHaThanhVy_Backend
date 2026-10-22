package com.ltw.studentattendance.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.api.ApiClient;
import com.ltw.studentattendance.api.ApiResponse;
import com.ltw.studentattendance.api.ApiService;
import com.ltw.studentattendance.models.Student;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditStudentActivity extends AppCompatActivity {

    private ImageButton btnBackAddStudent;
    private TextView tvAddEditStudentTitle;
    private TextInputEditText etStudentCode, etStudentName, etStudentEmail, etStudentPhone, etStudentMajor, etStudentAddress;
    private RadioGroup rgGender;
    private RadioButton rbGenderMale, rbGenderFemale, rbGenderOther;
    private Button btnCancelStudent, btnSaveStudent;
    private ProgressBar pbStudentForm;

    private ApiService apiService;
    private Student editingStudent = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_student);

        apiService = ApiClient.getApiService(this);

        initViews();
        checkEditMode();
        setupEvents();
    }

    private void initViews() {
        btnBackAddStudent = findViewById(R.id.btnBackAddStudent);
        tvAddEditStudentTitle = findViewById(R.id.tvAddEditStudentTitle);
        etStudentCode = findViewById(R.id.etStudentCode);
        etStudentName = findViewById(R.id.etStudentName);
        etStudentEmail = findViewById(R.id.etStudentEmail);
        etStudentPhone = findViewById(R.id.etStudentPhone);
        etStudentMajor = findViewById(R.id.etStudentMajor);
        etStudentAddress = findViewById(R.id.etStudentAddress);
        rgGender = findViewById(R.id.rgGender);
        rbGenderMale = findViewById(R.id.rbGenderMale);
        rbGenderFemale = findViewById(R.id.rbGenderFemale);
        rbGenderOther = findViewById(R.id.rbGenderOther);
        btnCancelStudent = findViewById(R.id.btnCancelStudent);
        btnSaveStudent = findViewById(R.id.btnSaveStudent);
        pbStudentForm = findViewById(R.id.pbStudentForm);
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("student")) {
            editingStudent = (Student) getIntent().getSerializableExtra("student");
            if (editingStudent != null) {
                isEditMode = true;
                tvAddEditStudentTitle.setText(R.string.edit_student);
                etStudentCode.setText(editingStudent.getStudentCode());
                etStudentName.setText(editingStudent.getFullName());
                etStudentEmail.setText(editingStudent.getEmail());
                etStudentPhone.setText(editingStudent.getPhoneNumber());
                etStudentMajor.setText(editingStudent.getMajor());
                etStudentAddress.setText(editingStudent.getAddress());

                String gender = editingStudent.getGender();
                if ("Nữ".equalsIgnoreCase(gender) || "Female".equalsIgnoreCase(gender)) {
                    rbGenderFemale.setChecked(true);
                } else if ("Khác".equalsIgnoreCase(gender) || "Other".equalsIgnoreCase(gender)) {
                    rbGenderOther.setChecked(true);
                } else {
                    rbGenderMale.setChecked(true);
                }
            }
        }
    }

    private void setupEvents() {
        btnBackAddStudent.setOnClickListener(v -> finish());
        btnCancelStudent.setOnClickListener(v -> finish());
        btnSaveStudent.setOnClickListener(v -> performSave());
    }

    private void performSave() {
        String code = etStudentCode.getText() != null ? etStudentCode.getText().toString().trim() : "";
        String name = etStudentName.getText() != null ? etStudentName.getText().toString().trim() : "";
        String email = etStudentEmail.getText() != null ? etStudentEmail.getText().toString().trim() : "";
        String phone = etStudentPhone.getText() != null ? etStudentPhone.getText().toString().trim() : "";
        String major = etStudentMajor.getText() != null ? etStudentMajor.getText().toString().trim() : "";
        String address = etStudentAddress.getText() != null ? etStudentAddress.getText().toString().trim() : "";

        if (TextUtils.isEmpty(code)) {
            etStudentCode.setError(getString(R.string.error_field_required));
            etStudentCode.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            etStudentName.setError(getString(R.string.error_field_required));
            etStudentName.requestFocus();
            return;
        }

        String gender = "Nam";
        if (rbGenderFemale.isChecked()) gender = "Nữ";
        else if (rbGenderOther.isChecked()) gender = "Khác";

        setLoading(true);

        Student student = isEditMode ? editingStudent : new Student();
        student.setStudentCode(code);
        student.setFullName(name);
        student.setEmail(email);
        student.setPhoneNumber(phone);
        student.setMajor(major);
        student.setAddress(address);
        student.setGender(gender);

        if (isEditMode) {
            apiService.updateStudent(student.getId(), student).enqueue(new Callback<ApiResponse<Student>>() {
                @Override
                public void onResponse(Call<ApiResponse<Student>> call, Response<ApiResponse<Student>> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddEditStudentActivity.this, "Cập nhật sinh viên thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddEditStudentActivity.this, "Không thể cập nhật sinh viên", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(AddEditStudentActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.createStudent(student).enqueue(new Callback<ApiResponse<Student>>() {
                @Override
                public void onResponse(Call<ApiResponse<Student>> call, Response<ApiResponse<Student>> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddEditStudentActivity.this, "Thêm sinh viên thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddEditStudentActivity.this, "Không thể thêm sinh viên", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(AddEditStudentActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setLoading(boolean isLoading) {
        if (pbStudentForm != null) {
            pbStudentForm.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnSaveStudent.setEnabled(!isLoading);
        btnCancelStudent.setEnabled(!isLoading);
    }
}
