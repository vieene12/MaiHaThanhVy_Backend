package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;

public class Student {
    @SerializedName("id") private int id;
    @SerializedName("studentCode") private String studentCode;
    @SerializedName("fullName") private String fullName;
    @SerializedName("email") private String email;
    @SerializedName("dateOfBirth") private String dateOfBirth;
    @SerializedName("gender") private String gender;
    @SerializedName("phoneNumber") private String phoneNumber;
    @SerializedName("address") private String address;
    @SerializedName("avatar") private String avatar;
    @SerializedName("major") private String major;
    @SerializedName("userId") private Integer userId;
    @SerializedName("isActive") private boolean isActive;
    @SerializedName("createdAt") private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
