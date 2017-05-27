package com.miaxis.face.view.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.adapter.RecordAdapter;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Record;
import com.miaxis.face.greendao.gen.RecordDao;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecordActivity extends BaseActivity {

    @BindView(R.id.btn_last)
    Button btnLast;
    @BindView(R.id.tv_page)
    TextView tvPage;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.lv_record)
    ListView lvRecord;

    private List<Record> recordList;
    private int curPage = 1;
    private RecordAdapter adapter;

    private static final int PAGE_SIZE = 20;
    private RecordDao recordDao;
    private long total;
    private long pageNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);
        initWindow();

        recordDao = Face_App.getRecordDao();
        total = recordDao.count();
        pageNum = (total % PAGE_SIZE == 0) ? total / PAGE_SIZE : (total / PAGE_SIZE + 1);
        recordList = recordDao.queryBuilder().offset(curPage * PAGE_SIZE).limit(PAGE_SIZE).orderAsc(RecordDao.Properties.Id).list();
        adapter = new RecordAdapter(recordList, this);
        lvRecord.setAdapter(adapter);
        tvPage.setText(curPage+"");
    }

    @OnClick(R.id.btn_next)
    void onNextPate() {
        if (curPage < total)
            curPage ++;
        loadRecord();
    }

    @OnClick(R.id.btn_last)
    void onLastPage() {
        if (curPage > 1)
            curPage --;
        loadRecord();
    }

    @OnClick(R.id.btn_back)
    void onBack() {
        finish();
    }

    private void loadRecord() {
        tvPage.setText(curPage + "");
        recordList = recordDao.queryBuilder().offset(curPage * PAGE_SIZE).limit(PAGE_SIZE).orderAsc(RecordDao.Properties.Id).list();
        adapter.setRecordList(recordList);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
