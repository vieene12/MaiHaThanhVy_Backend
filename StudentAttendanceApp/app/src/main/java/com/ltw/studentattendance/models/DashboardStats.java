package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class DashboardStats implements Serializable {
    @SerializedName("totalStudents")
    private int totalStudents;

    @SerializedName("totalClasses")
    private int totalClasses;

    @SerializedName("totalSubjects")
    private int totalSubjects;

    @SerializedName("todaySessions")
    private int todaySessions;

    @SerializedName("attendanceRate")
    private double attendanceRate;

    @SerializedName("attendedSessions")
    private int attendedSessions;

    @SerializedName("absentSessions")
    private int absentSessions;

    @SerializedName("lateSessions")
    private int lateSessions;

    @SerializedName("totalSessions")
    private int totalSessions;

    public DashboardStats() {}

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }

    public int getTotalSubjects() { return totalSubjects; }
    public void setTotalSubjects(int totalSubjects) { this.totalSubjects = totalSubjects; }

    public int getTodaySessions() { return todaySessions; }
    public void setTodaySessions(int todaySessions) { this.todaySessions = todaySessions; }

    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }

    public int getAttendedSessions() { return attendedSessions; }
    public void setAttendedSessions(int attendedSessions) { this.attendedSessions = attendedSessions; }

    public int getAbsentSessions() { return absentSessions; }
    public void setAbsentSessions(int absentSessions) { this.absentSessions = absentSessions; }

    public int getLateSessions() { return lateSessions; }
    public void setLateSessions(int lateSessions) { this.lateSessions = lateSessions; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
}
