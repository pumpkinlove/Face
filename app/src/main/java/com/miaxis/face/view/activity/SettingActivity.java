package com.miaxis.face.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Record;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.greendao.gen.RecordDao;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.et_ip)
    EditText etIp;
    @BindView(R.id.et_port)
    EditText etPort;
    @BindView(R.id.et_org)
    EditText etOrg;
    @BindView(R.id.et_pass_score)
    EditText etPassScore;
    @BindView(R.id.rb_finger_on)
    RadioButton rbFingerOn;
    @BindView(R.id.rb_finger_off)
    RadioButton rbFingerOff;
    @BindView(R.id.rg_finger)
    RadioGroup rgFinger;
    @BindView(R.id.rb_net_on)
    RadioButton rbNetOn;
    @BindView(R.id.rb_net_off)
    RadioButton rbNetOff;
    @BindView(R.id.rg_net)
    RadioGroup rgNet;
    @BindView(R.id.rb_query_on)
    RadioButton rbQueryOn;
    @BindView(R.id.rb_query_off)
    RadioButton rbQueryOff;
    @BindView(R.id.rg_query)
    RadioGroup rgQuery;
    @BindView(R.id.tv_select_time)
    TextView tvSelectTime;
    @BindView(R.id.tv_result_count)
    TextView tvResultCount;
    @BindView(R.id.et_monitor_interval)
    EditText etMonitorInterval;
    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.btn_save_config)
    Button btnSaveConfig;
    @BindView(R.id.btn_cancel_config)
    Button btnCancelConfig;
    @BindView(R.id.btn_clear_now)
    Button btnClearNow;
    @BindView(R.id.btn_update)
    Button btnUpdate;
    @BindView(R.id.btn_exit)
    Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btn_clear_now)
    void upLoad() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Face_App.timerTask.run();
            }
        }).start();
    }

    @OnClick(R.id.btn_update)
    void update() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RecordDao recordDao = Face_App.getRecordDao();
                for (int i=0; i<500; i++) {
                    Record record = new Record();
                    record.setName(i + "");
                    record.setCardNo(000000000000000000 + i + "");
                    recordDao.insert(record);
                    Log.e("=====", "" + i);
                }
            }
        }).start();
    }

    @OnClick(R.id.btn_exit)
    void singOut() {
        setResult(Constants.RESULT_CODE_FINISH);
        finish();
        System.exit(0);
    }
}
