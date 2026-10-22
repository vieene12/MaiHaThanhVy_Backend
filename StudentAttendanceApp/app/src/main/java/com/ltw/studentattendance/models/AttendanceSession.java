package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AttendanceSession implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("classRoomId")
    private int classRoomId;

    @SerializedName("teacherId")
    private int teacherId;

    @SerializedName("sessionDate")
    private String sessionDate;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("qrCode")
    private String qrCode;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("wifiSsid")
    private String wifiSsid;

    @SerializedName("note")
    private String note;

    @SerializedName("isActive")
    private boolean isActive = true;

    @SerializedName("className")
    private String className;

    @SerializedName("classRoom")
    private ClassRoom classRoom;

    public AttendanceSession() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClassRoomId() { return classRoomId; }
    public void setClassRoomId(int classRoomId) { this.classRoomId = classRoomId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public String getSessionDate() { return sessionDate; }
    public void setSessionDate(String sessionDate) { this.sessionDate = sessionDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getWifiSsid() { return wifiSsid; }
    public void setWifiSsid(String wifiSsid) { this.wifiSsid = wifiSsid; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getClassName() {
        if (className != null && !className.isEmpty()) return className;
        if (classRoom != null) return classRoom.getClassName();
        return "";
    }
    public void setClassName(String className) { this.className = className; }

    public ClassRoom getClassRoom() { return classRoom; }
    public void setClassRoom(ClassRoom classRoom) { this.classRoom = classRoom; }
}
