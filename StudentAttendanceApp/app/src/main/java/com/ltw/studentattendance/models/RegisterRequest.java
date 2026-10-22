package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("role")
    private String role; // STUDENT or TEACHER

    public RegisterRequest(String fullName, String email, String password, String phoneNumber, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
