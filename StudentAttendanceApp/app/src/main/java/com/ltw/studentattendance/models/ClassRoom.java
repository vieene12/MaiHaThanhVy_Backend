package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;

public class ClassRoom {
    @SerializedName("id") private int id;
    @SerializedName("className") private String className;
    @SerializedName("subjectId") private int subjectId;
    @SerializedName("teacherId") private int teacherId;
    @SerializedName("room") private String room;
    @SerializedName("semester") private String semester;
    @SerializedName("schedule") private String schedule;
    @SerializedName("maxStudents") private int maxStudents;
    @SerializedName("subjectName") private String subjectName;
    @SerializedName("teacherName") private String teacherName;
    @SerializedName("studentCount") private int studentCount;
    @SerializedName("isActive") private boolean isActive;
    @SerializedName("createdAt") private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
