package com.example.fengs.campusattendance.database;

import com.arcsoft.facerecognition.AFR_FSDKFace;

import org.litepal.crud.DataSupport;

public class Face extends DataSupport{
    private int id;
    private GroupDB groupDB;
    private String studentName;
    private String studentID;
    private byte[] featureData;

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getFeatureData() {
        return featureData;
    }

    public void setFeatureData(byte[] featureData) {
        this.featureData = featureData;
    }

}
