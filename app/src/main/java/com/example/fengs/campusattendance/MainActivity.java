package com.example.fengs.campusattendance;

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
import android.widget.Toast;

import com.example.fengs.campusattendance.DataView.FaceViewActivity;
import com.example.fengs.campusattendance.DataView.GroupViewActivity;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Spinner groupSpinner;
    private ImageView groupBigImage;
    private Button button_classes_begin;
    private List<GroupDB> groupDBList;
    private GroupDB selectGroupDB;
    private Uri imageFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        groupSpinner = findViewById(R.id.group_select_spinner);
        groupBigImage = findViewById(R.id.group_big_image_view);
        button_classes_begin = findViewById(R.id.button_classes_begin);
        button_classes_begin.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        groupDBList = DataSupport.findAll(GroupDB.class);
        ArrayAdapter<GroupDB> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groupDBList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        groupSpinner.setAdapter(arrayAdapter);
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 selectGroupDB = (GroupDB) parent.getItemAtPosition(position);
                 if (selectGroupDB.getGroupImage() != null) {
                     groupBigImage.setImageBitmap(selectGroupDB.getGroupImage());
                 } else {
                     groupBigImage.setImageResource(R.drawable.book_default);
                 }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                groupBigImage.setImageResource(R.drawable.book_default);
            }
        });
    }

    /**
     * 创建右上角菜单
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    /**
     * 菜单选项
     * @param item
     * @return
     */
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_classes_begin: {
                Intent intent = new Intent(MainActivity.this, ClassActivity.class);
                intent.putExtra("groupID", selectGroupDB.getId());
                startActivity(intent);
            }
        }
    }
}
