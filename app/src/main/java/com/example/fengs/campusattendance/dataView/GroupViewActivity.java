package com.example.fengs.campusattendance.dataView;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.BitmapHandle;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

/**
 * 组信息显示和管理界面
 */
public class GroupViewActivity extends AppCompatActivity {

    private EditText dialogGroupCourse; //添加时对话框里的课程栏
    private EditText dialogGroupClass; //添加时对话框里的班级栏
    private EditText dialogPhoneNumber; //添加时对话框里的电话栏
    private ImageButton dialogGroupImageButton; //添加时对话框里的课程图栏

    private Bitmap groupImageBitmap; //拍照得到的新图
    private Uri imageFileUri; //拍照得到的图片地址

    private RecyclerView recyclerView; //循环列表视图
    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view); //设置对应界面布局

        //列表设置
        recyclerView = findViewById(R.id.group_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        recyclerViewUpdate(); //更新分组列表

        Button back_button = findViewById(R.id.group_back_button);
        back_button.setOnClickListener(v -> finish()); //左上角的返回按键

        //添加按键动作
        Button add_button = findViewById(R.id.group_add_button);
        add_button.setOnClickListener(v -> {
            LayoutInflater inflater = getLayoutInflater();
            final View layout = inflater.inflate(R.layout.dialog_add_group, null);

            dialogGroupCourse = layout.findViewById(R.id.edit_view_group_course); //课程对应输入框
            dialogGroupClass = layout.findViewById(R.id.edit_view_group_class); //班级对应输入框
            dialogPhoneNumber = layout.findViewById(R.id.edit_view_phone_number); //电话对应输入框
            dialogGroupImageButton = layout.findViewById(R.id.group_image_button); //图片
            dialogGroupImageButton.setOnClickListener(v2 ->
                    imageFileUri = BitmapHandle.imageCapture(this, REQUEST_CODE_IMAGE_CAMERA)); //点击之后调用系统拍照程序

            AlertDialog dialog = new AlertDialog.Builder(GroupViewActivity.this)
                    .setTitle("添加") //标题
                    .setView(layout) //布局
                    .setPositiveButton("确定", null) //正向按钮, 之后单独添加动作
                    .setNegativeButton("取消",null) //反向按钮, 之后单独添加动作
                    .show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((View v1) -> {

                //课程与班级信息都不为空
                if (!dialogGroupCourse.getText().toString().isEmpty()
                        && !dialogGroupClass.getText().toString().isEmpty()) {
                    GroupDB groupDB = new GroupDB();
                    groupDB.setGroupCourseStr(dialogGroupCourse.getText().toString()); //设置课程
                    groupDB.setGroupClassStr(dialogGroupClass.getText().toString()); //设置班级
                    groupDB.setAdminPhoneNumberStr(dialogPhoneNumber.getText().toString()); //设置电话号
                    groupDB.setGroupImage(groupImageBitmap); //设置课程图片
                    groupDB.save(); //保存到数据库
                    recyclerViewUpdate(); //更新分组列表
                    dialog.dismiss();
                } else {
                    Toast.makeText(GroupViewActivity.this, "请输入相关信息", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> {
                    dialog.dismiss();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //判断请求码
        if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
            try {
                groupImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageFileUri); //得到bitmap
                this.getContentResolver().delete(imageFileUri, null, null); //删掉备份的图片
            } catch (IOException e) {
                e.printStackTrace();
            }

            groupImageBitmap = BitmapHandle.getZoomBitmap(groupImageBitmap, 450, 600); //裁剪大小
            dialogGroupImageButton.setImageBitmap(groupImageBitmap); //设置图片显示
            dialogGroupImageButton.setScaleType(ImageView.ScaleType.FIT_CENTER); //居中缩放
            FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            dialogGroupImageButton.setLayoutParams(param); //高和宽填满父容器
        }
    }

    /**
     * 更新数据列表
     */
    private void recyclerViewUpdate() {
        List<GroupDB> groupDBList = DataSupport.findAll(GroupDB.class); //得到当前组的人脸信息
        final GroupAdapter adapter = new GroupAdapter(groupDBList); //新建适配器
        recyclerView.setAdapter(adapter); //设置设配器
    }

    /**
     * 进入人脸显示界面, 传入group的数据库ID
     * @param groupDB
     */
    public void faceView(GroupDB groupDB) {
        Intent intent = new Intent(GroupViewActivity.this, FaceViewActivity.class);
        intent.putExtra("groupID", groupDB.getId()); //传入分组ID
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerViewUpdate(); //从人脸信息界面退出到组信息, 重新显示界面
    }
}
