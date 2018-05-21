package com.example.fengs.campusattendance;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.example.fengs.campusattendance.dataView.SignInFaceAdapter;
import com.example.fengs.campusattendance.database.BitmapHandle;
import com.example.fengs.campusattendance.database.Face;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.Arrays;
import java.util.List;


public class ClassActivity extends AppCompatActivity {
    private static final String TAG = "ClassActivity";
    private TextureView textureView;
    private SurfaceView surfaceView;
    private ImageView current_face_imageView;
    private TextView cur_face_name_textView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private GroupDB groupDB;
    private String cameraID_front_or_back;
    private RecyclerView recyclerView;
    private SignInFaceAdapter signInFaceAdapter;

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private DrawRectTask drawRectTask;

    private FaceRecognition faceRecognition;

    private byte[] preview_image_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        setSupportActionBar(findViewById(R.id.class_activity_toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        int group_id = getIntent().getIntExtra("groupID", 0);
        cameraID_front_or_back = getIntent().getStringExtra("camera");
        groupDB = DataSupport.find(GroupDB.class, group_id);

        //列表设置
        recyclerView = findViewById(R.id.sign_in_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        signInFaceAdapter = new SignInFaceAdapter(groupDB.getFaces());
        recyclerView.setAdapter(signInFaceAdapter);

        surfaceView = findViewById(R.id.canvas_surfaceView);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        textureView = findViewById(R.id.camera_texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        current_face_imageView = findViewById(R.id.current_face_imageView);
        cur_face_name_textView = findViewById(R.id.cur_face_name_text_view);

        faceRecognition = new FaceRecognition(); //人脸跟踪相关
    }
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera(cameraID_front_or_back);
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        /**
         * 每一帧画面, 回调一次此函数
         * @param surface
         */
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            long time = System.currentTimeMillis();
//            Bitmap bitmap = textureView.getBitmap();
//            current_face_imageView.setImageBitmap(bitmap);

            /* 第一次运行 或 上次任务已经结束  注意每次需new一个实例,新建的任务只能执行一次,否则会出现异常 */
//            if (drawRectTask == null || drawRectTask.getStatus() != AsyncTask.Status.RUNNING) {
//                drawRectTask = new DrawRectTask();
//                drawRectTask.execute(bitmap);
//            }
        }
    };

    private void onImageAvailable(ImageReader reader) {

        Image img = reader.acquireLatestImage();
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        preview_image_data = ImageUtil.getBytesFromImageAsType(img, ImageUtil.NV21);
        List<AFT_FSDKFace> result = faceRecognition.FaceTrackingProcess(preview_image_data, imgWidth, imgHeight);

        /* 第一次运行 或 上次任务已经结束  注意每次需new一个实例,新建的任务只能执行一次,否则会出现异常 */
        if (drawRectTask == null || drawRectTask.getStatus() != AsyncTask.Status.RUNNING) {
            drawRectTask = new DrawRectTask();
            drawRectTask.execute(preview_image_data, imgWidth, imgHeight, !result.isEmpty());
        }

        if (!result.isEmpty()) {

            AFT_FSDKFace faceInfo = FaceRecognition.getMaxFace_AFT(result);
            RectF realRect = new RectF(faceInfo.getRect());
            Matrix matrix = new Matrix();

            //图片镜像并旋转90度, 变为正常尺寸 720 480
            matrix.setScale(-1, 1);
            matrix.postTranslate(imgWidth, 0);
            matrix.postRotate(90, imgWidth/2, imgHeight/2); //旋转90度
            matrix.postTranslate(0,(imgWidth-imgHeight)/2); //中心对齐
            matrix.mapRect(realRect); //矩形框进行矩阵变换

//            float scale = textureView.getWidth() / img.getHeight(); // 840 / 480
            float scale = (float)textureView.getHeight() / img.getWidth(); // 1302 / 720
            matrix.reset(); //新变换
            matrix.postTranslate((textureView.getWidth() - 720) / 2,
                    (textureView.getHeight() - 720) / 2);  //参考参数: matrix.postTranslate((840 - 720) / 2, (1302 - 720) / 2);
            matrix.preScale(scale, scale,
                    720 / 2, 720 / 2);
            matrix.mapRect(realRect);

            //根据矩形的尺寸确定边框的大小
//            float width = realRect.width() / 30 > 10 ? realRect.width() / 30 : 10;
            float width = 5;
            Paint rect_paint = new Paint(); //矩形框的画笔
            rect_paint.setColor(Color.GREEN); //颜色
            rect_paint.setStrokeWidth(width); //线宽
            rect_paint.setStyle(Paint.Style.STROKE); //线条风格

            Canvas rect_canvas = surfaceView.getHolder().lockCanvas();
            rect_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
            rect_canvas.drawRect(realRect, rect_paint); //画矩形框
            surfaceView.getHolder().unlockCanvasAndPost(rect_canvas);
        } else {
            /* 没找到人脸, 清空surfaceView的绘图层 */
            Canvas rect_canvas = surfaceView.getHolder().lockCanvas();
            rect_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
            surfaceView.getHolder().unlockCanvasAndPost(rect_canvas);
        }
        img.close(); //释放图片内存
    }

    private void backup_onImageAvailable(ImageReader reader) {
        Image img = reader.acquireLatestImage();

        preview_image_data = ImageUtil.getBytesFromImageAsType(img, ImageUtil.NV21);
//                Bitmap bitmap = BitmapHandle.byteToBitmap(preview_image_data);
//                current_face_imageView.setImageBitmap(bitmap);
        long time = System.currentTimeMillis();
        Bitmap temp_bitmap = BitmapHandle.rawByteArray2RGBABitmap2(preview_image_data, img.getWidth(), img.getHeight());

        Log.i(TAG, "图像变换调试第一次: " + "宽: " + temp_bitmap.getWidth() + ", 高: " + temp_bitmap.getHeight());  //720  480
        Bitmap newBitmap = Bitmap.createBitmap(textureView.getHeight(),textureView.getHeight(), temp_bitmap.getConfig());

        Canvas canvas = new Canvas(newBitmap);
        Matrix matrix = new Matrix();
        //图片镜像并旋转90度
        matrix.setScale(-1, 1);
        matrix.postTranslate(temp_bitmap.getWidth(), 0);
        matrix.postRotate(90, temp_bitmap.getWidth()/2, temp_bitmap.getHeight()/2);
        matrix.postTranslate(0,(temp_bitmap.getWidth()-temp_bitmap.getHeight())/2);
        canvas.drawBitmap(temp_bitmap, matrix, new Paint());

        Log.i(TAG, "图像变换调试newBitmap变换后: " + "宽: " + newBitmap.getWidth() + ", 高: " + newBitmap.getHeight());  //720  480

        Bitmap newNewBitmap = Bitmap.createBitmap(textureView.getWidth(), textureView.getHeight(), newBitmap.getConfig());

        Matrix newNewMatrix = new Matrix();
        Canvas newNewCanvas = new Canvas(newNewBitmap);
        float sx = (float)textureView.getWidth() / (float)newBitmap.getWidth();
        float sy = (float)textureView.getHeight() / (float)newBitmap.getHeight();
//        float scale = Math.max(sx, sy); //取最大比例就是短边放大到边界, 长边就超出边界；   取最小就是长边放大到边界, 短边在边界内
//        float scale = Math.min(sx, sy); //取最大比例就是短边放大到边界, 长边就超出边界；   取最小就是长边放大到边界, 短边在边界内
        float scale = 840.f / 480.f;
        newNewMatrix.postTranslate((840 - 720) / 2, (1302 - 720) / 2);
        newNewMatrix.preScale(scale, scale,
                720 / 2, 720 / 2);
        newNewCanvas.drawBitmap(newBitmap, newNewMatrix, new Paint());
        Log.i(TAG, "图像变换调试newNewBitmap变换后: " + "宽: " + newNewBitmap.getWidth() + ", 高: " + newNewBitmap.getHeight());  //720  480

        Log.i(TAG, "图像变换调试textureView: " + "宽: " + textureView.getBitmap().getWidth() + ", 高: " + textureView.getBitmap().getHeight());  //720  480
//        newBitmap = textureView.getBitmap();
        runOnUiThread(() -> current_face_imageView.setImageBitmap(newNewBitmap));
        byte[] bitmap_data;
        bitmap_data = Bmp2YUV.getNV21(newNewBitmap.getWidth(), newNewBitmap.getHeight(), newNewBitmap);

        List<AFT_FSDKFace> result = faceRecognition.FaceTrackingProcess(bitmap_data, newNewBitmap.getWidth(), newNewBitmap.getHeight());
//                Log.i(TAG, "createCameraPreview: " + "View宽: " + textureView.getBitmap().getWidth() + ", 高: " + textureView.getBitmap().getHeight());
//                Log.i(TAG, "createCameraPreview: " + "img宽: " + img.getWidth() + ", 高: " + img.getHeight());  //720  480
        img.close();

        if (!result.isEmpty()) {

            AFT_FSDKFace faceRect = FaceRecognition.getMaxFace_AFT(result);
            Log.i(TAG, "faceInfo: " + faceRect.toString());
            //                AFR_FSDKFace faceFeature = FaceRecognition.singleGetFaceFeature(bitmap_data, bitmap.getWidth(), bitmap.getHeight(), faceRect.getRect(), faceRect.getDegree());
//                    Log.i(TAG, "人脸跟踪时间: " + (System.currentTimeMillis() - time));
            Rect rect = faceRect.getRect();
//            int width = rect.width() / 30 > 10 ? rect.width() / 30 : 10;
            int width = 10;
            Paint rect_paint = new Paint();
            rect_paint.setColor(Color.GREEN);
            rect_paint.setStrokeWidth(width);
            rect_paint.setStyle(Paint.Style.STROKE);

            Canvas rect_canvas = surfaceView.getHolder().lockCanvas();
            rect_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
            rect_canvas.drawRect(rect, rect_paint);
            surfaceView.getHolder().unlockCanvasAndPost(rect_canvas);
        } else {
            Canvas rect_canvas = surfaceView.getHolder().lockCanvas();
            rect_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
            surfaceView.getHolder().unlockCanvasAndPost(rect_canvas);
        }
    }
    private class DrawRectTask extends AsyncTask<Object, Void, Face> {

        @Override
        protected Face doInBackground(Object... Objects) {
            byte [] imageByte = (byte[]) Objects[0];
            Face resultFace = null;
            int imgWidth = (int) Objects[1];
            int imgHeight = (int) Objects[2];
            boolean hasResult = (boolean) Objects[3];

//            imageByte = Bmp2YUV.getNV21(textureView.getWidth(), textureView.getHeight(), textureView.getBitmap());
//            int imgWidth = textureView.getWidth();
//            int imgHeight = textureView.getHeight();

            if (hasResult) {
                List<AFD_FSDKFace> result = FaceRecognition.singleFaceDetection(imageByte, imgWidth, imgHeight);
                if (!result.isEmpty()) {
                    AFD_FSDKFace faceInfo = FaceRecognition.getMaxFace_AFD(result);
                    AFR_FSDKFace faceFeature = FaceRecognition.singleGetFaceFeature(imageByte, imgWidth, imgHeight, faceInfo.getRect(), faceInfo.getDegree());
                    AFR_FSDKFace registeredFaceFeature = new AFR_FSDKFace();
                    float maxScore = 0.6f; //相似度确认最小阈值
                    for (Face face : signInFaceAdapter.getFaceList()) {
                        registeredFaceFeature.setFeatureData(face.getFeatureData());
                        float score = FaceRecognition.singleFaceMatching(registeredFaceFeature, faceFeature);
                        if (score > maxScore) {
                            maxScore = score;
                            resultFace = face;
                        }
                        Log.i(TAG, "名字：" + face.getFaceName() +  "   SCORE: " + score);
                    }
                    Log.i(TAG, "------------------------------------------------------------------");
                }
            }
            return resultFace;
        }

        @Override
        protected void onPostExecute(Face face) {
            super.onPostExecute(face);
            int faceIndex = signInFaceAdapter.getFaceList().indexOf(face);
            if (face != null) {
                if (mBackgroundHandler != null) {
                    mBackgroundHandler.removeCallbacks(currentFaceImageHide);
                }
//                faceList.indexOf(face);
//                faceList.remove(face);
                current_face_imageView.setImageBitmap(face.getFaceImage());
                current_face_imageView.setAlpha(1.0f);
                cur_face_name_textView.setAlpha(1.0f);
                cur_face_name_textView.setText(String.format("%s:%s", face.getFaceID(), face.getFaceName()));
                recyclerView.smoothScrollToPosition(faceIndex);
                signInFaceAdapter.removeDataDelayToDisplay(faceIndex, 1000);
            } else {
                current_face_imageView.postDelayed(currentFaceImageHide, 2000);
            }
        }
    }

    Runnable currentFaceImageHide = new Runnable() {
        @Override
        public void run() {
//            mTextView.setAlpha(0.5f);
//            current_face_imageView.setImageResource();
            current_face_imageView.setAlpha(0.5f);
//            current_face_imageView.setImageResource(android.R.color.darker_gray);
            cur_face_name_textView.setText("");
            cur_face_name_textView.setAlpha(0.5f);
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
        }

    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建相机预览
     */
    protected void createCameraPreview() {
        try {
            //设置捕获请求为预览, 还可以设置为拍照录像等
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            imageReader = ImageReader.newInstance(imageDimension.getWidth(), imageDimension.getHeight(),
                    ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(this::onImageAvailable, mBackgroundHandler);
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            //设置预览大小
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder.addTarget(surface); //添加预览画面捕获后的输出平面
            captureRequestBuilder.addTarget(imageReader.getSurface());
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(ClassActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera(String camera_front_or_back) {
        //获得所有摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            String cameraId;
            if (camera_front_or_back.equals("back")) {
                cameraId = manager.getCameraIdList()[0];
            } else {
                cameraId = manager.getCameraIdList()[1];
            }
            //获得某个摄像头的特征，支持的参数
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            //支持的STREAM CONFIGURATION
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            /* 获取预览图片画面的输出尺寸, 参数为输出类型 */
            imageDimension = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), textureView.getWidth(), textureView.getHeight());
            Log.i(TAG, "openCamera: 宽高" + imageDimension.getWidth() + "  " + imageDimension.getHeight());

            /* */
            updateTextureViewSizeCenter();
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ClassActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            //打开相机
            manager.openCamera(cameraId, stateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    /**
     * 选择最合适的尺寸
     * @param outputSizes
     * @param width
     * @param height
     * @return
     */
    private Size chooseOptimalSize(Size[] outputSizes, int width, int height) {
        double preferredRatio = height / (double) width;
        Size currentOptimalSize = outputSizes[0];
        double currentOptimalRatio = currentOptimalSize.getWidth() / (double) currentOptimalSize.getHeight();
        for (Size currentSize : outputSizes) {
            double currentRatio = currentSize.getWidth() / (double) currentSize.getHeight();
            if (Math.abs(preferredRatio - currentRatio) <
                    Math.abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize;
                currentOptimalRatio = currentRatio;
            }
        }
        return currentOptimalSize;
    }
    /**
     * 计算缩放的比例, 使图像无缩放的进行展示
     */
    private void updateTextureViewSizeCenter(){
        float sx = (float)textureView.getWidth() / (float)imageDimension.getHeight() ;
        float sy = (float)textureView.getHeight() / (float)imageDimension.getWidth();
        float scale = Math.max(sx, sy); //取最大比例就是短边放大到边界, 长边就超出边界；   取最小就是长边放大到边界, 短边在边界内
//        float scale = Math.min(sx, sy); //取最大比例就是短边放大到边界, 长边就超出边界；   取最小就是长边放大到边界, 短边在边界内
        Matrix matrix = new Matrix();
        RectF srcRect = new RectF(0, 0, textureView.getWidth(), textureView.getHeight());
        RectF dstRect = new RectF(0, 0, imageDimension.getHeight(), imageDimension.getWidth());
        dstRect.offset(srcRect.centerX() - dstRect.centerX(), srcRect.centerY() - dstRect.centerY());
        matrix.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.FILL);
        //图像的宽和高为横屏数据
        matrix.postScale(scale, scale,
                textureView.getWidth()/2, textureView.getHeight()/2);
        textureView.setTransform(matrix);
    }


    /**
     *
     * @param imageWidth
     * @param imageHeight
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private Matrix imageTransform(int imageWidth, int imageHeight, int viewHeight, int viewWidth) {
//        Matrix matrix = new Matrix();
//        RectF imageRect = new RectF(0, 0, imageWidth, imageHeight);
//        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
//        float centerX = imageRect.centerX();
//        float centerY = imageRect.centerY();
//        float scale = Math.max(
//            (float) imageHeight / viewHeight,
//            (float) imageWidth / viewWidth);
//        imageRect.offset(centerX - imageRect.centerX(), centerY - imageRect.centerY());
//        matrix.setRectToRect(imageRect, viewRect, Matrix.ScaleToFit.FILL);
//        matrix.postScale(scale, -scale, viewRect.centerX(), viewRect.centerY());
//        matrix.postRotate(270, viewRect.centerX(), viewRect.centerY());


        Matrix matrix = new Matrix();
        //图片镜像并旋转90度
        matrix.setScale(-1, 1);
        matrix.postTranslate(imageWidth, 0);
        matrix.postRotate(90 ,imageWidth/2,imageHeight/2);
        matrix.postTranslate(0,(imageWidth-imageHeight)/2);

        return matrix;
    }

    /**
     * google camera2 官方缩放例子
     * @param viewWidth
     * @param viewHeight
     * @param size
     */
    private void configureTransform(int viewWidth, int viewHeight, Size size) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, size.getHeight(), size.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / size.getHeight(),
                    (float) viewWidth / size.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    /**
     * 更新预览画面
     */
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraCaptureSessions.close();
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(ClassActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView != null) {
            textureView.setVisibility(View.VISIBLE);
            if (textureView.isAvailable()) {
                openCamera(cameraID_front_or_back);
            } else {
                textureView.setSurfaceTextureListener(textureListener);
            }
        }
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        if (textureView != null) {
            textureView.setVisibility(View.INVISIBLE);
        }
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceRecognition != null) {
            faceRecognition.FinishFaceTracking();
        }
    }

    /**
     * 菜单选项
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //左上角返回键
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
