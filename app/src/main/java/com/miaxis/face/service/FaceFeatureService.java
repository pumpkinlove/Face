package com.miaxis.face.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.miaxis.face.app.Face_App;
import com.miaxis.face.constant.Constants;

import org.zz.faceapi.MXFaceAPI;
import org.zz.faceapi.MXFaceInfo;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FaceFeatureService extends IntentService {
    private static final String ACTION_FEATURE = "com.miaxis.face.service.action.feature";

    private static final String CAMERA_DATA = "com.miaxis.face.service.extra.CAMERA_DATA";
    private static final String FACE_NUM = "com.miaxis.face.service.extra.FACE_NUM";
    private static final String FACE_BUFFER = "com.miaxis.face.service.extra.FACE_BUFFER";

    public FaceFeatureService() {
        super("FaceFeatureService");
    }
    private MXFaceAPI mxFaceApi = Face_App.getMxAPI();

    public static void startExtractFeature(Context context, byte[] cameraData, int[] faceNum, MXFaceInfo[] pFaceBuffer) {
        Intent intent = new Intent(context, FaceFeatureService.class);
        intent.setAction(ACTION_FEATURE);
        intent.putExtra(CAMERA_DATA, cameraData);
        intent.putExtra(FACE_NUM, faceNum);
        intent.putExtra(FACE_BUFFER, pFaceBuffer);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FEATURE.equals(action)) {
                final byte[] cameraData = intent.getByteArrayExtra(CAMERA_DATA);
                final int[] faceNum = intent.getIntArrayExtra(FACE_NUM);
                final MXFaceInfo[] pFaceBuffer = (MXFaceInfo[]) intent.getParcelableArrayExtra(FACE_NUM);
                extractFeature(cameraData, faceNum, pFaceBuffer);
            }
        }
    }

    private void extractFeature(byte[] cameraData, int[] faceNum, MXFaceInfo[] pFaceBuffer) {
        byte[] pFeatureBuf = new byte[mxFaceApi.mxGetFeatureSize()];
        long s1 = System.currentTimeMillis();
        int re = mxFaceApi.mxFeatureExtractYUV(cameraData, Constants.PRE_WIDTH, Constants.PRE_HEIGHT, pFaceBuffer[0], pFeatureBuf);
        long s2 = System.currentTimeMillis();
        Log.e("mxFeatureExtractYUV", ""+re + "耗时: " + (s2 - s1));
    }

}
