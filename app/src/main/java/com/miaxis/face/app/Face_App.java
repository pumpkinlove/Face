package com.miaxis.face.app;

import android.app.Application;
import android.app.smdt.SmdtManager;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.miaxis.face.event.InitCWEvent;
import com.miaxis.face.event.ReInitEvent;
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

    @Override
    public void onCreate() {
        super.onCreate();

        initData();
        initCW();
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
                eventBus.post(new InitCWEvent(re));
            }
        }).start();
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
