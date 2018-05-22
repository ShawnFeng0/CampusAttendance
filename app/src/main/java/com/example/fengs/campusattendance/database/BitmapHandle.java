package com.example.fengs.campusattendance.database;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BitmapHandle {

    /**
     * bitmap 转换为字节流数据, 一般用来保存到数据库使用
     * @param bitmap 要转换的bitmap
     * @return 转换之后的字节流, 方便保存到数据库
     */
    public static byte[] bitmapToByte(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } else {
            return null;
        }
    }

    /**
     * 字节流转换为bitmap格式图, 用来从数据库提取bitmap数据
     * @param imageData 之前被bitmap转换生成的字节流
     * @return 转换之后的bitmap
     */
    public static Bitmap byteToBitmap(byte[] imageData) {
        if (imageData != null && imageData.length != 0) {
            return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        } else {
            return null;
        }
    }

    /**
     * YUV420888 to bitmap, 从camera2库中读出的数据就使yuv420_888格式, 需要转换为bitmap方便查看
     * @param data 图像原始数据
     * @param width 图像宽度
     * @param height 图像高度
     * @return 生成的bitmap
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
     * bitmap转换为NV21格式字节流, NV21字节流是虹软人脸识别库的要求的图片字节格式
     * @param inputWidth 图片宽度
     * @param inputHeight 图片高度
     * @param scaled 原始图像,从相机接口得到
     * @return 转换后的NV21
     */
    public static byte[] bitmapToNV21Byte(int inputWidth, int inputHeight, Bitmap scaled) {
        int [] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

//        byte [] yuv = new byte[inputWidth*inputHeight*3/2];
        /*
        encodeYUV420SP throws ArrayIndexOutOfRange Exception when inputWidth or inputHeight is an odd number.
        Declaring the yuv array as byte [] yuv = new byte[inputHeight * inputWidth + 2 * (int) Math.ceil(inputHeight/2.0) *(int) Math.ceil(inputWidth/2.0)];
        solved the issue
         */
        byte [] yuv = new byte[inputHeight * inputWidth + 2 * (int) Math.ceil(inputHeight/2.0) *(int) Math.ceil(inputWidth/2.0)];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

//        scaled.recycle();

        return yuv;
    }

    /**
     * 跟上面的bitmap转nv21函数结合用
     * @param yuv420sp 生成nv21数据
     * @param argb 原始图像数据
     * @param width 图像宽度
     * @param height 图像高度
     */
    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff);

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    /**
     * 得到一个真实的人脸矩形框
     * 人脸识别引擎得到的人脸矩形框比较小, 此函数可把矩形框稍微放大一点, 方便保存后查看
     * @param src_bitmap 原始的bitmap图像
     * @param rect 原始的矩形框
     * @return 放大后的矩形框
     */
    public static Rect getBigFaceRect(Bitmap src_bitmap, Rect rect) {
        //特别是上边框比较小
        if (rect.top - rect.height()/4 > 0) { //如果上边框还能再往上放大1/4
            rect.top -= rect.height()/4; //放大1/4
            if (rect.left - rect.width()/8 > 0) { //左边框能再往左1/8, 就放大1/8
                rect.left -= rect.width()/8;
            } else {
                rect.left = 0; //否则直接放大到边界
            }
            if (rect.right + rect.width()/8 < src_bitmap.getWidth()) { //右边框放大1/8, 不能超过原始图像边界
                rect.right += rect.width()/8;
            } else {
                rect.right = src_bitmap.getWidth(); //否则直接放大到边界
            }
        } else {
            rect.top = 0; //否则直接放大到边界
        }

        if (rect.bottom + rect.height()/8 < src_bitmap.getHeight()) { //下边框放大1/8
            rect.bottom += rect.height()/8;
        } else {
            rect.bottom = src_bitmap.getHeight(); //否则直接放大到边界
        }

        return rect;
    }

    /**
     * 保存图片上的矩形框为bitmap, 指定宽度和高度进行保存, 一般用来保存人脸的矩形框图片
     * @param src_bitmap 原始bitmap图
     * @param rect 图像上的矩形框
     * @param targetWidth 转换后的目标宽度
     * @param targetHeight 转换后的目标高度
     * @return 返回的目标矩形框里的图像
     */
    public static Bitmap getRectZoomBitmap(Bitmap src_bitmap, Rect rect, float targetWidth, float targetHeight) {
        float scaleWidth = targetWidth / rect.width();
        float scaleHeight = targetHeight / rect.height();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(src_bitmap,
                rect.left, rect.top, rect.width(), rect.height(), matrix, true);
    }

    /**
     * 得到缩放后的Bitmap
     * @param src_bitmap 原始图
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 新图
     */
    public static Bitmap getZoomBitmap(Bitmap src_bitmap, int targetWidth, int targetHeight) {
        float scaleWidth = (float)targetWidth / src_bitmap.getWidth();
        float scaleHeight = (float)targetHeight / src_bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(src_bitmap,
                0, 0, src_bitmap.getWidth(), src_bitmap.getHeight(), matrix, true);
    }

    /**
     * 调用系统拍照程序
     */
    public static Uri imageCapture(AppCompatActivity activity, int requestCode) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //拍照Intent
        ContentValues values = new ContentValues(1); //数据容器
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); //数据类型和格式
        Uri imageFileUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); //得到数据的地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri); //设置图片的输出位置
        activity.startActivityForResult(intent, requestCode); //启动拍照程序, 拍照后启动读取函数onActivityResult()
        return imageFileUri;
    }


    /**
     * yuv420p:  yyyyyyyyuuvv
     * yuv420sp: yyyyyyyyuvuv
     * nv21:     yyyyyyyyvuvu
     */
    public static final int YUV420P = 0;
    public static final int YUV420SP = 1;
    public static final int NV21 = 2;
    private static final String TAG = "ImageUtil";

    /**
     * 从image得到指定类型的字节流
     * 此方法内注释以640*480为例
     * 未考虑CropRect的
     * @param image 要转化的图片
     * @param type 格式
     * @return 返回的字节流
     */
    public static byte[] getBytesFromImageAsType(Image image, int type) {
        try {
            //获取源数据，如果是YUV格式的数据planes.length = 3
            //plane[i]里面的实际数据可能存在byte[].length <= capacity (缓冲区总大小)
            final Image.Plane[] planes = image.getPlanes();

            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
            // 所以我们只取width部分
            int width = image.getWidth();
            int height = image.getHeight();

            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            byte[] yuvBytes = new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
            //目标数组的装填到的位置
            int dstIndex = 0;

            //临时存储uv数据的
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            int pixelsStride, rowStride;
            for (int i = 0; i < planes.length; i++) {
                pixelsStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();

                ByteBuffer buffer = planes[i].getBuffer();

                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                int srcIndex = 0;
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yuvBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }

//            image.close();

            //根据要求的结果类型进行填充
            switch (type) {
                case YUV420P:
                    System.arraycopy(uBytes, 0, yuvBytes, dstIndex, uBytes.length);
                    System.arraycopy(vBytes, 0, yuvBytes, dstIndex + uBytes.length, vBytes.length);
                    break;
                case YUV420SP:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = uBytes[i];
                        yuvBytes[dstIndex++] = vBytes[i];
                    }
                    break;
                case NV21:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = vBytes[i];
                        yuvBytes[dstIndex++] = uBytes[i];
                    }
                    break;
            }
            return yuvBytes;
        } catch (final Exception e) {
            if (image != null) {
//                image.close();
            }
            Log.i(TAG, e.toString());
        }
        return null;
    }
}
