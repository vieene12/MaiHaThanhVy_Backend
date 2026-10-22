package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Subject implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("subjectCode")
    private String subjectCode;

    @SerializedName("subjectName")
    private String subjectName;

    @SerializedName("credits")
    private int credits;

    @SerializedName("description")
    private String description;

    public Subject() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return subjectName != null ? subjectName : "";
    }
}
