package com.example.fengs.campusattendance.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BitmapHandle {
    public static byte[] bitmapToByte(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } else {
            return null;
        }
    }

    public static Bitmap byteToBitmap(byte[] imageData) {
        if (imageData != null && imageData.length != 0) {
            return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        } else {
            return null;
        }
    }
}
