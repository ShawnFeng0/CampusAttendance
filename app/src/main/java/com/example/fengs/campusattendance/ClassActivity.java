package com.example.fengs.campusattendance;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
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
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 签到界面
 */
public class ClassActivity extends AppCompatActivity {
    private static final String TAG = "ClassActivity";
    private TextureView textureView; //图像显示层
    private SurfaceView surfaceView; //矩形显示层
    private ImageView current_face_imageView; //签到成功后左上角的图片
    private TextView cur_face_name_textView; //签到成功后左上角的提示信息

    private String cameraID_front_or_back; //前置还是后置
    private GroupDB groupDB; //当前组
    private RecyclerView recyclerView; //未签到的循环列表
    private SignInFaceAdapter signInFaceAdapter; //列表对应的适配器

    protected CameraDevice cameraDevice; //摄像头设备
    protected CameraCaptureSession cameraCaptureSessions; //捕获所需
    protected CaptureRequest.Builder captureRequestBuilder; //捕获所需
    private Size imageDimension; //预览的分辨率
    private ImageReader imageReader; //从摄像头读到的图像数据
    private static final int REQUEST_CAMERA_PERMISSION = 200; //需要摄像头权限的请求码
    private Handler mBackgroundHandler; //后台线程的接口
    private HandlerThread mBackgroundThread; //后台线程
    private DrawRectTask drawRectTask; //画矩形的任务

    private FaceRecognition faceRecognition; //人脸跟踪的接口

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        setSupportActionBar(findViewById(R.id.class_activity_toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        int group_id = getIntent().getIntExtra("groupID", 0); //得到传进来的分组数据库ID
        groupDB = DataSupport.find(GroupDB.class, group_id); //根据ID得到数据库中对应的分组
        cameraID_front_or_back = getIntent().getStringExtra("camera"); //前置还是后置摄像头

        //列表设置
        recyclerView = findViewById(R.id.sign_in_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //纵向显示
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //纵向分割线

        signInFaceAdapter = new SignInFaceAdapter(groupDB.getFaces());
        recyclerView.setAdapter(signInFaceAdapter);

        surfaceView = findViewById(R.id.canvas_surfaceView); //得到对应的显示界面
        surfaceView.setZOrderOnTop(true); //设置到顶层
        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT); //透明格式

        textureView = findViewById(R.id.camera_texture); //摄像头信息获取
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener); //设置动作监听器

        current_face_imageView = findViewById(R.id.current_face_imageView); //识别到的人脸图
        cur_face_name_textView = findViewById(R.id.cur_face_name_text_view); //识别到的人脸信息

        Button createNotSignInButton = findViewById(R.id.create_list_button); //生成未签到列表的按钮
        createNotSignInButton.setOnClickListener(this::onClick);
        faceRecognition = new FaceRecognition(); //人脸跟踪相关
    }

    //textureListener 动作监听, 创建, 大小改变, 销毁, 更新, 都有对应的执行函数
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera(cameraID_front_or_back); //创建之后打开对应摄像头
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
        }
    };

    //获取到一帧图像
    private void onImageAvailable(ImageReader reader) {

        Image img = reader.acquireLatestImage(); //得到最新一帧的图像
        int imgWidth = img.getWidth(); //图像数据宽
        int imgHeight = img.getHeight(); //图像数据高

        byte[] preview_image_data = BitmapHandle.getBytesFromImageAsType(img, BitmapHandle.NV21); //转换为所需格式的字节流数据
        List<AFT_FSDKFace> result = faceRecognition.FaceTrackingProcess(preview_image_data, imgWidth, imgHeight); //人脸跟踪生成矩形框

        /* 第一次运行 或 上次任务已经结束  注意每次需new一个实例,新建的任务只能执行一次,否则会出现异常 */
        if (drawRectTask == null || drawRectTask.getStatus() != AsyncTask.Status.RUNNING) {
            drawRectTask = new DrawRectTask();
            drawRectTask.execute(preview_image_data, imgWidth, imgHeight, !result.isEmpty()); //运行提取特征, 以及匹配和显示匹配到的已有人脸的任务
        }

        //识别有结果
        if (!result.isEmpty()) {

            AFT_FSDKFace faceInfo = FaceRecognition.getMaxFace_AFT(result); //得到最大的矩形框
            RectF realRect = new RectF(faceInfo.getRect()); //复制为浮点型数据进行矩阵变换
            Matrix matrix = new Matrix();
            //图片镜像并旋转90度, 变为正常尺寸 720 480
            matrix.setScale(-1, 1); //X轴反向
            matrix.postTranslate(imgWidth, 0); //移动回原来的位置
            matrix.postRotate(90, imgWidth / 2, imgHeight / 2); //旋转90度
            matrix.postTranslate(0, (imgWidth - imgHeight) / 2); //中心对齐
            matrix.mapRect(realRect); //矩形框进行矩阵变换

            //以下变换得到跟textureView图像一致的尺寸以便于矩形框和图像匹配
//            float scale = textureView.getWidth() / img.getHeight(); // 840 / 480
            float scale = (float) textureView.getHeight() / img.getWidth(); // 1302 / 720
            matrix.reset(); //新变换
            matrix.postTranslate((textureView.getWidth() - 720) / 2,
                    (textureView.getHeight() - 720) / 2);  //参考参数: matrix.postTranslate((840 - 720) / 2, (1302 - 720) / 2);
            matrix.preScale(scale, scale,
                    720 / 2, 720 / 2);
            matrix.mapRect(realRect); //坐标进行矩阵变换

            //根据矩形的尺寸确定边框的大小
//            float width = realRect.width() / 30 > 10 ? realRect.width() / 30 : 10;
            float width = 5; //矩形边框宽度
            Paint rect_paint = new Paint(); //矩形框的画笔
            rect_paint.setColor(Color.GREEN); //颜色
            rect_paint.setStrokeWidth(width); //线宽
            rect_paint.setStyle(Paint.Style.STROKE); //线条风格

            Canvas rect_canvas = surfaceView.getHolder().lockCanvas(); //锁定获得当前画布
            rect_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
            rect_canvas.drawRect(realRect, rect_paint); //画矩形框
            surfaceView.getHolder().unlockCanvasAndPost(rect_canvas); //加锁显示新画布
        } else {
            /* 没找到人脸, 清空surfaceView的绘图层 */
            Canvas rect_canvas = surfaceView.getHolder().lockCanvas();
            rect_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
            surfaceView.getHolder().unlockCanvasAndPost(rect_canvas);
        }
        img.close(); //释放图片内存
    }

    //生成未签到人员列表
    private void onClick(View v) {
        List<String> faceStrings = new ArrayList<>();
        for (Face face : signInFaceAdapter.getFaceList()) { //遍历所有未签到的人员信息
            faceStrings.add(String.format("学号: %s, 姓名:%s", face.getFaceID(), face.getFaceName()));
        }

        //未签到列表为空, 全部签到成功
        if (faceStrings.size() == 0) {
            faceStrings.add("今天全体都到齐了哦!");
        }

        //生成未签到人员列表框
//        String[] faceStringsArray = faceStrings.toArray(new String[faceStrings.size()]);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("未签到人员信息:") //标题
                .setItems(faceStrings.toArray(new String[faceStrings.size()]), (dialog1, which) -> {
                }) //设置列表项
                .setPositiveButton("发送短信", null) //确定按钮, 动作在下面注册
                .setNegativeButton("取消", null) //取消按钮, 动作再下面注册
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((View v1) -> {
            final View layout = getLayoutInflater().inflate(R.layout.dialog_send_sms, null);
            EditText editTextPhone = layout.findViewById(R.id.edit_view_phone_number_for_send); //电话号码栏
            EditText editTextComment = layout.findViewById(R.id.edit_view_comment_to_send); //备注栏
            String phoneNumber = groupDB.getAdminPhoneNumberStr(); //得到当前分组管理员电话
            if (phoneNumber == null || phoneNumber.isEmpty()) { //如果分组中没有管理员电话, 就用本机号码代替
                TelephonyManager tm = (TelephonyManager) this.getSystemService(getApplication().TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                phoneNumber = tm.getLine1Number(); //得到本机号码
            }
            editTextPhone.setText(phoneNumber); //显示到对应栏

            //弹出短信发送窗口
            new AlertDialog.Builder(this)
                    .setTitle("发送未签到人员短信到:")
                    .setView(layout)
                    .setPositiveButton("发送", (dialog13, which) -> {
                        StringBuilder sendString = new StringBuilder();
                        sendString.append(editTextComment.getText().toString()).append("\n"); //备注信息放到开头
                        sendString.append("未签到学生信息:").append("\n"); //接下来是未签到的学生信息
                        for (String string : faceStrings) {
                            sendString.append(string).append("\n"); //一个一行
                        }
                        sendSMS(editTextPhone.getText().toString(), sendString.toString()); //发送
                        dialog.dismiss();
                    })
                    .setNegativeButton("取消", (dialog12, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> dialog.dismiss());
    }

    /**
     * 直接调用短信接口发短信，不含发送报告和接受报告
     * @param phoneNumber 目标电话号码
     * @param message 短信内容
     */
    private void sendSMS(String phoneNumber, String message) {
        Context context = ClassActivity.this;
        //处理返回的发送状态
        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sendIntent= PendingIntent.getBroadcast(context, 0, sentIntent,
                0);
        // register the Broadcast Receivers
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context,
                                "短信发送成功", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        break;
                }
            }
        }, new IntentFilter(SENT_SMS_ACTION));

        //处理返回的接收状态
        String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
        // create the deilverIntent parameter
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent backIntent= PendingIntent.getBroadcast(context, 0,
                deliverIntent, 0);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                Toast.makeText(context,
                        "收信人已经成功接收", Toast.LENGTH_SHORT)
                        .show();
            }
        }, new IntentFilter(DELIVERED_SMS_ACTION));

        // 获取短信管理器
        SmsManager smsManager = SmsManager.getDefault();
        if (message.length() > 70) {
            ArrayList<String> msgs = smsManager.divideMessage(message);
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            for(int i = 0;i<msgs.size();i++){
                sentIntents.add(sendIntent);
            }
            smsManager.sendMultipartTextMessage(phoneNumber, null, msgs, sentIntents, null);
        } else {
            smsManager.sendTextMessage(phoneNumber, null, message, sendIntent, backIntent); //系统短信发送接口
        }
    }

    //AsyncTask类, 后台函数是独立的线程进行耗时操作, 之后把数据返回到UI线程
    private class DrawRectTask extends AsyncTask<Object, Void, Face> {

        @Override
        protected Face doInBackground(Object... Objects) { //后台任务, 耗时的一些操作
            byte [] imageByte = (byte[]) Objects[0]; //此参数是nv21图像数据
            Face resultFace = null;
            int imgWidth = (int) Objects[1]; //此参数是图像宽度
            int imgHeight = (int) Objects[2]; //此参数是图像高度
            boolean hasResult = (boolean) Objects[3]; //此参数判断之前有没有识别到数据

            //如果之前识别到了
            if (hasResult) {
                //重新识别
                List<AFD_FSDKFace> result = FaceRecognition.singleFaceDetection(imageByte, imgWidth, imgHeight);
                if (!result.isEmpty()) { //识别到结果之后, 进行相似度匹配
                    AFD_FSDKFace faceInfo = FaceRecognition.getMaxFace_AFD(result); //得到最大的矩形框
                    AFR_FSDKFace faceFeature = FaceRecognition.singleGetFaceFeature(imageByte, imgWidth, imgHeight, faceInfo.getRect(), faceInfo.getDegree()); //提取人脸特征信息
                    AFR_FSDKFace registeredFaceFeature = new AFR_FSDKFace();
                    float maxScore = 0.6f; //相似度确认最小阈值
                    for (Face face : signInFaceAdapter.getFaceList()) { //遍历当前未签到的所有人脸信息
                        registeredFaceFeature.setFeatureData(face.getFeatureData()); //得到当前的人脸特征信息
                        float score = FaceRecognition.singleFaceMatching(registeredFaceFeature, faceFeature); //人脸信息比对
                        if (score > maxScore) { //得到匹配度最大的一个
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
        protected void onPostExecute(Face face) { //此函数运行绘图的一些操作, 运行在主绘图窗口
            super.onPostExecute(face);
            if (face != null) {
                int faceIndex = signInFaceAdapter.getFaceList().indexOf(face); //得到当前的索引
                current_face_imageView.setImageBitmap(face.getFaceImage()); //设置图片
                current_face_imageView.setAlpha(1.0f); //不透明
                cur_face_name_textView.setAlpha(1.0f); //不透明
                cur_face_name_textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); //红色字体
                cur_face_name_textView.setText(String.format("%s:%s签到成功!", face.getFaceID(), face.getFaceName())); //提示字
                recyclerView.scrollToPosition(faceIndex); //平滑移动到对应位置(只要屏幕中出现即可)
                signInFaceAdapter.removeDataDelayToDisplay(faceIndex); //进行删除
                current_face_imageView.removeCallbacks(currentFaceImageHide); //取消隐藏任务
            } else { //没有识别到, 2秒后隐藏左上角的提示图
                current_face_imageView.postDelayed(currentFaceImageHide, 2000);
            }
        }
    }

    //隐藏左上角的提示图
    Runnable currentFaceImageHide = new Runnable() {
        @Override
        public void run() {
            current_face_imageView.setAlpha(0.0f); //全透明
            cur_face_name_textView.setAlpha(0.0f); //全透明
            cur_face_name_textView.setText(""); //删除提示字
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera; //得到摄像头设备
            createCameraPreview(); //创建预览接口
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

    //开始后台任务
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    //停止后台任务
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
                    ImageFormat.YUV_420_888, 2); //设置图片读取格式
            imageReader.setOnImageAvailableListener(this::onImageAvailable, mBackgroundHandler); //设置读取后的运行函数
            SurfaceTexture texture = textureView.getSurfaceTexture(); //得到预览的表面
            assert texture != null;
            //设置预览大小
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder.addTarget(surface); //添加预览画面捕获后的输出平面
            captureRequestBuilder.addTarget(imageReader.getSurface()); //预览画面捕获后的输出到imageReader
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), /* 配置完成创建捕获会话 */
                    new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview(); //更新预览画面
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

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);//获得所有摄像头的管理者CameraManager
        try {
            String cameraId;
            //判断使用前置还是后置摄像头
            if (camera_front_or_back.equals("back")) {
                cameraId = manager.getCameraIdList()[0];
            } else {
                cameraId = manager.getCameraIdList()[1];
            }
            //获得某个摄像头的特征，支持的参数
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            //支持的STREAM CONFIGURATION
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            /* 获取预览, 参数为输出类图片画面的输出尺寸型 */
            imageDimension = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), textureView.getWidth(), textureView.getHeight());
            Log.i(TAG, "openCamera: 宽高" + imageDimension.getWidth() + "  " + imageDimension.getHeight());
            updateTextureViewSizeCenter(); //更新显示视图的分辨率以放置预览图数据
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ClassActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                Toast.makeText(this, "没有摄像头权限", Toast.LENGTH_SHORT).show();
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
     * @param outputSizes 所有分辨率
     * @param width 目标宽度
     * @param height 目标高度
     * @return 返回最合适的分辨率
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
     * 更新预览画面
     */
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO); //设置自动对焦等
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //关闭摄像头
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
            textureView.setVisibility(View.VISIBLE); //设置可见
            if (textureView.isAvailable()) {
                openCamera(cameraID_front_or_back); //重新打开摄像头, 开始预览等
            } else {
                textureView.setSurfaceTextureListener(textureListener);
            }
        }
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        if (textureView != null) {
            textureView.setVisibility(View.INVISIBLE); //设置不可见 不加这一句锁屏打开后会崩溃
        }
        closeCamera(); //关闭摄像头
        stopBackgroundThread(); //停止后台线程
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceRecognition != null) {
            faceRecognition.FinishFaceTracking(); //销毁人脸跟踪引擎
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //菜单选项
        switch (item.getItemId()) {
            case android.R.id.home: //左上角返回键
                onBackPressed(); //返回
        }
        return super.onOptionsItemSelected(item);
    }
}
