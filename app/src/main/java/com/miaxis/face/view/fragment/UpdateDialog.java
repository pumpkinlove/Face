package com.miaxis.face.view.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Config;
import com.miaxis.face.bean.Version;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.net.UpdateVersion;
import com.miaxis.face.service.DownVersionService;
import com.miaxis.face.util.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xu.nan on 2016/9/12.
 */
public class UpdateDialog extends BaseDialogFragment {

    @BindView(R.id.ud_content)
    TextView udContent;
    @BindView(R.id.pb_update)
    ContentLoadingProgressBar pbUpdate;
    @BindView(R.id.ud_cancel)
    TextView udCancel;
    @BindView(R.id.ud_confirm)
    TextView udConfirm;
    @BindView(R.id.ll_ud_bottom)
    LinearLayout llUdBottom;
    Unbinder unbinder;
    private Version curVersion;
    private Version lastVersion;

    private Context context;

    private Config config;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.77), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        hideNavigationBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_update, container);
        unbinder = ButterKnife.bind(this, view);
        initDialog();
        EventBus.getDefault().register(this);
        config = Face_App.getConfig();
        return view;
    }

    private void initDialog() {
        udContent.setText("发现新版本 " + lastVersion.getVersion() + "， 是否立刻升级？");
        pbUpdate.setVisibility(View.GONE);
    }

    public void setCurVersion(Version curVersion) {
        this.curVersion = curVersion;
    }

    public void setLastVersion(Version lastVersion) {
        this.lastVersion = lastVersion;
    }

    @OnClick(R.id.ud_cancel)
    void cancelClick(View view) {
        dismiss();
    }

    @OnClick(R.id.ud_confirm)
    void downLoadNewVersion(View view) {
        pbUpdate.setVisibility(View.VISIBLE);
        udContent.setText("正在下载...");
        llUdBottom.setVisibility(View.GONE);
        setCancelable(false);
//        DownVersionService.startActionFoo(getActivity());
        final String filepath = Environment.getExternalStorageDirectory().getPath()+"/ " + getResources().getString(R.string.app_name) + "_" + lastVersion.getVersion()+".apk";
        final String url = "http://"+ config.getIp() + ":" + config.getPort() + "/" + Constants.PROJECT_NAME + "/" + Constants.DOWN_VERSION;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File f = HttpConnUtils.downFile(filepath, pbUpdate, url);
                EventBus.getDefault().post(f);
            }
        }).start();
    }

    protected void installApk(File file) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);//
        Process.killProcess(Process.myPid());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownLoadFinishEvent(File f) {
        llUdBottom.setVisibility(View.INVISIBLE);
        udContent.setText("下载完成");
        setCancelable(true);
        installApk(f);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
