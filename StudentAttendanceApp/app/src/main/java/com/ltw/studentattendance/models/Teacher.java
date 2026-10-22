package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Teacher implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("teacherCode")
    private String teacherCode;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("department")
    private String department;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("userId")
    private Integer userId;

    public Teacher() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTeacherCode() { return teacherCode; }
    public void setTeacherCode(String teacherCode) { this.teacherCode = teacherCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
