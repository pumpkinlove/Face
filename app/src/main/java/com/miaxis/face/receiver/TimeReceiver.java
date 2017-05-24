package com.miaxis.face.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.miaxis.face.event.TimeChangeEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/3/3 0003.
 */

public class TimeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
            EventBus.getDefault().post(new TimeChangeEvent());
        }
    }
}
