package com.example.fengs.campusattendance.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;

public class Face extends DataSupport{
    private int id;
    private GroupDB groupDB;
    private String studentName;
    private String studentID;
    private byte[] featureData;
    private byte[] faceImageData;

    public byte[] getFaceImageData() {
        return faceImageData;
    }

    public void setFaceImageData(byte[] faceImageData) {
        this.faceImageData = faceImageData;
    }

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

    public Bitmap getFaceImage() {
        return byteToBitmap(this.getFaceImageData());
    }

    public void setFaceImage(Bitmap faceImage) {
        this.setFaceImageData(bitmapToByte(faceImage));
    }

    private byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private Bitmap byteToBitmap(byte[] imageData) {
        if (imageData.length != 0) {
            return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        } else {
            return null;
        }
    }
}
