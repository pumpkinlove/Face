package com.miaxis.face.view.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.bean.Record;
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
        setVisibility(INVISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(ResultEvent e) {
        Record record = e.getRecord();
        if (record == null) {
            return;
        }
        switch (e.getResult()) {
            case ResultEvent.FACE_SUCCESS:
                break;
            case ResultEvent.FACE_FAIL_HAS_FINGER:
                break;
            case ResultEvent.FINGER_SUCCESS:
                break;
            case ResultEvent.FAIL:
                break;
            case ResultEvent.ID_PHOTO:
                setVisibility(VISIBLE);
                byte[] bmpData = Base64.decode(record.getCardImg(), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bmpData, 0, bmpData.length);
                ivIdPhoto.setImageBitmap(bmp);
                break;
        }
    }

}
