package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class LoginResponse implements Serializable {
    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    @SerializedName("role")
    private String role;

    @SerializedName("studentId")
    private Integer studentId;

    @SerializedName("teacherId")
    private Integer teacherId;

    public LoginResponse() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
}
