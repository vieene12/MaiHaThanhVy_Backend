package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id") private int id;
    @SerializedName("email") private String email;
    @SerializedName("fullName") private String fullName;
    @SerializedName("role") private String role;
    @SerializedName("phoneNumber") private String phoneNumber;
    @SerializedName("avatar") private String avatar;
    @SerializedName("isEmailVerified") private boolean isEmailVerified;
    @SerializedName("createdAt") private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public boolean isEmailVerified() { return isEmailVerified; }
    public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
