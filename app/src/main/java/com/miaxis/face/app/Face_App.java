package com.miaxis.face.app;

import android.app.Application;
import android.app.smdt.SmdtManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.miaxis.face.event.InitCWEvent;
import com.miaxis.face.util.FileUtil;
import com.miaxis.face.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.zz.faceapi.MXFaceAPI;


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
    }

    void initData() {
        eventBus = EventBus.getDefault();
//        eventBus.register(this);
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
            LogUtil.writeLog("读取授权文件失败");
            return false;
        }
        return true;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mxAPI.mxFreeAlg();
    }
}
