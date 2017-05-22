package com.miaxis.face.view.activity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.service.FaceFeatureService;
import com.miaxis.face.service.ReadIdService;
import com.miaxis.face.util.LogUtil;
import com.miaxis.face.util.YuvUtil;
import com.miaxis.face.view.custom.ResultLayout;

import org.zz.faceapi.MXFaceAPI;
import org.zz.faceapi.MXFaceInfo;
import org.zz.idcard_hid_driver.IdCardDriver;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.miaxis.face.constant.Constants.*;

public class MainActivity extends BaseActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_wel_msg)
    TextView tvWelMsg;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_weather)
    TextView tvWeather;
    @BindView(R.id.sv_main)
    SurfaceView svMain;
    @BindView(R.id.sv_rect)
    SurfaceView svRect;
    @BindView(R.id.ll_top)
    LinearLayout llTop;
    @BindView(R.id.rv_result)
    ResultLayout rvResult;

    private Camera mCamera;
    private SurfaceHolder shMain;
    private SurfaceHolder shRect;

    private MXFaceAPI mxFaceAPI;
    private IdCardDriver idCardDriver;          // 二代证

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initData();
        initView();
        initSurface();
        startReadId();
    }

    void initData() {
        mxFaceAPI = Face_App.getMxAPI();
        idCardDriver = new IdCardDriver(this);
    }

    void initView() {
        llTop.bringToFront();
        svRect.setZOrderOnTop(true);
        rvResult.bringToFront();
    }

    void initSurface() {
        shMain = svMain.getHolder();
        shMain.addCallback(this);
        shMain.setFormat(SurfaceHolder.SURFACE_TYPE_NORMAL);
        shRect = svRect.getHolder();
        shRect.setFormat(PixelFormat.TRANSLUCENT);
    }

    void openCamera() {
        try {
            mCamera = Camera.open();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);
            parameters.setPreviewFpsRange(20, 30);
            parameters.setPictureSize(PIC_WIDTH, PIC_HEIGHT);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(180);
            mCamera.setPreviewDisplay(shMain);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (Exception e) {
            LogUtil.writeLog("打开摄像头异常" + e.getMessage());
        }
    }

    void closeCamera() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            LogUtil.writeLog("关闭摄像头异常" + e.getMessage());
        }
    }

    void startReadId() {
        ReadIdService.startActionReadId(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        closeCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Canvas canvas = shRect.lockCanvas(null);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (canvas == null) {
            return;
        }
        int[] pFaceNum  = new int[1];
        pFaceNum[0] = MAX_FACE_NUM;
        MXFaceInfo[] pFaceBuffer = new MXFaceInfo[MAX_FACE_NUM];
        for (int i = 0; i < MAX_FACE_NUM; i++) {
            pFaceBuffer[i] = new MXFaceInfo();
        }
        byte[] rotateData = YuvUtil.rotateYUV420Degree180(data, PRE_WIDTH, PRE_HEIGHT);
        int re = mxFaceAPI.mxDetectFaceYUV(rotateData, PRE_WIDTH, PRE_HEIGHT, pFaceNum, pFaceBuffer);
        if (re == 0 && pFaceNum[0] > 0) {
            drawFaceRect(pFaceBuffer, canvas, pFaceNum[0]);
            FaceFeatureService.startExtractFeature(this, null, pFaceNum, pFaceBuffer);
        }
        shRect.unlockCanvasAndPost(canvas);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.tv_title)
    void onTestClick() {
        startActivity(new Intent(this, TestActivity.class));
    }

    /* 画人脸框 */
    private void drawFaceRect(MXFaceInfo[] faceInfos, Canvas canvas, int len) {
        float[] startArrayX = new float[len];
        float[] startArrayY = new float[len];
        float[] stopArrayX = new float[len];
        float[] stopArrayY = new float[len];
        for (int i=0; i<len; i++) {
            startArrayX[i] = (CP_WIDTH - faceInfos[i].x * zoomRate);
            startArrayY[i] = (faceInfos[i].y * zoomRate);
            stopArrayX[i] = (CP_WIDTH - faceInfos[i].x * zoomRate - faceInfos[i].width * zoomRate);
            stopArrayY[i] = (faceInfos[i].y * zoomRate + faceInfos[i].height * zoomRate);
        }
        canvasDrawLine(canvas, len, startArrayX, startArrayY, stopArrayX, stopArrayY);
    }

    /* 画线 */
    private void canvasDrawLine(Canvas canvas, int iNum, float[] startArrayX, float[] startArrayY, float[] stopArrayX, float[] stopArrayY) {
        try {
            int iLen  = 50;
            Paint mPaint = new Paint();
            mPaint.setColor(getResources().getColor(R.color.white));
            float startX, startY, stopX, stopY;
            for (int i = 0; i < iNum; i++) {
                startX = startArrayX[i];
                startY = startArrayY[i];
                stopX = stopArrayX[i];
                stopY = stopArrayY[i];

                mPaint.setStrokeWidth(6);// 设置画笔粗细

                canvas.drawLine(startX,         startY,         startX - iLen,  startY,         mPaint);
                canvas.drawLine(stopX + iLen,   startY,         stopX,          startY,         mPaint);
                canvas.drawLine(startX,         startY,         startX,         startY + iLen,  mPaint);
                canvas.drawLine(startX,         stopY - iLen,   startX,         stopY,          mPaint);
                canvas.drawLine(stopX,          stopY,          stopX,          stopY - iLen,   mPaint);
                canvas.drawLine(stopX,          startY + iLen,  stopX,          startY,         mPaint);
                canvas.drawLine(stopX,          stopY,          stopX + iLen,   stopY,          mPaint);
                canvas.drawLine(startX - iLen,  stopY,          startX,         stopY,          mPaint);

            }
        } catch (Exception e) {
        }
    }

    class ReadIdThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("=========", "readId");
            }
        }
    }
}
