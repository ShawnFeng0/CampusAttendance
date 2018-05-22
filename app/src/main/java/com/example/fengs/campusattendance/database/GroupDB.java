package com.example.fengs.campusattendance.database;

import android.graphics.Bitmap;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public class GroupDB extends DataSupport {
    private int id; //数据库内部储存ID
    private String groupCourseStr; //课程名
    private String groupClassStr; //班级名
    private String adminPhoneNumberStr; //管理员电话号码
    private List<Face> faces; //人脸数据
    private byte[] groupImageData; //课程图片数据

    public GroupDB(){
        faces = new ArrayList<>();
    }

    public String getGroupCourseStr() {
        return groupCourseStr;
    }

    public void setGroupCourseStr(String groupCourseStr) {
        this.groupCourseStr = groupCourseStr;
    }

    public String getGroupClassStr() {
        return groupClassStr;
    }

    public void setGroupClassStr(String groupClassStr) {
        this.groupClassStr = groupClassStr;
    }

    public List<Face> getFaces() {
        /* 从数据库读取该组的人脸数据 */
        List<Face> faceList = DataSupport.where("groupdb_id = ?", String.valueOf(id)).find(Face.class);
        if (faceList.size() != 0) {
            return faces = faceList;
        } else {
            return faces;
        }
    }

    /**
     * 得到组字符串表达信息
     */
    @Override
    public String toString() {
        return String.format("%s-%s (课-班)", this.getGroupCourseStr(), this.getGroupClassStr());
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

    public String getAdminPhoneNumberStr() {
        return adminPhoneNumberStr;
    }

    public void setAdminPhoneNumberStr(String adminPhoneNumberStr) {
        this.adminPhoneNumberStr = adminPhoneNumberStr;
    }
}
