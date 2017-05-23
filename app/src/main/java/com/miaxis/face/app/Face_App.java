package com.miaxis.face.app;

import android.app.Application;
import android.app.smdt.SmdtManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.miaxis.face.bean.Config;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.event.InitCWEvent;
import com.miaxis.face.event.ReInitEvent;
import com.miaxis.face.greendao.gen.ConfigDao;
import com.miaxis.face.greendao.gen.DaoMaster;
import com.miaxis.face.greendao.gen.DaoSession;
import com.miaxis.face.greendao.gen.RecordDao;
import com.miaxis.face.util.FileUtil;
import com.miaxis.face.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.zz.faceapi.MXFaceAPI;


import java.io.File;

import cn.cloudwalk.sdk.ConStant;

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

    @Override
    public void onCreate() {
        super.onCreate();

        initData();
        initCW();
        initDbHelp();
        initConfig();
        smdtManager.smdtSetExtrnalGpioValue(2, true);
    }

    void initData() {
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        mxAPI = new MXFaceAPI();
        smdtManager = new SmdtManager(this);
    }

    void initCW() {
        if (!readLicence()) {
            eventBus.post(new InitCWEvent(InitCWEvent.ERR_LICENCE));
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
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "recluse-db", null);
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
}
