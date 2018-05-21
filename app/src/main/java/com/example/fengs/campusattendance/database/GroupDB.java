package com.example.fengs.campusattendance.database;

import android.graphics.Bitmap;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public class GroupDB extends DataSupport {
    private int id;
    private String groupCourse;
    private String groupName;
    private String adminPhoneNumber;
    private List<Face> faces;
    private byte[] groupImageData;

    public GroupDB(){
        faces = new ArrayList<>();
    }

    public String getGroupCourse() {
        return groupCourse;
    }

    public void setGroupCourse(String groupCourse) {
        this.groupCourse = groupCourse;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Face> getFaces() {
        List<Face> faceList = DataSupport.where("groupdb_id = ?", String.valueOf(id)).find(Face.class);
        if (faceList.size() != 0) {
            return faces = faceList;
        } else {
            return faces;
        }
    }

    public String toString() {
        return String.format("%s-%s (课-班)", this.getGroupCourse(), this.getGroupName());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private byte[] getGroupImageData() {
        return groupImageData;
    }

    private void setGroupImageData(byte[] groupImageData) {
        this.groupImageData = groupImageData;
    }

    public Bitmap getGroupImage() {
        return BitmapHandle.byteToBitmap(this.getGroupImageData());
    }

    public void setGroupImage(Bitmap groupImage) {
        this.setGroupImageData(BitmapHandle.bitmapToByte(groupImage));
    }

    public String getAdminPhoneNumber() {
        return adminPhoneNumber;
    }

    public void setAdminPhoneNumber(String adminPhoneNumber) {
        this.adminPhoneNumber = adminPhoneNumber;
    }
}
