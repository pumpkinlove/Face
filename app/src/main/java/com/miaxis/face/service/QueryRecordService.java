package com.miaxis.face.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Record;
import com.miaxis.face.event.SearchDoneEvent;
import com.miaxis.face.greendao.gen.RecordDao;
import com.miaxis.face.util.DateUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class QueryRecordService extends IntentService {
    private static final String ACTION_QUERY = "com.miaxis.face.service.action.QUERY";

    // TODO: Rename parameters
    private static final String CUR_PAGE = "com.miaxis.face.service.extra.CUR_PAGE";
    private static final String FROM_DATE = "com.miaxis.face.service.extra.FROM_DATE";
    private static final String TO_DATE = "com.miaxis.face.service.extra.TO_DATE";
    private static final String SEX = "com.miaxis.face.service.extra.SEX";
    private static final String RESULT = "com.miaxis.face.service.extra.RESULT";
    private static final int PAGE_SIZE = 10;
    public QueryRecordService() {
        super("QueryRecordService");
    }

    public static void startActionQuery(Context context, int curPage, String fromDate, String toDate, String sex, String result) {
        Intent intent = new Intent(context, QueryRecordService.class);
        intent.setAction(ACTION_QUERY);
        intent.putExtra(CUR_PAGE, curPage);
        intent.putExtra(FROM_DATE, fromDate);
        intent.putExtra(TO_DATE, toDate);
        intent.putExtra(SEX, sex);
        intent.putExtra(RESULT, result);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_QUERY.equals(action)) {
                final int curPage = intent.getIntExtra(CUR_PAGE, 1);
                final String fromDate = intent.getStringExtra(FROM_DATE);
                final String toDate = intent.getStringExtra(TO_DATE);
                final String sex = intent.getStringExtra(SEX);
                final String result = intent.getStringExtra(RESULT);
                handleActionQuery(curPage, fromDate, toDate, sex, result);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionQuery(int curPage, String fromDate, String toDate, String sex, String result) {
        QueryBuilder<Record> builder = fetchBuilder(fromDate, toDate, sex, result);
        long totalCount = builder.count();
        long totalPage = (totalCount % PAGE_SIZE == 0) ? totalCount / PAGE_SIZE : (totalCount / PAGE_SIZE + 1);
        List<Record> recordList = builder.offset((curPage - 1) * PAGE_SIZE).limit(PAGE_SIZE).orderDesc(RecordDao.Properties.Id).list();
        EventBus.getDefault().post(new SearchDoneEvent(totalCount, totalPage, recordList));

    }

    private QueryBuilder<Record> fetchBuilder(String fromDate, String toDate, String sex, String result) {
        RecordDao recordDao = Face_App.getRecordDao();
        QueryBuilder<Record> builder = recordDao.queryBuilder();
        if ("男".equals(sex) || "女".equals(sex)) {
            builder.where(RecordDao.Properties.Sex.eq(sex));
        }
        if ("成功".equals(result)) {
            builder.whereOr(RecordDao.Properties.Status.eq("人脸通过"), RecordDao.Properties.Sex.eq("指纹通过"));
        } else if ("失败".equals(result)) {
            builder.where(RecordDao.Properties.Status.eq("失败"));
        }
        if (null != fromDate && fromDate.length() > 0) {
            try {
                builder.where(RecordDao.Properties.CreateDate.ge(DateUtil.fromMonthDay(fromDate)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (null != toDate && toDate.length() > 0) {
            try {
                Date searchToDate = DateUtil.fromMonthDay(toDate);
                builder.where(RecordDao.Properties.CreateDate.le(DateUtil.addOneDay(searchToDate)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return builder;
    }

}
