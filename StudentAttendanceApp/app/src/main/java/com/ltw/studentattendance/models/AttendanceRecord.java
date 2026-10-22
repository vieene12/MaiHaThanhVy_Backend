package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;

public class AttendanceRecord {
    @SerializedName("id") private int id;
    @SerializedName("sessionId") private int sessionId;
    @SerializedName("studentId") private int studentId;
    @SerializedName("studentName") private String studentName;
    @SerializedName("studentCode") private String studentCode;
    @SerializedName("checkInTime") private String checkInTime;
    @SerializedName("method") private String method; // MANUAL, QR, FACE, WIFI, NFC
    @SerializedName("status") private String status; // PRESENT, ABSENT, LATE, EXCUSED
    @SerializedName("note") private String note;
    @SerializedName("latitude") private Double latitude;
    @SerializedName("longitude") private Double longitude;
    @SerializedName("createdAt") private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isPresent() { return "PRESENT".equals(status); }
    public boolean isLate() { return "LATE".equals(status); }
    public boolean isAbsent() { return "ABSENT".equals(status); }
}
