package com.example.fengs.campusattendance.dataView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.example.fengs.campusattendance.FaceRecognition;
import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.BitmapHandle;
import com.example.fengs.campusattendance.database.Face;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

/**
 * 人脸信息显示和管理界面
 */
public class FaceViewActivity extends AppCompatActivity {


    private RecyclerView recyclerView; //循环列表视图
    private ImageView bigImageView; //大图预览

    private Bitmap cameraNewBitmap; //拍照得到的新图
    private Uri imageFileUri; //拍照的得到的图片地址
    private GroupDB groupDB; //当前组信息
    private List<Face> faceList; //所有人脸数据
    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;

    private final String TAG = this.getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_view); //设置对应界面布局

        int group_id = getIntent().getIntExtra("groupID", 0); //得到启动时上一个窗口传来的组数据库ID信息
        groupDB = DataSupport.find(GroupDB.class, group_id); //从数据库查找该组信息
        TextView face_title_text = findViewById(R.id.face_title_text); //设置当前界面的标题
        face_title_text.setText(String.format("%s:%s", groupDB.getGroupCourseStr(), groupDB.getGroupClassStr()));

        bigImageView = findViewById(R.id.face_big_image_view); //绑定到对应视图

        //列表设置
        recyclerView = findViewById(R.id.face_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this); //线性布局
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL); //水平显示
        recyclerView.setLayoutManager(layoutManager); //设置布局
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)); //添加水平方向的竖直分割线

        Button back_button = findViewById(R.id.face_back_button);
        back_button.setOnClickListener(v -> onBackPressed()); //左上角的返回按键
        Button add_button = findViewById(R.id.face_add_button);
        add_button.setOnClickListener(v ->
                imageFileUri = BitmapHandle.imageCapture(this, REQUEST_CODE_IMAGE_CAMERA)); //右上角的添加按键

        recycleViewUpdate(); //更新列表

        //设置预览的大图显示, 有人脸显示人脸, 没有就显示添加
        if (faceList.size() != 0) {
            setBigImageView(faceList.get(0).getFaceImage()); //先显示第一个图
        } else {
            setBigImageView(R.drawable.add_photos); //没有则显示图标
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //判断请求码
        if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
            try {
                //从指定URI地址读取bitmap
                cameraNewBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageFileUri);
                this.getContentResolver().delete(imageFileUri, null, null); //删除uri指定图片数据, 不然会保存到系统中占用内存
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap mutableBitmap = cameraNewBitmap.copy(Bitmap.Config.ARGB_8888, true); //转换成可编辑的bitmap

            byte[] bitmap_data; //生成NV21数据
            bitmap_data = BitmapHandle.bitmapToNV21Byte(cameraNewBitmap.getWidth(), cameraNewBitmap.getHeight(), cameraNewBitmap);

            //识别人脸,得到人脸的矩形框
            List<AFD_FSDKFace> result = FaceRecognition.singleFaceDetection(bitmap_data, cameraNewBitmap.getWidth(), cameraNewBitmap.getHeight());

            if (!result.isEmpty()) {
                AFD_FSDKFace faceRect = FaceRecognition.getMaxFace_AFD(result); //得到最大的矩形框

                int width = faceRect.getRect().width()/30 > 10 ? faceRect.getRect().width()/30 : 10; //边框根据矩形大小决定
                Rect bigFaceRect = BitmapHandle.getBigFaceRect(cameraNewBitmap, faceRect.getRect()); //矩形放大之后保存
                Canvas canvas = new Canvas(mutableBitmap);
                Paint paint = new Paint(); //画笔
                paint.setColor(Color.GREEN); //颜色
                paint.setStrokeWidth(width); //宽度
                paint.setStyle(Paint.Style.STROKE); //线条
                canvas.drawRect(bigFaceRect, paint); //绘制矩形

                //提取人脸特征信息
                AFR_FSDKFace faceFeature = FaceRecognition.singleGetFaceFeature(bitmap_data, cameraNewBitmap.getWidth(), cameraNewBitmap.getHeight(), faceRect.getRect(), faceRect.getDegree());

                //人脸特征信息中, IDE提示不对, 第一个字节是0xaf, 有时提取会出问题, 所以增加一个判断
                if (faceFeature.getFeatureData()[0] != 0xaf) {
                    //弹出添加人脸的窗口
                    LayoutInflater inflater = getLayoutInflater();
                    final View layout = inflater.inflate(R.layout.dialog_add_face, null); //对话框布局
                    AlertDialog dialog = new AlertDialog.Builder(FaceViewActivity.this)
                            .setTitle("添加") //标题
                            .setView(layout) //布局
                            .setPositiveButton("确定", null) //正向按钮, 之后单独添加动作
                            .setNegativeButton("取消", null) //反向按钮, 之后单独添加动作
                            .show(); //显示
                    //单独添加按键的动作, 点完确定后不会如果条件不满足不会推出.   不单独添加就会退出
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                        EditText faceIDStr = layout.findViewById(R.id.edit_view_faceID); //学号
                        EditText faceName = layout.findViewById(R.id.edit_view_face_name); //名字
                        //学号和姓名都不能为空
                        if (!faceName.getText().toString().isEmpty() && !faceIDStr.getText().toString().isEmpty()) {
                            Face face = new Face(); //新建人脸
                            face.setFaceID(faceIDStr.getText().toString()); //设置学号
                            face.setFaceName(faceName.getText().toString()); //设置名字
                            face.setFaceImage(BitmapHandle.getRectZoomBitmap(cameraNewBitmap, bigFaceRect, 400, 400)); //缩放到指定宽高
                            face.setFeatureData(faceFeature.getFeatureData()); //人脸特征数据
                            face.save(); //保存到数据库
                            groupDB.getFaces().add(face); //添加到对应分组, 这样两个数据库表都会被更新, face里也会有groupDB的ID
                            groupDB.save(); //保存到数据库
                            recycleViewUpdate(); //更新分组列表
                            dialog.dismiss();
                        } else { //否则弹出提示
                            Toast.makeText(FaceViewActivity.this, "请输入相关信息", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                        dialog.dismiss();
                    });
                } else {
                    Toast.makeText(FaceViewActivity.this, "人脸信息提取失败, 请点击右上角重新添加", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(FaceViewActivity.this, "没有检测到人脸，请换一张图片", Toast.LENGTH_SHORT).show();
            }
            setBigImageView(mutableBitmap);
        }
    }

    /**
     * 更新显示列表
     */
    private void recycleViewUpdate() {
        faceList = groupDB.getFaces(); //得到当前组的人脸信息
        final FaceAdapter adapter = new FaceAdapter(faceList); //新建适配器
        recyclerView.setAdapter(adapter); //设置适配器
    }

    /**
     * 设置大图预览, 填充整个界面
     * @param bitmap 要设置的bitmap
     */
    public void setBigImageView(Bitmap bitmap) {
        bigImageView.setImageBitmap(bitmap); //设置图像
        bigImageView.setScaleType(ImageView.ScaleType.FIT_CENTER); //拉伸显示, 充满整个视图
        bigImageView.setOnClickListener(v -> {}); //点击之后不调用任何程序
    }

    /**
     * 设置大图预览, 小图标显示
     * @param resId 要设置的图标
     */
    public void setBigImageView(int resId) {
        bigImageView.setImageResource(resId); //设置图像
        bigImageView.setScaleType(ImageView.ScaleType.CENTER); //不拉伸显示, 原始图居中
        bigImageView.setOnClickListener(v ->
                imageFileUri = BitmapHandle.imageCapture(this, REQUEST_CODE_IMAGE_CAMERA)); //点击之后调用系统拍照程序
    }
}
