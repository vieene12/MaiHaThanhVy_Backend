package com.ltw.studentattendance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.activities.admin.AdminDashboardActivity;
import com.ltw.studentattendance.activities.student.StudentDashboardActivity;
import com.ltw.studentattendance.activities.teacher.TeacherDashboardActivity;
import com.ltw.studentattendance.utils.Constants;
import com.ltw.studentattendance.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginAndNavigate, 1500);
    }

    private void checkLoginAndNavigate() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isLoggedIn()) {
            String role = sessionManager.getRole();
            Intent intent;
            if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else if (Constants.ROLE_TEACHER.equalsIgnoreCase(role)) {
                intent = new Intent(this, TeacherDashboardActivity.class);
            } else {
                intent = new Intent(this, StudentDashboardActivity.class);
            }
            startActivity(intent);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
