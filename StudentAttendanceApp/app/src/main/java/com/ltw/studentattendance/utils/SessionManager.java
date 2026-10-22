package com.ltw.studentattendance.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, int userId, String email, String name, String role) {
        editor.putString(Constants.KEY_TOKEN, token);
        editor.putInt(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USER_EMAIL, email);
        editor.putString(Constants.KEY_USER_NAME, name);
        editor.putString(Constants.KEY_USER_ROLE, role);
        editor.apply();
    }

    public String getToken() { return prefs.getString(Constants.KEY_TOKEN, null); }
    public int getUserId() { return prefs.getInt(Constants.KEY_USER_ID, -1); }
    public String getUserEmail() { return prefs.getString(Constants.KEY_USER_EMAIL, ""); }
    public String getUserName() { return prefs.getString(Constants.KEY_USER_NAME, ""); }
    public String getUserRole() { return prefs.getString(Constants.KEY_USER_ROLE, ""); }
    public String getFaceData() { return prefs.getString(Constants.KEY_FACE_DATA, null); }

    public void saveFaceData(String faceData) {
        editor.putString(Constants.KEY_FACE_DATA, faceData);
        editor.apply();
    }

    public boolean isLoggedIn() { return getToken() != null; }
    public boolean isAdmin() { return Constants.ROLE_ADMIN.equals(getUserRole()); }
    public boolean isTeacher() { return Constants.ROLE_TEACHER.equals(getUserRole()); }
    public boolean isStudent() { return Constants.ROLE_STUDENT.equals(getUserRole()); }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
