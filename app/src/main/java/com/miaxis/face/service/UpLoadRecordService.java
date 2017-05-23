package com.miaxis.face.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.AjaxResponse;
import com.miaxis.face.bean.Config;
import com.miaxis.face.bean.Record;
import com.miaxis.face.net.UpLoadRecord;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpLoadRecordService extends IntentService {

    private static final String ACTION_UPLOAD = "com.miaxis.face.service.action.UPLOAD";

    private static final String RECORD = "com.miaxis.face.service.extra.RECORD";

    public UpLoadRecordService() {
        super("UpLoadRecordService");
    }

    Retrofit retrofit;


    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        Config c = Face_App.getConfig();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://" + c.getIp() + ":" + c.getPort() + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        super.onStart(intent, startId);
    }

    public static void startActionFoo(Context context, Record record) {
        Intent intent = new Intent(context, UpLoadRecordService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(RECORD, record);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                final Record record = (Record) intent.getSerializableExtra(RECORD);
                handleActionFoo(record);
            }
        }
    }

    private void handleActionFoo(Record record) {
        UpLoadRecord up = retrofit.create(UpLoadRecord.class);
        Call<AjaxResponse> call = up.upLoadRecord(record);
        call.enqueue(new Callback<AjaxResponse>() {
            @Override
            public void onResponse(Call<AjaxResponse> call, Response<AjaxResponse> response) {
                AjaxResponse a = response.body();
                Log.e("onResponse", a.getMessage());
            }

            @Override
            public void onFailure(Call<AjaxResponse> call, Throwable t) {
                Log.e("onFailure", "onFailure");
            }
        });

    }

}
