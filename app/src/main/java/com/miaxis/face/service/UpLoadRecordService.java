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
import com.miaxis.face.greendao.gen.RecordDao;
import com.miaxis.face.net.UpLoadRecord;
import com.miaxis.face.util.DateUtil;
import com.miaxis.face.util.FileUtil;

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
    private static final String CONFIG = "com.miaxis.face.service.extra.CONFIG";

    public UpLoadRecordService() {
        super("UpLoadRecordService");
    }

    Retrofit retrofit;
    RecordDao recordDao;

    public static void startActionFoo(Context context, Record record, Config config) {
        Intent intent = new Intent(context, UpLoadRecordService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(RECORD, record);
        intent.putExtra(CONFIG, config);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                final Record record = (Record) intent.getSerializableExtra(RECORD);
                final Config config = (Config) intent.getSerializableExtra(CONFIG);
                handleActionUpLoad(record, config);
            }
        }
    }

    private void handleActionUpLoad(final Record record, final Config c) {
        String cardImg = FileUtil.pathToBase64(record.getCardImg());
        String faceImg = FileUtil.pathToBase64(record.getFaceImg());
        retrofit = new Retrofit.Builder()
                .baseUrl("http://" + c.getIp() + ":" + c.getPort() + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        recordDao = Face_App.getRecordDao();
        UpLoadRecord up = retrofit.create(UpLoadRecord.class);
        Call<AjaxResponse> call = up.upLoadRecord(
                record.getId()+"",
                record.getCardNo(),
                record.getName(),
                record.getSex(),
                record.getBirthday(),
                record.getAddress(),
                record.getBusEntity(),
                record.getStatus(),
                cardImg,
                faceImg,
                record.getFinger0(),
                record.getFinger1(),
                record.getPrintFinger(),
                record.getLocation(),
                record.getLongitude(),
                record.getLatitude(),
                DateUtil.toAll(record.getCreateDate()),
                record.getDevsn(),
                record.getCardId()
                );
        call.enqueue(new Callback<AjaxResponse>() {
            @Override
            public void onResponse(Call<AjaxResponse> call, Response<AjaxResponse> response) {
                AjaxResponse a = response.body();
                if (a.getCode() == AjaxResponse.SUCCESS) {
                    record.setHasUp(true);
                    recordDao.update(record);
                }
            }

            @Override
            public void onFailure(Call<AjaxResponse> call, Throwable t) {
            }
        });

    }

}
