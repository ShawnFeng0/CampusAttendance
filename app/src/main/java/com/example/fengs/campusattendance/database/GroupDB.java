package com.example.fengs.campusattendance.database;

import android.graphics.Bitmap;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public class GroupDB extends DataSupport {
    private int id;
    private String groupID;
    private String groupName;
    private List<Face> faces;
    private byte[] groupImageData;

    public GroupDB(){
        faces = new ArrayList<>();
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
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
        return this.getGroupID() + ":" + this.getGroupName();
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
}
