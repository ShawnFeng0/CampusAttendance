package com.example.fengs.campusattendance.DataView;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.example.fengs.campusattendance.Bmp2YUV;
import com.example.fengs.campusattendance.FaceRecognition;
import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.Face;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

public class FaceViewActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private GroupDB groupDB;
    private List<Face> faceList;
    private RecyclerView recyclerView;
    private ImageView bigImageView;

    private Uri imageFileUri;
    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
    private static final int REQUEST_CODE_REGISTER = 2;

    private final String TAG = this.getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_view);

        int group_id = getIntent().getIntExtra("groupID", 0);
        groupDB = DataSupport.find(GroupDB.class, group_id);
        TextView face_title_text = findViewById(R.id.face_title_text);
        face_title_text.setText(groupDB.getGroupID() + ":" + groupDB.getGroupName());

        bigImageView = findViewById(R.id.face_big_image_view);

        //列表设置
        recyclerView = findViewById(R.id.face_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL); //水平显示
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)); //水平方向的竖直分割线

        Button back_button = findViewById(R.id.face_back_button);
        back_button.setOnClickListener(v -> finish());
        Button add_button = findViewById(R.id.face_add_button);
        add_button.setOnClickListener(v -> imageCapture());

        recycleViewUpdate(); //更新列表

        //设置预览的大图显示, 有人脸显示人脸, 没有就显示添加
        if (faceList.size() != 0) {
            setBigImageView(faceList.get(0).getFaceImage());
        } else {
            setBigImageView(R.drawable.add_photos);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageFileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            byte[] bitmap_data;
            bitmap_data = (new Bmp2YUV()).getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);

            Log.d(TAG, "onCreate: ");
            List<AFD_FSDKFace> result = FaceRecognition.FaceDetectionProcess(bitmap_data, bitmap.getWidth(), bitmap.getHeight());

            if (!result.isEmpty()) {
                AFD_FSDKFace faceRect = FaceRecognition.getMaxFace(result);

                int width = faceRect.getRect().width()/30 > 10 ? faceRect.getRect().width()/30 : 10;
                Canvas canvas = new Canvas(mutableBitmap);
                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStrokeWidth(width);
                paint.setStyle(Paint.Style.STROKE);

                Rect rect = getRealFaceRect(bitmap, faceRect.getRect());

                canvas.drawRect(rect, paint);

                AFR_FSDKFace faceFeature = FaceRecognition.getFaceFeature(bitmap_data, bitmap.getWidth(), bitmap.getHeight(), faceRect);

                //弹出添加人脸的窗口
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.dialog_add_face, null);
                AlertDialog dialog = new AlertDialog.Builder(FaceViewActivity.this)
                        .setTitle("添加")
                        .setView(layout)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("取消", null)
                        .show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                        EditText faceIDtext = layout.findViewById(R.id.edit_view_faceID);
                        EditText faceName = layout.findViewById(R.id.edit_view_face_name);
                        if (!faceName.getText().toString().isEmpty() && !faceIDtext.getText().toString().isEmpty()) {
                            Face face = new Face();
                            face.setFaceID(faceIDtext.getText().toString());
                            face.setFaceName(faceName.getText().toString());
                            face.setFaceImage(getFaceImage(bitmap, rect));
                            face.setFeatureData(faceFeature.getFeatureData());
                            face.save();
                            groupDB.getFaces().add(face);
                            groupDB.save();
                            recycleViewUpdate(); //更新分组列表
                            dialog.dismiss();
                        } else {
                            Toast.makeText(FaceViewActivity.this, "请输入相关信息", Toast.LENGTH_SHORT).show();
                        }
                    });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                        dialog.dismiss();
                });

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
        faceList = groupDB.getFaces();
        final FaceAdapter adapter = new FaceAdapter(faceList);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 设置大图预览, 填充整个界面
     * @param bitmap
     */
    public void setBigImageView(Bitmap bitmap) {
        bigImageView.setImageBitmap(bitmap);
        bigImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bigImageView.setOnClickListener(v -> {});
    }

    /**
     * 设置大图预览, 小图标显示
     * @param resId
     */
    public void setBigImageView(int resId) {
        bigImageView.setImageResource(resId);
        bigImageView.setScaleType(ImageView.ScaleType.CENTER);
        bigImageView.setOnClickListener(v -> imageCapture());
    }

    /**
     * 保存人脸图片, 缩放原图
     * @param src_bitmap
     * @param rect
     */
    private Bitmap getFaceImage(Bitmap src_bitmap, Rect rect) {
        float targetWidth = 400; //目标宽度高度
        float targetHeight = 400;
        float scaleWidth = targetWidth / rect.width();
        float scaleHeight = targetHeight / rect.height();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(src_bitmap,
                rect.left, rect.top, rect.width(), rect.height(), matrix, true);
    }

    /**
     * 得到一个真实的人脸矩形框
     * 人脸识别引擎得到的人脸矩形框比较小, 所以稍微放大一点
     * @param src_bitmap
     * @param rect
     * @return
     */
    private Rect getRealFaceRect(Bitmap src_bitmap, Rect rect) {
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
     * 调用系统拍照程序
     */
    public void imageCapture() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
    }
}
