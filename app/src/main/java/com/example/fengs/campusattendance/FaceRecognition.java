package com.example.fengs.campusattendance;

import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;

import java.util.ArrayList;
import java.util.List;

public class FaceRecognition {

    public static List<AFD_FSDKFace> FaceDetectionProcess(byte[] data, int width, int height) {
        AFD_FSDKEngine engine = new AFD_FSDKEngine();

        // 用来存放检测到的人脸信息列表
        List<AFD_FSDKFace> result = new ArrayList<>();

        //初始化人脸检测引擎，使用时请替换申请的APPID和SDKKEY
        AFD_FSDKError err = engine.AFD_FSDK_InitialFaceEngine("FT6NUE7YNUTpsY8MU9MsYoDbznky3HGTCfrnZBkcgGtJ","E2gjivyni7Cs54McJ2rvCjGgpitJU6fLsKfvHxSBJLHm", AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
        Log.d("com.arcsoft", "AFD_FSDK_InitialFaceEngine = " + err.getCode());

        //输入的data数据为NV21格式（如Camera里NV21格式的preview数据），其中height不能为奇数，人脸检测返回结果保存在result。
        err = engine.AFD_FSDK_StillImageFaceDetection(data, width, height, AFD_FSDKEngine.CP_PAF_NV21, result);
        Log.d("com.arcsoft", "AFD_FSDK_StillImageFaceDetection =" + err.getCode());
        Log.d("com.arcsoft", "Face=" + result.size());
        for (AFD_FSDKFace face : result) {
            Log.d("com.arcsoft", "Face:" + face.toString());
        }

        //销毁人脸检测引擎
        err = engine.AFD_FSDK_UninitialFaceEngine();
        Log.d("com.arcsoft", "AFD_FSDK_UninitialFaceEngine =" + err.getCode());

        return result;
    }

    public static AFR_FSDKFace getFaceFeature(byte[] data, int width, int height, AFD_FSDKFace face) {
        AFR_FSDKEngine engine = new AFR_FSDKEngine();

        //用来存放提取到的人脸信息
        AFR_FSDKFace faceFeature = new AFR_FSDKFace();

        //初始化人脸识别引擎，使用时请替换申请的APPID 和SDKKEY
        AFR_FSDKError error = engine.AFR_FSDK_InitialEngine("FT6NUE7YNUTpsY8MU9MsYoDbznky3HGTCfrnZBkcgGtJ", "E2gjivyni7Cs54McJ2rvCjH4JvfqjsHSqzBenDLwSPG8");
        Log.d("com.arcsoft", "AFR_FSDK_InitialEngine = " + error.getCode());

        //输入的data数据为NV21格式（如Camera里NV21格式的preview数据）；人脸坐标一般使用人脸检测返回的Rect传入；人脸角度请按照人脸检测引擎返回的值传入。
        error = engine.AFR_FSDK_ExtractFRFeature(data, width, height, AFR_FSDKEngine.CP_PAF_NV21, new Rect(face.getRect()), face.getDegree(), faceFeature);
        Log.d("com.arcsoft", "Face=" + faceFeature.getFeatureData()[0]+ "," + faceFeature.getFeatureData()[1] + "," + faceFeature.getFeatureData()[2] + "," + error.getCode());

        //销毁人脸识别引擎
        error = engine.AFR_FSDK_UninitialEngine();
        Log.d("com.arcsoft", "AFR_FSDK_UninitialEngine : " + error.getCode());

        return faceFeature;
    }

    public static AFD_FSDKFace getMaxFace(List<AFD_FSDKFace> result) {

        int area_t;
        int area_max = 0;
        Rect rect_t;
        AFD_FSDKFace face_max = null;

        if (!result.isEmpty()) {
            for (AFD_FSDKFace face : result) {
                rect_t = face.getRect();
                area_t = rect_t.width() * rect_t.height();
                if (area_t > area_max) {
                    area_max = area_t;
                    face_max = face;
                }
            }
        }

        return face_max;
    }
}
