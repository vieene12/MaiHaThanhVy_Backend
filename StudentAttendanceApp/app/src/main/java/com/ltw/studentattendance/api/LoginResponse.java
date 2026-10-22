package com.ltw.studentattendance.api;

import com.google.gson.annotations.SerializedName;
import com.ltw.studentattendance.models.User;

public class LoginResponse {
    @SerializedName("token") private String token;
    @SerializedName("user") private User user;
    @SerializedName("role") private String role;
    @SerializedName("requiresOtp") private boolean requiresOtp;
    @SerializedName("otpType") private String otpType;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isRequiresOtp() { return requiresOtp; }
    public void setRequiresOtp(boolean requiresOtp) { this.requiresOtp = requiresOtp; }
    public String getOtpType() { return otpType; }
    public void setOtpType(String otpType) { this.otpType = otpType; }
}
