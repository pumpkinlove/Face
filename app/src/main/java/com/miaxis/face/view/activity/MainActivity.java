package com.miaxis.face.view.activity;

import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Record;
import com.miaxis.face.event.DrawRectEvent;
import com.miaxis.face.event.ResultEvent;
import com.miaxis.face.util.FileUtil;
import com.miaxis.face.util.LogUtil;
import com.miaxis.face.util.MyUtil;
import com.miaxis.face.util.YuvUtil;
import com.miaxis.face.view.custom.ResultLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.zz.faceapi.MXFaceAPI;
import org.zz.faceapi.MXFaceInfo;
import org.zz.idcard_hid_driver.IdCardDriver;
import org.zz.jni.mxImageLoad;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindColor;
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
    @BindColor(R.color.white)
    int white;

    private Record mRecord;

    private Camera mCamera;
    private SurfaceHolder shMain;
    private SurfaceHolder shRect;

    private MXFaceAPI mxFaceAPI;
    private IdCardDriver idCardDriver;          // 二代证
    private mxImageLoad dtload;                 // 加载图像
    private SmdtManager smdtManager;
    private EventBus eventBus;

    private byte[] idFaceFeature;               // 二代证照片 人脸特征

    private ExecutorService executorService = Executors.newSingleThreadExecutor();  //用来进行特征提取的线程池

    private boolean isExtractWorking;
    private boolean detectFlag;
    private boolean extractFlag;
    private boolean matchFlag;

    private byte[] curFaceFeature;
    private byte[] curCameraImg;

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
        smdtManager = SmdtManager.create(this);
        dtload = new mxImageLoad();
        eventBus = EventBus.getDefault();
        eventBus.register(this);
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
        Thread t = new ReadIdThread();
        t.start();
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
        if (!detectFlag) {
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
            eventBus.post(new DrawRectEvent(pFaceNum[0], pFaceBuffer));
            if (!isExtractWorking && extractFlag) {
                isExtractWorking = true;
                ExtractAndMatch matchRunnable = new ExtractAndMatch(rotateData, pFaceBuffer);
                executorService.submit(matchRunnable);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onRect(DrawRectEvent e) {
        Canvas canvas = shRect.lockCanvas(null);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawFaceRect(e.getFaceInfos(), canvas, e.getFaceNum());
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
        eventBus.unregister(this);
    }

    @OnClick(R.id.tv_title)
    void onTestClick() {
        startActivity(new Intent(this, TestActivity.class));
    }

    /* 画人脸框 */
    void drawFaceRect(MXFaceInfo[] faceInfos, Canvas canvas, int len) {
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
    void canvasDrawLine(Canvas canvas, int iNum, float[] startArrayX, float[] startArrayY, float[] stopArrayX, float[] stopArrayY) {
        int iLen  = 50;
        Paint mPaint = new Paint();
        mPaint.setColor(white);
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
    }

    class ReadIdThread extends Thread {
        @Override
        public void run() {
            byte[] lastCardId = null;
            byte[] curCardId;
            int re;
            while (true) {
                curCardId = new byte[64];
                re = idCardDriver.mxReadCardId(curCardId);
                switch (re) {
                    case GET_CARD_ID:
                        openLed();
                        if (!Arrays.equals(lastCardId, curCardId)) {
                            detectFlag = true;
                            extractFlag = true;
                            mRecord = new Record();
                            mRecord.setCardId(getCardIdStr(curCardId));
                            readCard();
                            getIdPhotoFeature();
                        }
                        lastCardId = curCardId;
                        break;
                    case NO_CARD:
                        closeLed();
                        lastCardId = null;
                        break;
                }
            }
        }
    }

    /* 线程 从视频流中提取特征并比对 */
    class ExtractAndMatch implements Runnable {
        private byte[] pCameraData = null;
        private MXFaceInfo[]  pFaceBuffer = null;

        public ExtractAndMatch(byte[] pCameraData, MXFaceInfo[] pFaceBuffer) {
            this.pCameraData = pCameraData;
            this.pFaceBuffer = pFaceBuffer;
        }

        @Override
        public void run() {
            long t1 = System.currentTimeMillis();
            curFaceFeature = extractFeature(pFaceBuffer[0]);
            curCameraImg = pCameraData;
            extractFlag = false;
            long t2 = System.currentTimeMillis();
            Log.e("mxFeatureExtract", "耗时：" + (t2 - t1));
            if (curFaceFeature != null && matchFlag) {
                float[] fScore = new float[1];
                int re = mxFaceAPI.mxFeatureMatch(idFaceFeature, curFaceFeature, fScore);
                if (re == 0 && fScore[0] >= PASS_SCORE ) {
                    Log.e("MatchRunnable___", "验证通过 " + re + " _" + fScore[0]);
                } else {
                    Log.e("MatchRunnable___", "验证失败 " + re + " _" + fScore[0]);
                }
                matchFlag = false;
            }
            isExtractWorking = false;
        }
    }

    void openLed() {
        try {
            Thread.sleep(GPIO_INTERVAL);
            smdtManager.smdtSetExtrnalGpioValue(3, true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void closeLed() {
        try {
            Thread.sleep(GPIO_INTERVAL);
            smdtManager.smdtSetExtrnalGpioValue(3, false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    String getCardIdStr(byte[] cardId) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cardId.length; i++) {
            if (cardId[i] == 0x00)
                break;
            if (i == 0) {
                sb.append(String.format("%02x ", cardId[i]));
            } else {
                sb.append(String.format("%02x ", cardId[i]));
            }
        }
        return sb.toString();
    }

    void readCard() {
        byte[] bCardFullInfo = new byte[256 + 1024 + 1024];
        int re = idCardDriver.mxReadCardFullInfo(bCardFullInfo);
        if (re == 1) {
            analysisIdCardInfo(bCardFullInfo);
        } else if (re == 0) {
            analysisIdCardInfo(bCardFullInfo);
            byte[] bFingerData0 = new byte[mFingerDataSize];
            byte[] bFingerData1 = new byte[mFingerDataSize];
            int iLen = 256 + 1024;
            System.arraycopy(bCardFullInfo, iLen, bFingerData0, 0, bFingerData0.length);
            iLen += 512;
            System.arraycopy(bCardFullInfo, iLen, bFingerData1, 0, bFingerData1.length);
            mRecord.setFinger0(Base64.encodeToString(bFingerData0, Base64.DEFAULT));
            mRecord.setFinger1(Base64.encodeToString(bFingerData1, Base64.DEFAULT));
        }
        eventBus.post(new ResultEvent(ResultEvent.ID_PHOTO, mRecord));
    }

    /* 解析身份证信息 */
    void analysisIdCardInfo(byte[] bCardInfo) {
        byte[] id_Name = new byte[30]; // 姓名
        byte[] id_Sex = new byte[2]; // 性别 1为男 其他为女
        byte[] id_Rev = new byte[4]; // 民族
        byte[] id_Born = new byte[16]; // 出生日期
        byte[] id_Home = new byte[70]; // 住址
        byte[] id_Code = new byte[36]; // 身份证号
        byte[] _RegOrg = new byte[30]; // 签发机关
        byte[] id_ValidPeriodStart = new byte[16]; // 有效日期 起始日期16byte 截止日期16byte
        byte[] id_ValidPeriodEnd = new byte[16];
        byte[] id_NewAddr = new byte[36]; // 预留区域
        byte[] id_pImage = new byte[1024]; // 图片区域
        int iLen = 0;
        System.arraycopy(bCardInfo, iLen, id_Name, 0, id_Name.length);
        iLen = iLen + id_Name.length;
        mRecord.setName(MyUtil.unicode2String(id_Name).trim());

        System.arraycopy(bCardInfo, iLen, id_Sex, 0, id_Sex.length);
        iLen = iLen + id_Sex.length;

        if (id_Sex[0] == '1') {
            mRecord.setSex("男");
        } else {
            mRecord.setSex("女");
        }

        System.arraycopy(bCardInfo, iLen, id_Rev, 0, id_Rev.length);
        iLen = iLen + id_Rev.length;
//        int iRev = Integer.parseInt(MyUtil.unicode2String(id_Rev));
//        mRecord.setRace(FOLK[iRev - 1]);

        System.arraycopy(bCardInfo, iLen, id_Born, 0, id_Born.length);
        iLen = iLen + id_Born.length;
        mRecord.setBirthday(MyUtil.unicode2String(id_Born));

        System.arraycopy(bCardInfo, iLen, id_Home, 0, id_Home.length);
        iLen = iLen + id_Home.length;
//        mRecord.setAddress(MyUtil.unicode2String(id_Home).trim());

        System.arraycopy(bCardInfo, iLen, id_Code, 0, id_Code.length);
        iLen = iLen + id_Code.length;
        mRecord.setCardNo(MyUtil.unicode2String(id_Code).trim());

        System.arraycopy(bCardInfo, iLen, _RegOrg, 0, _RegOrg.length);
        iLen = iLen + _RegOrg.length;
//        curId.setRegOrg(CommonUtil.unicode2String(_RegOrg).trim());

        System.arraycopy(bCardInfo, iLen, id_ValidPeriodStart, 0, id_ValidPeriodStart.length);
        iLen = iLen + id_ValidPeriodStart.length;
        System.arraycopy(bCardInfo, iLen, id_ValidPeriodEnd, 0, id_ValidPeriodEnd.length);
        iLen = iLen + id_ValidPeriodEnd.length;
//        curId.setValidTime(CommonUtil.unicode2String(id_ValidPeriodStart) + "-" + CommonUtil.unicode2String(id_ValidPeriodEnd).trim());

        System.arraycopy(bCardInfo, iLen, id_NewAddr, 0, id_NewAddr.length);
        iLen = iLen + id_NewAddr.length;
        System.arraycopy(bCardInfo, iLen, id_pImage, 0, id_pImage.length);
        byte[] bmp = new byte[PHOTO_SIZE];
        int re = idCardDriver.Wlt2Bmp(id_pImage, bmp);
        if (re == 0) {
            FileUtil.writeBytesToFile(bmp, FileUtil.getAvailableImgPath(this), mRecord.getCardNo() + "_" + mRecord.getName() + ".jpg");
            String idPhoto64 = Base64.encodeToString(bmp, Base64.DEFAULT);
            mRecord.setCardImg(idPhoto64);
        }

    }

    private Byte lock1 = 1;

    byte[] extractFeature(MXFaceInfo faceInfo) {
        synchronized (lock1) {
            byte[] feature = new byte[mxFaceAPI.mxGetFeatureSize()];
            detectFlag = false;
            int re = mxFaceAPI.mxFeatureExtract(null, 0, 0, faceInfo, feature);
            detectFlag = true;
            if (re == 0) {
                return feature;
            }
            return null;
        }
    }

    private void getIdPhotoFeature() {
        /** 加载图像 */
        int re = -1;
        int[] oX = new int[1];
        int[] oY = new int[1];
        // 获取图像大小
        String availablePath = FileUtil.getAvailableImgPath(this);
        File f = new File(availablePath, mRecord.getCardNo() + "_" + mRecord.getName() + ".jpg");
        Date d1 = new Date();
        re = dtload.LoadFaceImage(f.getPath(), null, null, oX, oY);
        if (re != 1) {
            return;
        }
        byte[] pGrayBuff = new byte[oX[0] * oY[0]];
        byte[] pRGBBuff = new byte[oX[0] * oY[0] * 3];
        re = dtload.LoadFaceImage(f.getPath(), pRGBBuff, pGrayBuff, oX, oY);
        if (re != 1) {
            return;
        }
        Date d2 = new Date();
        Log.e("身份证_", "_加载图像耗时__" + (d2.getTime() - d1.getTime()));
        /** 检测人脸 */
        int[] pFaceNum = new int[1];
        pFaceNum[0] = 1;                //身份证照片只可能检测到一张人脸
        MXFaceInfo[] pFaceBuffer = new MXFaceInfo[1];
        pFaceBuffer[0] = new MXFaceInfo();
        int iX = oX[0];
        int iY = oY[0];
        re = mxFaceAPI.mxDetectFace(pGrayBuff, iX, iY, pFaceNum, pFaceBuffer);
        if (re != 0) {
            return;
        }

        idFaceFeature = extractFeature(pFaceBuffer[0]);

        if (curFaceFeature != null) {
            float[] fScore = new float[1];
            re = mxFaceAPI.mxFeatureMatch(idFaceFeature, curFaceFeature, fScore);
            if (re == 0 && fScore[0] >= PASS_SCORE) {
                eventBus.post(new ResultEvent(ResultEvent.FACE_SUCCESS, mRecord));
                Log.e("预读___", "验证通过 " + re + " _" + fScore[0]);
                matchFlag = false;
                extractFlag = false;
            } else {
                Log.e("预读___", "验证失败 " + re + " _" + fScore[0]);
                extractFlag = true;
                matchFlag = true;
            }
        } else {
            extractFlag = true;
            matchFlag = true;
        }
    }

}
