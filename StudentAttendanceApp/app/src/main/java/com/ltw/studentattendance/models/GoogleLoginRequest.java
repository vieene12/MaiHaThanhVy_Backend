package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;

public class GoogleLoginRequest {
    @SerializedName("idToken")
    private String idToken;

    @SerializedName("email")
    private String email;

    @SerializedName("fullName")
    private String fullName;

    public GoogleLoginRequest(String idToken, String email, String fullName) {
        this.idToken = idToken;
        this.email = email;
        this.fullName = fullName;
    }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
