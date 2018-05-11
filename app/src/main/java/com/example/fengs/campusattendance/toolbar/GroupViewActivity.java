package com.example.fengs.campusattendance.toolbar;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fengs.campusattendance.GroupAdapter;
import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.List;

public class GroupViewActivity extends AppCompatActivity {

    private List<GroupDB> groupDBList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        recycleViewUpdate(); //更新分组列表

        Button back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GroupViewActivity.this);
                dialog.setTitle("添加新组");
                dialog.setView(R.layout.dialog_add_group);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LayoutInflater inflater = LayoutInflater.from(GroupViewActivity.this);
                        View layout = inflater.inflate(R.layout.dialog_add_group, null);
                        EditText groupIDtext = layout.findViewById(R.id.edit_view_groupID);
                        EditText groupName = layout.findViewById(R.id.edit_view_group_name);
//                        GroupDB groupDB = new GroupDB();
//                        groupDB.setGroupID(groupIDtext.getText().toString());
//                        groupDB.setGroupName(groupName.getText().toString());
                        Toast.makeText(GroupViewActivity.this, groupIDtext.getText().toString(), Toast.LENGTH_SHORT).show();
//                        groupDB.save();
//                        recycleViewUpdate(); //更新分组列表
                        dialog.dismiss();

                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    private void recycleViewUpdate() {
        groupDBList = DataSupport.findAll(GroupDB.class);
        final GroupAdapter adapter = new GroupAdapter(groupDBList);
        recyclerView.setAdapter(adapter);
    }
}
