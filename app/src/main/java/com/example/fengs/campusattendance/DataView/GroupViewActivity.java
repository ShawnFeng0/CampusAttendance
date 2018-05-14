package com.example.fengs.campusattendance.DataView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.List;

public class GroupViewActivity extends AppCompatActivity {

    private List<GroupDB> groupDBList;
    private RecyclerView recyclerView;
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
                AlertDialog dialog = new AlertDialog.Builder(GroupViewActivity.this)
                        .setTitle("添加新组")
                        .setView(layout)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("取消",null)
                        .show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                    EditText groupIDtext = layout.findViewById(R.id.edit_view_groupID);
                    EditText groupName = layout.findViewById(R.id.edit_view_group_name);
                    if (!groupIDtext.getText().toString().isEmpty() && !groupName.getText().toString().isEmpty()) {
                        GroupDB groupDB = new GroupDB();
                        groupDB.setGroupID(groupIDtext.getText().toString());
                        groupDB.setGroupName(groupName.getText().toString());
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


    /**
     * 更新数据列表
     */
    private void recyclerViewUpdate() {
        groupDBList = DataSupport.findAll(GroupDB.class);
        final GroupAdapter adapter = new GroupAdapter(groupDBList);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 进入人脸显示界面, 传入group的数据库ID
     * @param groupDB
     */
    public void faceView(GroupDB groupDB) {
        Intent intent = new Intent(GroupViewActivity.this, FaceViewActivity.class);
        intent.putExtra("groupID", groupDB.getId());
        startActivityForResult(intent, REQUEST_CODE_REGISTER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerViewUpdate();
    }
}
