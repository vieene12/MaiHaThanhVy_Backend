package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ClassEnrollment implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("classRoomId")
    private int classRoomId;

    @SerializedName("studentId")
    private int studentId;

    @SerializedName("enrolledAt")
    private String enrolledAt;

    @SerializedName("student")
    private Student student;

    @SerializedName("classRoom")
    private ClassRoom classRoom;

    public ClassEnrollment() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClassRoomId() { return classRoomId; }
    public void setClassRoomId(int classRoomId) { this.classRoomId = classRoomId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(String enrolledAt) { this.enrolledAt = enrolledAt; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public ClassRoom getClassRoom() { return classRoom; }
    public void setClassRoom(ClassRoom classRoom) { this.classRoom = classRoom; }
}
