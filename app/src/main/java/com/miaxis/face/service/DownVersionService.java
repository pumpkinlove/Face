package com.miaxis.face.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.miaxis.face.R;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Config;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.net.UpdateVersion;
import com.miaxis.face.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class DownVersionService extends IntentService {
    private static final String ACTION_DOWNLOAD = "com.miaxis.face.service.action.ACTION_DOWNLOAD";

    public DownVersionService() {
        super("DownVersionService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context) {
        Intent intent = new Intent(context, DownVersionService.class);
        intent.setAction(ACTION_DOWNLOAD);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                handleActionFoo();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo() {
        Config config = Face_App.getConfig();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://" + config.getIp() + ":" + config.getPort() + "/")
                .baseUrl("http://192.168.6.87:8088/")
                .addConverterFactory(GsonConverterFactory.create())
                .callbackExecutor(executorService)
                .build();
        UpdateVersion uv = retrofit.create(UpdateVersion.class);
//        String url = "http://192.168.6.67:8080/"+ "CIIPS_A/version/downLoadLastVersion.action";
        String url = "http://192.168.6.87:8088/"+ Constants.PROJECT_NAME + "/" + Constants.DOWN_VERSION;
//        String url = "http://" + config.getIp() + ":" + config.getPort() + "/" + Constants.PROJECT_NAME + "/" + Constants.DOWN_VERSION;
        Call<ResponseBody> call = uv.downVersion(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("onResponse", "");
                writeResponseBodyToDisk(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", t.getMessage());
            }
        });
    }

    private void writeResponseBodyToDisk(ResponseBody body) {
        File apk = new File(FileUtil.FACE_MAIN_PATH, getResources().getString(R.string.app_name) + ".apk");
        try {
            InputStream is = body.byteStream();
            long totalLength = body.contentLength();
            Log.e("available", is.available() + "");
            FileOutputStream fos = new FileOutputStream(apk);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int cur = 0;
            int len;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                cur += len;
            }
            fos.flush();
            fos.close();
            bis.close();
            is.close();
            EventBus.getDefault().post(apk);
        } catch (IOException e) {
        } finally {
        }
    }
}
