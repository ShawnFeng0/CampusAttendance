package com.example.fengs.campusattendance;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private Bitmap mutableBitmap;

    private final String TAG = this.getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resgister);

        ImageView imageView = findViewById(R.id.imageView);

        Uri imageUri = getIntent().getParcelableExtra("imageUri");
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        byte[] data;
        data = (new Bmp2YUV()).getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);

        Log.d(TAG, "onCreate: ");
        List<AFD_FSDKFace> result = FaceRecognition.FaceDetectionProcess(data, bitmap.getWidth(), bitmap.getHeight());

        if (!result.isEmpty()) {
            AFD_FSDKFace face =  FaceRecognition.getMaxFace(result);

            int width = face.getRect().width()/30 > 10 ? face.getRect().width()/30 : 10;
            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(width);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(face.getRect(), paint);

            AFR_FSDKFace faceFeature = FaceRecognition.getFaceFeature(data, bitmap.getWidth(), bitmap.getHeight(), face);
            
        } else {
            Toast.makeText(RegisterActivity.this, "没有检测到人脸，请换一张图片", Toast.LENGTH_SHORT).show();
        }
        imageView.setImageBitmap(mutableBitmap);
    }
}
