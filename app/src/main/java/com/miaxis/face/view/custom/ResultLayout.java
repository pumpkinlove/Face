package com.miaxis.face.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.event.ResultEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/5/22 0022.
 */

public class ResultLayout extends LinearLayout {

    @BindView(R.id.gif_finger)
    GifView gifFinger;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.iv_camera_photo)
    ImageView ivCameraPhoto;
    @BindView(R.id.iv_result)
    ImageView ivResult;
    @BindView(R.id.iv_id_photo)
    ImageView ivIdPhoto;

    private EventBus eventBus;

    public ResultLayout(Context context) {
        super(context);
        init();
    }

    public ResultLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResultLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    private void init() {
        View v = inflate(getContext(), R.layout.view_result, this);
        ButterKnife.bind(this, v);

        bringToFront();
        gifFinger.setMovieResource(R.raw.put_finger);

        eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(ResultEvent e) {
    }

}
