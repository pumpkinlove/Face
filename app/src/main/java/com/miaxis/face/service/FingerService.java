package com.miaxis.face.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Base64;

import com.miaxis.face.bean.Record;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.event.ResultEvent;

import org.greenrobot.eventbus.EventBus;
import org.zz.jni.mxImageLoad;
import org.zz.jni.zzFingerAlg;
import org.zz.mxhidfingerdriver.MXFingerDriver;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FingerService extends IntentService {

    private static final String ACTION_FINGER = "com.miaxis.face.service.action.FINGER";

    private static final String RECORD = "com.miaxis.face.service.extra.RECORD";

    public FingerService() {
        super("FingerService");
    }

    public static void startActionFinger(Context context, Record record) {
        Intent intent = new Intent(context, FingerService.class);
        intent.setAction(ACTION_FINGER);
        intent.putExtra(RECORD, record);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FINGER.equals(action)) {
                final Record record = (Record) intent.getSerializableExtra(RECORD);
                handleActionFinger(record);
            }
        }
    }

    private void handleActionFinger(Record record) {
        zzFingerAlg alg = new zzFingerAlg();
        MXFingerDriver fingerDriver = new MXFingerDriver(this);
        int re;
        byte[] bImgBuf = new byte[Constants.IMAGE_SIZE_BIG];
        re = fingerDriver.mxAutoGetImage(bImgBuf, Constants.IMAGE_X_BIG, Constants.IMAGE_Y_BIG, Constants.TIME_OUT, 0);
        if (re == 0) {
            byte[] printFingerFeature = new byte[Constants.TZ_SIZE];
            re = alg.mxGetTz512(bImgBuf, printFingerFeature);
            if (re == 1) {
                re = alg.mxFingerMatch512(Base64.decode(record.getFinger0(), Base64.DEFAULT), printFingerFeature, Constants.LEVEL);
                if (re == 0) {
                    EventBus.getDefault().post(new ResultEvent(ResultEvent.FINGER_SUCCESS, record));
                    return;
                } else {
                    re = alg.mxFingerMatch512(Base64.decode(record.getFinger1(), Base64.DEFAULT), printFingerFeature, Constants.LEVEL);
                    if (re == 0) {
                        EventBus.getDefault().post(new ResultEvent(ResultEvent.FINGER_SUCCESS, record));
                        return;
                    }
                }
            }
        }
        EventBus.getDefault().post(new ResultEvent(ResultEvent.FAIL, record));
    }

}
