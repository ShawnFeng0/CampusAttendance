package com.example.fengs.campusattendance.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

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

    /**
     * YUV420888 to bitmap
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static Bitmap rawByteArray2RGBABitmap2(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];

        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0 , width, 0, 0, width, height);
        return bmp;
    }

    /**
     * 得到一个真实的人脸矩形框
     * 人脸识别引擎得到的人脸矩形框比较小, 此函数把矩形框稍微放大一点
     * @param src_bitmap
     * @param rect
     * @return
     */
    public static Rect getRealFaceRect(Bitmap src_bitmap, Rect rect) {
        //特别是上边框比较小
        if (rect.top - rect.height()/4 > 0) {
            rect.top -= rect.height()/4;
            if (rect.left - rect.width()/8 > 0) {
                rect.left -= rect.width()/8;
            } else {
                rect.left = 0;
            }
            if (rect.right + rect.width()/8 < src_bitmap.getWidth()) {
                rect.right += rect.width()/8;
            } else {
                rect.right = src_bitmap.getWidth();
            }
        } else {
            rect.top = 0;
        }

        if (rect.bottom + rect.height()/8 < src_bitmap.getHeight()) {
            rect.bottom += rect.height()/8;
        } else {
            rect.bottom = src_bitmap.getHeight();
        }

        return rect;
    }

    /**
     * 保存人脸图片, 缩放原图
     * @param src_bitmap
     * @param rect
     */
    public static Bitmap getFaceImage(Bitmap src_bitmap, Rect rect, float targetWidth, float targetHeight) {
        float scaleWidth = targetWidth / rect.width();
        float scaleHeight = targetHeight / rect.height();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(src_bitmap,
                rect.left, rect.top, rect.width(), rect.height(), matrix, true);
    }
}
