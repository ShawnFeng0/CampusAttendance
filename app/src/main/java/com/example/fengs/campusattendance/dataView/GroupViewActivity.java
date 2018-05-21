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
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

public class GroupViewActivity extends AppCompatActivity {

    private Bitmap groupImageBitmap;
    private RecyclerView recyclerView;
    EditText dialogGroupCourse;
    EditText dialogGroupName;
    EditText dialogPhoneNumber;
    ImageButton dialogGroupImageButton;
    private Uri imageFileUri;
    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
    private static final int REQUEST_CODE_REGISTER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);
        recyclerView = findViewById(R.id.group_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        recyclerViewUpdate(); //更新分组列表

        Button back_button = findViewById(R.id.group_back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button add_button = findViewById(R.id.group_add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.dialog_add_group, null);

                dialogGroupCourse = layout.findViewById(R.id.edit_view_group_course);
                dialogGroupName = layout.findViewById(R.id.edit_view_group_name);
                dialogPhoneNumber = layout.findViewById(R.id.edit_view_phone_number);
                dialogGroupImageButton = layout.findViewById(R.id.group_image_button);
                dialogGroupImageButton.setOnClickListener(v2 -> {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
                    startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
                });

                AlertDialog dialog = new AlertDialog.Builder(GroupViewActivity.this)
                        .setTitle("添加新组")
                        .setView(layout)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("取消",null)
                        .show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((View v1) -> {
                    if (!dialogGroupCourse.getText().toString().isEmpty()
                            && !dialogGroupName.getText().toString().isEmpty()) {
                        GroupDB groupDB = new GroupDB();
                        groupDB.setGroupCourse(dialogGroupCourse.getText().toString());
                        groupDB.setGroupName(dialogGroupName.getText().toString());
                        groupDB.setAdminPhoneNumber(dialogPhoneNumber.getText().toString());
                        groupDB.setGroupImage(groupImageBitmap);
                        groupDB.save();
                        recyclerViewUpdate(); //更新分组列表
                        dialog.dismiss();
                    } else {
                        Toast.makeText(GroupViewActivity.this, "请输入相关信息", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> {
                        dialog.dismiss();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
            try {
                groupImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageFileUri);
                this.getContentResolver().delete(imageFileUri, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            groupImageBitmap = getZoomBitmap(groupImageBitmap, 450, 600);
            dialogGroupImageButton.setImageBitmap(groupImageBitmap);
            dialogGroupImageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            dialogGroupImageButton.setLayoutParams(param);
        }
    }

    /**
     * 更新数据列表
     */
    private void recyclerViewUpdate() {
        List<GroupDB> groupDBList = DataSupport.findAll(GroupDB.class);
        final GroupAdapter adapter = new GroupAdapter(groupDBList);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 得到缩放后的Bitmap
     * @param src_bitmap
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private Bitmap getZoomBitmap(Bitmap src_bitmap, int targetWidth, int targetHeight) {
        float scaleWidth = (float)targetWidth / src_bitmap.getWidth();
        float scaleHeight = (float)targetHeight / src_bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(src_bitmap,
                0, 0, src_bitmap.getWidth(), src_bitmap.getHeight(), matrix, true);
    }

    /**
     * 进入人脸显示界面, 传入group的数据库ID
     * @param groupDB
     */
    public void faceView(GroupDB groupDB) {
        Intent intent = new Intent(GroupViewActivity.this, FaceViewActivity.class);
        intent.putExtra("groupID", groupDB.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerViewUpdate();
    }
}
