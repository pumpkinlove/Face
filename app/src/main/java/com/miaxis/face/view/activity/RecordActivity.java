package com.miaxis.face.view.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.adapter.RecordAdapter;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Record;
import com.miaxis.face.event.CountRecordEvent;
import com.miaxis.face.event.SearchDoneEvent;
import com.miaxis.face.greendao.gen.RecordDao;
import com.miaxis.face.service.QueryRecordService;
import com.miaxis.face.util.DateUtil;
import com.miaxis.face.view.fragment.RecordDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.query.QueryBuilder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;

public class RecordActivity extends BaseActivity {

    @BindView(R.id.btn_last)
    Button btnLast;
    @BindView(R.id.s_page)
    Spinner sPage;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.tv_total)
    TextView tvTotal;
    @BindView(R.id.tv_from_time)
    TextView tvFromTime;
    @BindView(R.id.tv_to_time)
    TextView tvToTime;
    @BindView(R.id.s_sex)
    Spinner sSex;
    @BindView(R.id.s_result)
    Spinner sResult;
    @BindView(R.id.btn_search)
    Button btnSearch;
    @BindView(R.id.lv_record)
    ListView lvRecord;
    @BindView(R.id.btn_back)
    Button btnBack;

    @BindArray(R.array.sex)
    String[] sexArray;

    @BindArray(R.array.result)
    String[] resultArray;

    private List<Record> recordList;
    private int curPage = 1;
    private RecordAdapter adapter;

    private static final int PAGE_SIZE = 10;
    private RecordDao recordDao;
    private long totalCount;
    private long totalPage;
    private RecordDialog recordDialog;
    private List<Integer> pageNumList;
    private ArrayAdapter<Integer> pageAdapter;
    private String searchSex;
    private String searchResult;
    private ProgressDialog pd;
    private QueryBuilder<Record> builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);
        initWindow();
        EventBus.getDefault().register(this);
        recordDao = Face_App.getRecordDao();
        adapter = new RecordAdapter(this);
        lvRecord.setAdapter(adapter);
        recordDialog = new RecordDialog();
        pd = new ProgressDialog(this);
        pd.setMessage("正在检索...");
        pd.setCancelable(false);
        pageNumList = new ArrayList<>();
        pageAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, pageNumList);
        pageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sPage.setAdapter(pageAdapter);
        onSearch();
    }

    @OnClick(R.id.btn_next)
    void onNextPate() {
        if (curPage < totalPage) {
            curPage++;
            sPage.setSelection(curPage-1);
        }
    }

    @OnClick(R.id.btn_last)
    void onLastPage() {
        if (curPage > 1) {
            curPage--;
            sPage.setSelection(curPage-1);
        }
    }

    @OnClick(R.id.btn_back)
    void onBack() {
        finish();
    }

    @OnClick(R.id.btn_search)
    void onSearch() {
        QueryRecordService.startActionQuery(this, curPage, tvFromTime.getText().toString(), tvToTime.getText().toString(), searchSex, searchResult);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @OnItemClick(R.id.lv_record)
    void onRecordClicked(int position) {
        if (!recordDialog.isAdded()) {
            Record re = recordList.get(position);
            recordDialog.setRecord(re);
            recordDialog.show(getFragmentManager(), "RECORD_DIALOG");
        }
    }

    @OnItemSelected(R.id.s_sex)
    void onSexSelect(int position) {
        searchSex = sexArray[position];
    }

    @OnItemSelected(R.id.s_result)
    void onResultSelect(int position) {
        searchResult = resultArray[position];
    }

    @OnItemSelected(R.id.s_page)
    void onPageSelected(int position) {
        Log.e("onPageSelected", "----------");
        curPage = pageNumList.get(position);
        QueryRecordService.startActionQuery(this, curPage, tvFromTime.getText().toString(), tvToTime.getText().toString(), searchSex, searchResult);
    }

    @OnClick(R.id.tv_from_time)
    void onSelectFromTime() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tvFromTime.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    }
                }
                ,c.get(Calendar.YEAR)
                ,c.get(Calendar.MONTH)
                ,c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @OnClick(R.id.tv_to_time)
    void onSelectToTime() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tvToTime.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    }
                }
                ,c.get(Calendar.YEAR)
                ,c.get(Calendar.MONTH)
                ,c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @OnClick(R.id.btn_clear)
    void onClear() {
        tvFromTime.setText(null);
        tvToTime.setText(null);
        sSex.setSelection(0);
        sResult.setSelection(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchDoneEvent(SearchDoneEvent e) {
        Log.e("onSearchDoneEvent", "----------");
        pd.dismiss();
        totalCount = e.getTotalCount();
        totalPage = e.getTotalPageNum();
        pageNumList.clear();

        for (int i=1; i<=totalPage; i++) {
            pageNumList.add(i);
        }
        pageAdapter.notifyDataSetChanged();

        tvTotal.setText("共" + totalPage + "页 " + totalCount + "条");
        recordList = e.getRecordList();
        adapter.setRecordList(recordList);
        adapter.notifyDataSetChanged();
    }

}
