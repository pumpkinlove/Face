package com.miaxis.face.view.activity;

import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.miaxis.face.R;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Config;
import com.miaxis.face.bean.Record;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.event.DrawRectEvent;
import com.miaxis.face.event.HasCardEvent;
import com.miaxis.face.event.NoCardEvent;
import com.miaxis.face.event.ResultEvent;
import com.miaxis.face.event.TimeChangeEvent;
import com.miaxis.face.greendao.gen.RecordDao;
import com.miaxis.face.receiver.TimeReceiver;
import com.miaxis.face.service.FingerService;
import com.miaxis.face.service.UpLoadRecordService;
import com.miaxis.face.util.DateUtil;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.miaxis.face.constant.Constants.CP_WIDTH;
import static com.miaxis.face.constant.Constants.GET_CARD_ID;
import static com.miaxis.face.constant.Constants.GPIO_INTERVAL;
import static com.miaxis.face.constant.Constants.LEFT_VOLUME;
import static com.miaxis.face.constant.Constants.LOOP;
import static com.miaxis.face.constant.Constants.MAX_FACE_NUM;
import static com.miaxis.face.constant.Constants.NO_CARD;
import static com.miaxis.face.constant.Constants.PHOTO_SIZE;
import static com.miaxis.face.constant.Constants.PIC_HEIGHT;
import static com.miaxis.face.constant.Constants.PIC_WIDTH;
import static com.miaxis.face.constant.Constants.PRE_HEIGHT;
import static com.miaxis.face.constant.Constants.PRE_WIDTH;
import static com.miaxis.face.constant.Constants.PRIORITY;
import static com.miaxis.face.constant.Constants.RIGHT_VOLUME;
import static com.miaxis.face.constant.Constants.SOUND_RATE;
import static com.miaxis.face.constant.Constants.mFingerDataSize;
import static com.miaxis.face.constant.Constants.zoomRate;

public class MainActivity extends BaseActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, AMapLocationListener, WeatherSearch.OnWeatherSearchListener {

    long at1;
    long at2;

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
    @BindView(R.id.tv_pass)
    TextView tvPass;
    @BindView(R.id.iv_record)
    ImageView ivRecord;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.btn_cancel)
    Button btnCancel;

    private Record mRecord;

    private Camera mCamera;
    private SurfaceHolder shMain;
    private SurfaceHolder shRect;

    private SoundPool soundPool;
    private Map<Integer, Integer> soundMap;

    private MXFaceAPI mxFaceAPI;
    private IdCardDriver idCardDriver;          // 二代证
    private mxImageLoad dtload;                 // 加载图像
    public AMapLocationClient mLocationClient;
    private WeatherSearchQuery mQuery;
    private WeatherSearch mWeatherSearch;
    private SmdtManager smdtManager;
    private EventBus eventBus;
    private TimeReceiver timeReceiver;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();  //用来进行特征提取的线程池

    private boolean isExtractWorking;
    private boolean detectFlag;
    private boolean extractFlag;
    private boolean matchFlag;

    private byte[] idFaceFeature;               // 二代证照片 人脸特征
    private byte[] curFaceFeature;
    private byte[] curCameraImg;
    private MXFaceInfo curFaceInfo;

    private double latitude;
    private double longitude;
    private String location;

    private Config config;
    private RecordDao recordDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initWindow();
        initData();
        initView();
        initSurface();
        initAMapSDK();
        initTimeReceiver();
        startReadId();
        startMonitor();
    }

    void initData() {
        mxFaceAPI = Face_App.getMxAPI();
        idCardDriver = new IdCardDriver(this);
        smdtManager = SmdtManager.create(this);
        dtload = new mxImageLoad();
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        recordDao = Face_App.getRecordDao();

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundMap = new HashMap<>();
        soundMap.put(1, soundPool.load(this, R.raw.success, 1));
        soundMap.put(2, soundPool.load(this, R.raw.fail, 1));
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

    void initAMapSDK() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(this);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setInterval(1000 * 30 * 1);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    void initTimeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        timeReceiver = new TimeReceiver();
        registerReceiver(timeReceiver, filter);
        onTimeEvent(null);
    }

    void openCamera() {
        try {
            mCamera = Camera.open();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
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

    void startMonitor() {
        Thread monitorThread = new MonitorThread();
        monitorThread.start();
    }

    void playSound(int soundID) {
        soundPool.play(soundMap.get(soundID), LEFT_VOLUME, RIGHT_VOLUME, PRIORITY, LOOP, SOUND_RATE);
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
        lastCallBackTime = System.currentTimeMillis();
        if (!detectFlag) {
            return;
        }
        int[] pFaceNum = new int[1];
        pFaceNum[0] = MAX_FACE_NUM;
        MXFaceInfo[] pFaceBuffer = new MXFaceInfo[MAX_FACE_NUM];
        for (int i = 0; i < MAX_FACE_NUM; i++) {
            pFaceBuffer[i] = new MXFaceInfo();
        }
        byte[] rotateData = YuvUtil.rotateYUV420Degree180(data, PRE_WIDTH, PRE_HEIGHT);
        int re;
        synchronized (lock2) {
            re = mxFaceAPI.mxDetectFaceYUV(rotateData, PRE_WIDTH, PRE_HEIGHT, pFaceNum, pFaceBuffer);
        }
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
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (e.getFaceNum() != 0) {
            drawFaceRect(e.getFaceInfos(), canvas, e.getFaceNum());
        }
        shRect.unlockCanvasAndPost(canvas);
    }

    @Override
    protected void onResume() {
        super.onResume();
        config = Face_App.getConfig();
        if (config.isQueryFlag()) {
            ivRecord.setVisibility(View.VISIBLE);
        } else {
            ivRecord.setVisibility(View.INVISIBLE);
        }
        tvWelMsg.setText(config.getBanner());
        monitorFlag = true;
        readIdFlag = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        monitorFlag = false;
        readIdFlag = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventBus.unregister(this);
        unregisterReceiver(timeReceiver);
    }

    /* 画人脸框 */
    void drawFaceRect(MXFaceInfo[] faceInfos, Canvas canvas, int len) {
        float[] startArrayX = new float[len];
        float[] startArrayY = new float[len];
        float[] stopArrayX = new float[len];
        float[] stopArrayY = new float[len];
        for (int i = 0; i < len; i++) {
            startArrayX[i] = (CP_WIDTH - faceInfos[i].x * zoomRate);
            startArrayY[i] = (faceInfos[i].y * zoomRate);
            stopArrayX[i] = (CP_WIDTH - faceInfos[i].x * zoomRate - faceInfos[i].width * zoomRate);
            stopArrayY[i] = (faceInfos[i].y * zoomRate + faceInfos[i].height * zoomRate);
        }
        canvasDrawLine(canvas, len, startArrayX, startArrayY, stopArrayX, stopArrayY);
    }

    /* 画线 */
    void canvasDrawLine(Canvas canvas, int iNum, float[] startArrayX, float[] startArrayY, float[] stopArrayX, float[] stopArrayY) {
        int iLen = 50;
        Paint mPaint = new Paint();
        mPaint.setColor(white);
        float startX, startY, stopX, stopY;
        for (int i = 0; i < iNum; i++) {
            startX = startArrayX[i];
            startY = startArrayY[i];
            stopX = stopArrayX[i];
            stopY = stopArrayY[i];
            mPaint.setStrokeWidth(6);// 设置画笔粗细
            canvas.drawLine(startX, startY, startX - iLen, startY, mPaint);
            canvas.drawLine(stopX + iLen, startY, stopX, startY, mPaint);
            canvas.drawLine(startX, startY, startX, startY + iLen, mPaint);
            canvas.drawLine(startX, stopY - iLen, startX, stopY, mPaint);
            canvas.drawLine(stopX, stopY, stopX, stopY - iLen, mPaint);
            canvas.drawLine(stopX, startY + iLen, stopX, startY, mPaint);
            canvas.drawLine(stopX, stopY, stopX + iLen, stopY, mPaint);
            canvas.drawLine(startX - iLen, stopY, startX, stopY, mPaint);
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                latitude = aMapLocation.getLatitude();
                longitude = aMapLocation.getLongitude();
                location = aMapLocation.getAddress();
                queryWeather(aMapLocation.getCity());
            } else {
//                tv_weather.setText("无天气信息");
                Log.e("AMapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        } else {
            tvWeather.setText("无天气信息");
        }
    }

    void queryWeather(String city) {
        mQuery = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        mWeatherSearch = new WeatherSearch(this);
        mWeatherSearch.setOnWeatherSearchListener(this);
        mWeatherSearch.setQuery(mQuery);
        mWeatherSearch.searchWeatherAsyn(); //异步搜索
    }

    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
        if (i == 1000) {
            if (localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherLive = localWeatherLiveResult.getLiveResult();
                tvWeather.setText(weatherLive.getWeather() + weatherLive.getTemperature() + "℃");
            } else {
                tvWeather.setText("无天气信息");
            }
        } else {
            tvWeather.setText("无天气信息");
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }

    private boolean readIdFlag = true;
    private boolean noCardFlag = false;

    class ReadIdThread extends Thread {
        @Override
        public void run() {
            byte[] lastCardId = null;
            byte[] curCardId;
            int re;
            while (true) {
                if (!readIdFlag) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                curCardId = new byte[64];
                re = idCardDriver.mxReadCardId(curCardId);
                switch (re) {
                    case GET_CARD_ID:
                        eventBus.post(new HasCardEvent());
                        at1 = System.currentTimeMillis();
                        noCardFlag = false;
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
                        if (!noCardFlag) {
                            eventBus.post(new NoCardEvent());
                        }
                        noCardFlag = true;
                        lastCardId = null;
                        break;
                }
            }
        }
    }

    /* 线程 从视频流中提取特征并比对 */
    class ExtractAndMatch implements Runnable {
        private byte[] pCameraData = null;
        private MXFaceInfo[] pFaceBuffer = null;

        public ExtractAndMatch(byte[] pCameraData, MXFaceInfo[] pFaceBuffer) {
            this.pCameraData = pCameraData;
            this.pFaceBuffer = pFaceBuffer;
        }

        @Override
        public void run() {
            long t1 = System.currentTimeMillis();
            curFaceFeature = extractFeature(pFaceBuffer[0]);
            curCameraImg = pCameraData;
            curFaceInfo = pFaceBuffer[0];
            extractFlag = false;
            long t2 = System.currentTimeMillis();
            Log.e("mxFeatureExtract", "耗时：" + (t2 - t1));
            if (curFaceFeature != null && matchFlag) {
                float[] fScore = new float[1];
                int re = mxFaceAPI.mxFeatureMatch(idFaceFeature, curFaceFeature, fScore);
                if (re == 0 && fScore[0] >= config.getPassScore()) {
                    mRecord.setFaceImg(MyUtil.getYUVBase64(curCameraImg, mCamera.getParameters().getPreviewFormat()));
                    eventBus.post(new ResultEvent(ResultEvent.FACE_SUCCESS, mRecord, pFaceBuffer[0]));
                    Log.e("MatchRunnable___", "验证通过 " + re + " _" + fScore[0]);
                } else {
                    mRecord.setFaceImg(MyUtil.getYUVBase64(curCameraImg, mCamera.getParameters().getPreviewFormat()));
                    if (config.isFingerFlag() && mRecord.getFinger0() != null && mRecord.getFinger0().length() > 0) {
                        eventBus.post(new ResultEvent(ResultEvent.FACE_FAIL_HAS_FINGER, mRecord, pFaceBuffer[0]));
                        FingerService.startActionFinger(getApplicationContext(), mRecord);
                    } else {
                        eventBus.post(new ResultEvent(ResultEvent.FAIL, mRecord, pFaceBuffer[0]));
                    }
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
        Log.e("=====", "readCard");
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
        mRecord.setAddress(MyUtil.unicode2String(id_Home).trim());

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

    private final Byte lock1 = 1;
    private final Byte lock2 = 2;

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
        synchronized (lock2) {
            re = mxFaceAPI.mxDetectFace(pGrayBuff, iX, iY, pFaceNum, pFaceBuffer);
        }
        if (re != 0) {
            return;
        }

        idFaceFeature = extractFeature(pFaceBuffer[0]);

        if (curFaceFeature != null) {
            float[] fScore = new float[1];
            re = mxFaceAPI.mxFeatureMatch(idFaceFeature, curFaceFeature, fScore);
            if (re == 0 && fScore[0] >= config.getPassScore()) {
                at2 = System.currentTimeMillis();
                Log.e("------", "" + (at2 - at1));
                Log.e("预读___", "验证通过 " + re + " _" + fScore[0]);
                mRecord.setFaceImg(MyUtil.getYUVBase64(curCameraImg, mCamera.getParameters().getPreviewFormat()));
                matchFlag = false;
                extractFlag = false;
                eventBus.post(new ResultEvent(ResultEvent.FACE_SUCCESS, mRecord, curFaceInfo));
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(ResultEvent e) {
        Record record = e.getRecord();
        switch (e.getResult()) {
            case ResultEvent.FACE_SUCCESS:
                record.setStatus("人脸通过");
                playSound(1);
                break;
            case ResultEvent.FINGER_SUCCESS:
                record.setStatus("指纹通过");
                playSound(1);
                break;
            case ResultEvent.FAIL:
                record.setStatus("失败");
                playSound(2);
                break;
            default:
                return;
        }
        record.setCreateDate(new Date());
        record.setDevsn(MyUtil.getSerialNumber());
        record.setBusEntity(config.getOrgName());
        record.setLocation(location);
        record.setLatitude(latitude + "");
        record.setLongitude(longitude + "");
        recordDao.insert(record);
        if (config.isNetFlag()) {
            UpLoadRecordService.startActionFoo(this, record, config);
        }
    }

    /* 处理 时间变化 事件， 实时更新时间*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimeEvent(TimeChangeEvent e) {
        DateFormat dateFormat = new SimpleDateFormat("E  yyyy-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(new Date());
        String time = timeFormat.format(new Date());
        tvTime.setText(time);
        tvDate.setText(date);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Constants.RESULT_CODE_FINISH) {
                finish();
            }
        }
    }

    private long lastCallBackTime = 9999999999999L;
    private boolean monitorFlag = true;

    class MonitorThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(1000);
                    if (monitorFlag) {
                        long cur = new Date().getTime();
                        if ((cur - lastCallBackTime) >= config.getIntervalTime() * 1000) {
                            if (mCamera != null) {
                                mCamera.stopPreview();
                                mCamera.setPreviewCallback(MainActivity.this);
                                mCamera.startPreview();
                                LogUtil.writeLog("修复视频卡顿");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.writeLog("修复视频卡顿线程" + e.getMessage());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoCardEvent(NoCardEvent e) {
        tvPass.setVisibility(View.VISIBLE);
        detectFlag = false;
        extractFlag = false;
        matchFlag = false;
        idFaceFeature = null;
        curFaceFeature = null;
        onRect(new DrawRectEvent(0, null));
        closeLed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHasCardEvent(HasCardEvent e) {
        tvPass.setVisibility(View.GONE);
    }

    @OnClick(R.id.iv_record)
    void onRecord() {
        toType = 1;
        etPwd.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        btnConfirm.setVisibility(View.VISIBLE);
        tvWelMsg.setVisibility(View.GONE);
    }

    private int mState = 0;
    private long firstTime = 0;

    @OnClick(R.id.tv_title)
    void onTitleClick() {
        long secondTime = System.currentTimeMillis();
        if ((secondTime - firstTime) > 1500) {
            mState = 0;
        } else {
            mState++;
        }
        firstTime = secondTime;
        Log.e("mState", mState + "");
        if (mState > 4) {
            toType = 0;
            etPwd.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
            tvWelMsg.setVisibility(View.GONE);
        } else {
            onCancel();
        }
    }

    private int toType;             // 0 SettingActivity   1 RecordActivity

    @OnClick(R.id.btn_cancel)
    void onCancel() {
        etPwd.setText(null);
        etPwd.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnConfirm.setVisibility(View.GONE);
        tvWelMsg.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_confirm)
    void onConfirm() {
        String pwd = etPwd.getText().toString();
        if (pwd.equals(config.getPassword())) {
            etPwd.setText(null);
            etPwd.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.GONE);
            tvWelMsg.setVisibility(View.VISIBLE);
            if (toType == 0) {
                startActivity(new Intent(this, SettingActivity.class));
            } else if (toType == 1) {
                startActivity(new Intent(this, RecordActivity.class));
            }
        } else {
            Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            etPwd.setText(null);
        }
    }
}
