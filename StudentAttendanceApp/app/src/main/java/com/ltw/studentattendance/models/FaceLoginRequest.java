package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;

public class FaceLoginRequest {
    @SerializedName("faceData")
    private String faceData;

    @SerializedName("studentCode")
    private String studentCode;

    public FaceLoginRequest(String faceData, String studentCode) {
        this.faceData = faceData;
        this.studentCode = studentCode;
    }

    public String getFaceData() { return faceData; }
    public void setFaceData(String faceData) { this.faceData = faceData; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
}
