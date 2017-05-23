package com.miaxis.face.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.event.InitCWEvent;
import com.miaxis.face.event.ReInitEvent;
import com.miaxis.face.util.LogUtil;
import com.miaxis.face.view.custom.GifView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoadingActivity extends BaseActivity {

    @BindView(R.id.tv_loading)  TextView tvLoading;
    @BindView(R.id.gif_loading) GifView gifLoading;

    @BindColor(R.color.white)   int white;

    private EventBus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        initWindow();
        ButterKnife.bind(this);

        gifLoading.setMovieResource(R.raw.loading);
        eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onInitCWEvent(InitCWEvent e) {
        switch (e.getResult()) {
            case InitCWEvent.ERR_FILE_COMPARE:
                tvLoading.setText("文件校验失败");
                LogUtil.writeLog("文件校验失败");
                eventBus.post(new ReInitEvent());
                break;
            case InitCWEvent.ERR_LICENCE:
                tvLoading.setText("读取授权文件失败");
                LogUtil.writeLog("读取授权文件失败");
                break;
            case InitCWEvent.INIT_SUCCESS:
                tvLoading.setText("初始化算法成功");
                LogUtil.writeLog("初始化算法成功");
                startActivity(new Intent(this, MainActivity.class));
                break;
            default:
                tvLoading.setText("初始化算法失败");
                LogUtil.writeLog("初始化算法失败" + e.getResult());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventBus.unregister(this);
    }
}
