package com.example.fengs.campusattendance.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;

public class Face extends DataSupport{
    private int id;
    private GroupDB groupDB;
    private String faceName;
    private String faceID;
    private byte[] featureData;
    private byte[] faceImageData;

    private byte[] getFaceImageData() {
        return faceImageData;
    }

    private void setFaceImageData(byte[] faceImageData) {
        this.faceImageData = faceImageData;
    }

    public String getFaceID() {
        return faceID;
    }

    public void setFaceID(String faceID) {
        this.faceID = faceID;
    }

    public String getFaceName() {
        return faceName;
    }

    public void setFaceName(String faceName) {
        this.faceName = faceName;
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
        return BitmapHandle.byteToBitmap(this.getFaceImageData());
    }

    public void setFaceImage(Bitmap faceImage) {
        this.setFaceImageData(BitmapHandle.bitmapToByte(faceImage));
    }

}
