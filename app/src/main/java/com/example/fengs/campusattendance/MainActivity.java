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
import android.widget.Button;
import android.widget.Toast;

import com.example.fengs.campusattendance.DataView.FaceViewActivity;
import com.example.fengs.campusattendance.DataView.GroupViewActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
    private static final int REQUEST_CODE_REGISTER = 2;
    private Uri imageFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));
        Button button = this.findViewById(R.id.button_register);
        button.setOnClickListener(this);
        button = this.findViewById(R.id.button_recognition);
        button.setOnClickListener(this);
        button = this.findViewById(R.id.button_addData);
        button.setOnClickListener(this);
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
            case R.id.button_register: {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
                startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
                break;
            }
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
