package com.miaxis.face.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Record;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.greendao.gen.RecordDao;
import com.miaxis.face.util.FileUtil;
import com.miaxis.face.util.LogUtil;

import java.io.File;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ClearService extends IntentService {
    private static final String ACTION_CLEAR = "com.miaxis.face.service.action.CLEAR";

    public ClearService() {
        super("ClearService");
    }

    public static void startActionClear(Context context) {
        Intent intent = new Intent(context, ClearService.class);
        intent.setAction(ACTION_CLEAR);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CLEAR.equals(action)) {
                handleActionClear();
            }
        }
    }

    private void handleActionClear() {
        int type = FileUtil.getAvailablePathType(this);
        switch (type) {
            case Constants.PATH_TFCARD:
                long sdAll = FileUtil.getSDAllSize(FileUtil.getAvailablePath(this));
                long sdFree = FileUtil.getSDFreeSize(FileUtil.getAvailablePath(this));
                double c = (double) sdFree / sdAll;
                if (c <= 0.30d) {
                    RecordDao dao = Face_App.getRecordDao();
                    List<Record> recordList = dao.queryBuilder().offset(0).limit(1000).orderAsc(RecordDao.Properties.Id).list();
                    dao.deleteInTx(recordList);
                    LogUtil.writeLog("清理记录" + recordList.size() + "条");
                }
                break;
            case Constants.PATH_LOCAL:
                RecordDao dao = Face_App.getRecordDao();
                long count = dao.count();
                if (count >= 15000) {
                    List<Record> recordList = dao.queryBuilder().offset(0).limit(1000).orderAsc(RecordDao.Properties.Id).list();
                    dao.deleteInTx(recordList);
                    LogUtil.writeLog("清理记录" + recordList.size() + "条");
                }
                break;
        }

    }

}
