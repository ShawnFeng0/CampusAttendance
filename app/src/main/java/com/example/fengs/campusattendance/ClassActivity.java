package com.example.fengs.campusattendance;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.fengs.campusattendance.dataView.SignInFaceAdapter;
import com.example.fengs.campusattendance.database.Face;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.List;

public class ClassActivity extends AppCompatActivity {

    private GroupDB groupDB;
    private RecyclerView recyclerView;
    private SurfaceView surfaceView;
//    private SurfaceViewCallback surfaceViewCallback = new SurfaceViewCallback();
    private List<Face> faceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);
        setSupportActionBar(findViewById(R.id.class_activity_toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        int group_id = getIntent().getIntExtra("groupID", 0);
        groupDB = DataSupport.find(GroupDB.class, group_id);

        //列表设置
        recyclerView = findViewById(R.id.sign_in_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //水平显示
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        recycleViewUpdate();

//        surfaceView = findViewById(R.id.);
//        surfaceView.getHolder().addCallback(surfaceViewCallback);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 更新显示列表
     */
    private void recycleViewUpdate() {
        faceList = groupDB.getFaces();
        final SignInFaceAdapter adapter = new SignInFaceAdapter(faceList);
        recyclerView.setAdapter(adapter);
        Toast.makeText(ClassActivity.this, "人数: " + faceList.size(), Toast.LENGTH_SHORT).show();
    }
}
