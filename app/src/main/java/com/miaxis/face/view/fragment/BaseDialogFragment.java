package com.miaxis.face.view.fragment;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.view.View;

/**
 * Created by xu.nan on 2017/6/6.
 */

public class BaseDialogFragment extends DialogFragment {

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        hideNavigationBar();
    }

    private void hideNavigationBar() {
        // TODO Auto-generated method stub
        final View decorView = getActivity().getWindow().getDecorView();
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(flags);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });
    }
}
