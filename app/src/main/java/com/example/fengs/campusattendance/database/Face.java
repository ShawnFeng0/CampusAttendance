package com.example.fengs.campusattendance.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;

public class Face extends DataSupport{
    private int id; /* 数据库内部ID */
    private GroupDB groupDB; /* 数据库表链接到分组表 一组对多个人 */
    private String faceName; /* 名字 */
    private String faceID; /* 学号 */
    private byte[] featureData; /* 人脸特征信息, 根据人脸识别库提取人脸图片得到 */
    private byte[] faceImageData; /* 人脸图bitmap转换成byte储存 */

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

    /**
     * 从图片数据生成Bitmap数据
     * @return Bitmap格式图
     */
    public Bitmap getFaceImage() {
        return BitmapHandle.byteToBitmap(this.getFaceImageData());
    }

    /**
     * 设置图片信息, 转化成字节流储存
     * @param faceImage 输入的Bitmap格式图
     */
    public void setFaceImage(Bitmap faceImage) {
        this.setFaceImageData(BitmapHandle.bitmapToByte(faceImage));
    }

}
