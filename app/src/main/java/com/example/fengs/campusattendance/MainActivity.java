package com.example.fengs.campusattendance;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.fengs.campusattendance.dataView.GroupViewActivity;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Spinner groupSpinner; //分组选择框
    private ImageView groupBigImage; //课程图片
    private GroupDB selectGroupDB; //当前选择的分组

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //设置对应的布局
        setSupportActionBar(findViewById(R.id.toolbar)); //设置工具栏

        groupSpinner = findViewById(R.id.group_select_spinner); //对应的选择框
        groupBigImage = findViewById(R.id.group_big_image_view); //对应的课程图浏览
        Button button_classes_begin = findViewById(R.id.button_classes_begin); //对应的按钮
        button_classes_begin.setOnClickListener(this); //按键动作监听
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<GroupDB> groupDBList = DataSupport.findAll(GroupDB.class); //返回主界面之后重新获取分组的列表
        ArrayAdapter<GroupDB> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groupDBList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //设置下拉的样式
        groupSpinner.setAdapter(arrayAdapter);

        //选项选择之后的动作监听
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 selectGroupDB = (GroupDB) parent.getItemAtPosition(position);
                 if (selectGroupDB.getGroupImage() != null) { //设置课程图
                     groupBigImage.setImageBitmap(selectGroupDB.getGroupImage());
                 } else { //没有就设置默认的图
                     groupBigImage.setImageResource(R.drawable.book_default);
                 }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                groupBigImage.setImageResource(R.drawable.book_default);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //设置选项菜单
        getMenuInflater().inflate(R.menu.main_toolbar, menu); //设置对应工具栏
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //工具栏选项点击后的动作
        switch (item.getItemId()) {
            case R.id.view_group: //查看分组
                Intent intent = new Intent(MainActivity.this, GroupViewActivity.class); //进入分组视图
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_classes_begin: { //开始签到
                Intent intent = new Intent(MainActivity.this, ClassActivity.class);
                intent.putExtra("groupID", selectGroupDB.getId()); //传递分组信息
                intent.putExtra("camera", "front"); //使用前置摄像头
                startActivity(intent);

//                /* 后置相机没有做图像的转换，暂时不可用 */
//                new AlertDialog.Builder(this)
//                        .setTitle("请选择相机")
//                        .setIcon(android.R.drawable.ic_dialog_info)
//                        .setItems(new String[]{"后置相机", "前置相机"}, (dialog, which) -> {
//                            Intent intent = new Intent(MainActivity.this, ClassActivity.class);
//                            intent.putExtra("groupID", selectGroupDB.getId());
//                            if (which == 0) {
//                                intent.putExtra("camera", "back");
//                            } else {
//                                intent.putExtra("camera", "front");
//                            }
//                            startActivity(intent);
//                        })
//                        .show();
            }
        }
    }
}
