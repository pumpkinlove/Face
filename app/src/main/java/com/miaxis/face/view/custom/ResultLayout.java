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
import com.miaxis.face.event.NoCardEvent;
import com.miaxis.face.event.ResultEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.zz.faceapi.MXFaceInfo;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.miaxis.face.constant.Constants.PRE_WIDTH;

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

    @BindString(R.string.result_success)
    String result_success;
    @BindString(R.string.result_failure)
    String result_failure;
    @BindString(R.string.result_press)
    String result_press;

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
                ivResult.setImageResource(R.mipmap.result_true);
                tvResult.setText(result_success);
                tvResult.setVisibility(VISIBLE);
                gifFinger.setVisibility(GONE);
                break;
            case ResultEvent.FACE_FAIL_HAS_FINGER:
                ivResult.setImageResource(R.mipmap.result_false);
                tvResult.setText(result_press);
                tvResult.setVisibility(VISIBLE);
                gifFinger.setVisibility(VISIBLE);
                break;
            case ResultEvent.FINGER_SUCCESS:
                ivResult.setImageResource(R.mipmap.result_true);
                tvResult.setText(result_success);
                tvResult.setVisibility(VISIBLE);
                gifFinger.setVisibility(GONE);
                break;
            case ResultEvent.FAIL:
                ivResult.setImageResource(R.mipmap.result_false);
                tvResult.setText(result_failure);
                tvResult.setVisibility(VISIBLE);
                gifFinger.setVisibility(GONE);
                break;
            case ResultEvent.ID_PHOTO:
                setVisibility(VISIBLE);
                ivResult.setImageBitmap(null);
                gifFinger.setVisibility(GONE);
                tvResult.setVisibility(GONE);
                byte[] bmpData = Base64.decode(record.getCardImg(), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bmpData, 0, bmpData.length);
                ivIdPhoto.setImageBitmap(bmp);
                ivCameraPhoto.setImageBitmap(null);
                break;
        }
        if (e.getFaceInfo() != null) {
            getFaceRect(record.getFaceImg(), e.getFaceInfo());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoCardEvent(NoCardEvent e) {
        setVisibility(INVISIBLE);
    }


    private void getFaceRect(String faceImg64, MXFaceInfo passFace) {
        byte[] bytes = Base64.decode(faceImg64, Base64.DEFAULT);
        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap rectBitmap = Bitmap.createBitmap(b, PRE_WIDTH - passFace.x -  passFace.width, passFace.y, passFace.width, passFace.height);//截取
        ivCameraPhoto.setImageBitmap(rectBitmap);
    }
}
