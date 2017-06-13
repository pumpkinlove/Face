package com.miaxis.face.app;

import android.app.Application;
import android.app.smdt.SmdtManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.miaxis.face.bean.Config;
import com.miaxis.face.bean.Record;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.event.InitCWEvent;
import com.miaxis.face.event.ReInitEvent;
import com.miaxis.face.event.TimerResetEvent;
import com.miaxis.face.greendao.gen.ConfigDao;
import com.miaxis.face.greendao.gen.DaoMaster;
import com.miaxis.face.greendao.gen.DaoSession;
import com.miaxis.face.greendao.gen.RecordDao;
import com.miaxis.face.service.ClearService;
import com.miaxis.face.service.UpLoadRecordService;
import com.miaxis.face.util.FileUtil;
import com.miaxis.face.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.zz.faceapi.MXFaceAPI;


import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.OnClick;
import cn.cloudwalk.sdk.ConStant;

import static com.miaxis.face.constant.Constants.MAX_COUNT;

/**
 * Created by Administrator on 2017/5/16 0016.
 */

public class Face_App extends Application {

    private static MXFaceAPI mxAPI;
    private SmdtManager smdtManager;
    private EventBus eventBus;
    private static ConfigDao configDao;
    private static RecordDao recordDao;
    private static Config config;
    private static Timer timer;
    public static TimerTask timerTask;

    @Override
    public void onCreate() {
        super.onCreate();

        initData();
        initDbHelp();
        initConfig();
        smdtManager.smdtSetExtrnalGpioValue(2, true);
        startTask();
        initCW();
        ClearService.startActionClear(getApplicationContext());
    }

    void initData() {
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        mxAPI = new MXFaceAPI();
        smdtManager = new SmdtManager(this);
    }

    void initCW() {
        if (!readLicence()) {
            eventBus.postSticky(new InitCWEvent(InitCWEvent.ERR_LICENCE));
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int re = mxAPI.mxInitAlg(getApplicationContext(), null, false);
                eventBus.postSticky(new InitCWEvent(re));
            }
        }).start();
    }

    private void initDbHelp() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(new GreenDaoContext(this), "Face.db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        configDao = daoSession.getConfigDao();
        recordDao = daoSession.getRecordDao();
    }

    public static void initConfig() {
        try {
            config = configDao.loadByRowId(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (config == null) {
            config = new Config();
            config.setId(1);
            config.setIp(Constants.DEFAULT_IP);
            config.setPort(Constants.DEFAULT_PORT);
            config.setIntervalTime(Constants.DEFAULT_INTERVAL);
            config.setBanner(Constants.DEFAULT_BANNER);
            config.setUpTime(Constants.DEFAULT_UPTIME);
            config.setPassScore(Constants.PASS_SCORE);
            config.setFingerFlag(Constants.DEFAULT_FINGER);
            config.setNetFlag(Constants.DEFAULT_NET);
            config.setQueryFlag(Constants.DEFAULT_NET);
            config.setPassword(Constants.DEFAULT_PASSWORD);
            configDao.insert(config);
        }
    }

    boolean readLicence() {
        ConStant.sLicence = FileUtil.readLicence();
        if (TextUtils.isEmpty(ConStant.sLicence)) {
            Toast.makeText(this, "读取授权文件失败", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mxAPI.mxFreeAlg();
    }

    public static MXFaceAPI getMxAPI() {
        return mxAPI;
    }

    public static ConfigDao getConfigDao() {
        return configDao;
    }

    public static RecordDao getRecordDao() {
        return recordDao;
    }

    public static Config getConfig() {
        initConfig();
        return config;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReInitEvent(ReInitEvent e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.writeLog("重新初始化算法");
                File oldFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ZZFaceModels");
                File newFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ZZFaceModels_Error");
                if (newFile.exists()) {
                    FileUtil.delDirectory(newFile);
                }
                if (oldFile.renameTo(newFile)) {
                    LogUtil.writeLog("错误文件备份为：" + newFile.getName());
                } else {
                    LogUtil.writeLog("错误文件备份失败");
                }
                mxAPI.mxFreeAlg();
                int re = mxAPI.mxInitAlg(getApplicationContext(), null, true);
                eventBus.post(new InitCWEvent(re));
            }
        }).start();
    }

    public static final int GROUP_SIZE = 100;

    private void upLoad() {
        long count = recordDao.count();
        long page = (count % GROUP_SIZE == 0) ? count / GROUP_SIZE : (count / GROUP_SIZE + 1);
        for (int i = 0; i < page; i ++) {
            List<Record> recordList = recordDao.queryBuilder().offset(i * GROUP_SIZE).limit(GROUP_SIZE).orderAsc(RecordDao.Properties.Id).list();
            for (int j=0; j<recordList.size(); j++) {
                Record record = recordList.get(j);
                if (!record.isHasUp()) {
                    Log.e("UpLoad", "===========  " + record.getName());
                    UpLoadRecordService.startActionFoo(getApplicationContext(), record, config);
                }
            }
        }
    }

    private void startTask() {
        initTask();
        Date start = new Date();
        start.setHours(Integer.valueOf(config.getUpTime().split(" : ")[0]));
        start.setMinutes(Integer.valueOf(config.getUpTime().split(" : ")[1]));
        long tStart = start.getTime();
        long t1 = new Date().getTime();
        if (tStart < t1) {
            start.setDate(new Date().getDate() + 1);
        }
        timer.schedule(timerTask, start, Constants.TASK_DELAY);
    }

    private void initTask() {
        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (config.isNetFlag()) {
                    upLoad();
                }
                ClearService.startActionClear(getApplicationContext());
            }
        };
    }

    public void reSetTimer() {
        timerTask.cancel();
        initTask();
        timer.cancel();
        timer.purge();
        timer = new Timer();
        Date start = new Date();
        start.setHours(Integer.valueOf(config.getUpTime().split(" : ")[0]));
        start.setMinutes(Integer.valueOf(config.getUpTime().split(" : ")[1]));
        start.setSeconds(0);
        long tStart = start.getTime();
        long t1 = new Date().getTime();
        if (tStart < t1) {
            start.setDate(new Date().getDate() + 1);
        }
        timer.schedule(timerTask, start, Constants.TASK_DELAY);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerResetEvent(TimerResetEvent e) {
        reSetTimer();
    }

}