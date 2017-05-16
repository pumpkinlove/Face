package com.miaxis.face.view.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.view.custom.GifView;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoadingActivity extends BaseActivity {

    @BindView(R.id.tv_loading)
    TextView tvLoading;

    @BindView(R.id.gif_loading)
    GifView gifLoading;

    @BindColor(R.color.white)
    int white;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ButterKnife.bind(this);

        gifLoading.setMovieResource(R.raw.loading3);

    }
}
