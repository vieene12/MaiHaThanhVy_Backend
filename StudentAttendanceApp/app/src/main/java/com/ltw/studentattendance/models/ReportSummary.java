package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ReportSummary implements Serializable {
    @SerializedName("classId")
    private int classId;

    @SerializedName("className")
    private String className;

    @SerializedName("totalSessions")
    private int totalSessions;

    @SerializedName("totalStudents")
    private int totalStudents;

    @SerializedName("presentCount")
    private int presentCount;

    @SerializedName("absentCount")
    private int absentCount;

    @SerializedName("lateCount")
    private int lateCount;

    @SerializedName("attendanceRate")
    private double attendanceRate;

    @SerializedName("studentReports")
    private List<StudentReportItem> studentReports;

    public ReportSummary() {}

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }

    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }

    public List<StudentReportItem> getStudentReports() { return studentReports; }
    public void setStudentReports(List<StudentReportItem> studentReports) { this.studentReports = studentReports; }

    public static class StudentReportItem implements Serializable {
        @SerializedName("studentId")
        private int studentId;

        @SerializedName("studentCode")
        private String studentCode;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("presentCount")
        private int presentCount;

        @SerializedName("absentCount")
        private int absentCount;

        @SerializedName("lateCount")
        private int lateCount;

        @SerializedName("attendanceRate")
        private double attendanceRate;

        public StudentReportItem() {}

        public int getStudentId() { return studentId; }
        public void setStudentId(int studentId) { this.studentId = studentId; }

        public String getStudentCode() { return studentCode; }
        public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public int getPresentCount() { return presentCount; }
        public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

        public int getAbsentCount() { return absentCount; }
        public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

        public int getLateCount() { return lateCount; }
        public void setLateCount(int lateCount) { this.lateCount = lateCount; }

        public double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
    }
}
