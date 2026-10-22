package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;

public class CheckInRequest {
    @SerializedName("sessionId")
    private int sessionId;

    @SerializedName("studentId")
    private int studentId;

    @SerializedName("method")
    private String method; // MANUAL, QR, FACE, LOCATION, WIFI

    @SerializedName("status")
    private String status; // PRESENT, ABSENT, LATE, EXCUSED

    @SerializedName("qrCode")
    private String qrCode;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("wifiSsid")
    private String wifiSsid;

    @SerializedName("faceData")
    private String faceData;

    @SerializedName("note")
    private String note;

    public CheckInRequest() {}

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getWifiSsid() { return wifiSsid; }
    public void setWifiSsid(String wifiSsid) { this.wifiSsid = wifiSsid; }

    public String getFaceData() { return faceData; }
    public void setFaceData(String faceData) { this.faceData = faceData; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
