package com.example.fengs.campusattendance;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fengs.campusattendance.DataView.FaceViewActivity;
import com.example.fengs.campusattendance.DataView.GroupViewActivity;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
    private static final int REQUEST_CODE_REGISTER = 2;
    private Spinner groupSpinner;
    private List<GroupDB> groupDBList;
    private Uri imageFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        groupSpinner = findViewById(R.id.group_select_spinner);

        groupDBList = DataSupport.findAll(GroupDB.class);

        ArrayAdapter<GroupDB> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groupDBList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        groupSpinner.setAdapter(arrayAdapter);

//        Button button = this.findViewById(R.id.button_register);
//        button.setOnClickListener(this);
//        button = this.findViewById(R.id.button_recognition);
//        button.setOnClickListener(this);
//        button = this.findViewById(R.id.button_addData);
//        button.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_group:
                Intent intent = new Intent(MainActivity.this, GroupViewActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
            Intent intent = new Intent(MainActivity.this, FaceViewActivity.class);
            intent.putExtra("imageUri", imageFileUri);
            startActivityForResult(intent, REQUEST_CODE_REGISTER);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_addData: {
//                for (int i = 1; i < 10; i++) {
//                    final String groupID = "142027";
//                    final String groupName = "通信工程";
//                    GroupDB group = new GroupDB();
//                    group.setGroupID(groupID + String.valueOf(i));
//                    group.setGroupName(groupName + String.valueOf(i));
////                    Face face_temp = new Face();
////                    face_temp.setFaceID("142027339");
////                    face_temp.setFaceName();
////                    face_temp.save();
////                    group.getStudentsGroup().add(face_temp);
//                    group.save();
//                }

                Toast.makeText(this, "You clicked group database", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
