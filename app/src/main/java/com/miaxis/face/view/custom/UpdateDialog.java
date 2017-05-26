package com.miaxis.face.view.custom;

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
public class UpdateDialog extends DialogFragment {

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                handleActionFoo();
            }
        }).start();
    }

    private void handleActionFoo() {
        Config config = Face_App.getConfig();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + config.getIp() + ":" + config.getPort() + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UpdateVersion uv = retrofit.create(UpdateVersion.class);
        String url = "http://"+ config.getIp() + ":" + config.getPort() + "/" + Constants.PROJECT_NAME + "/" + Constants.DOWN_VERSION;
        Call<ResponseBody> call = uv.downVersion(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                File futureStudioIconFile = new File(FileUtil.FACE_MAIN_PATH, getResources().getString(R.string.app_name) + ".apk");
                try {
                    InputStream is = response.body().byteStream();
                    long totalLength = is.available();
                    FileOutputStream fos = new FileOutputStream(futureStudioIconFile);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        Log.e("============", len + "/" + totalLength);
                    }
                    fos.flush();
                    fos.close();
                    bis.close();
                    is.close();
                } catch (IOException e) {
                } finally {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void writeResponseBodyToDisk(ResponseBody body) {
        File futureStudioIconFile = new File(FileUtil.FACE_MAIN_PATH, getResources().getString(R.string.app_name) + ".apk");
        try {
            InputStream is = body.byteStream();
            long totalLength = is.available();
            FileOutputStream fos = new FileOutputStream(futureStudioIconFile);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                Log.e("============", len + "/" + totalLength);
            }
            fos.flush();
            fos.close();
            bis.close();
            is.close();
        } catch (IOException e) {
        } finally {
        }
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
