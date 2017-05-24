package com.miaxis.face.view.activity;

import android.app.TimePickerDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.miaxis.face.R;
import com.miaxis.face.app.Face_App;
import com.miaxis.face.bean.Config;
import com.miaxis.face.bean.Record;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.event.TimerResetEvent;
import com.miaxis.face.greendao.gen.ConfigDao;
import com.miaxis.face.greendao.gen.RecordDao;

import org.greenrobot.eventbus.EventBus;

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
    @BindView(R.id.et_banner)
    EditText etBanner;
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

    private Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initWindow();
        config = Face_App.getConfig();
        initView();
    }

    void initView() {
        etIp.setText(config.getIp());
        etPort.setText(config.getPort()+"");
        etBanner.setText(config.getBanner());
        etPassScore.setText(config.getPassScore()+"");
        etOrg.setText(config.getOrgName());
        tvSelectTime.setText(config.getUpTime());
        etMonitorInterval.setText(config.getIntervalTime()+"");
        tvVersion.setText(getVersion());
        if (config.getFingerFlag()) {
            rbFingerOn.setChecked(true);
        } else {
            rbFingerOff.setChecked(true);
        }
        if (config.getQueryFlag()) {
            rbQueryOn.setChecked(true);
        } else {
            rbQueryOff.setChecked(true);
        }
        if (config.getNetFlag()) {
            rbNetOn.setChecked(true);
        } else {
            rbNetOff.setChecked(true);
        }
    }

    @OnClick(R.id.tv_select_time)
    void onSelectTime(View view) {
        String[] strs = tvSelectTime.getText().toString().split(" : ");
        int h = Integer.valueOf(strs[0]);
        int m = Integer.valueOf(strs[1]);
        TimePickerDialog d = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String h = hourOfDay + "";
                String m = minute + "";
                if (hourOfDay < 10) {
                    h = "0" + h;
                }
                if (minute < 10) {
                    m = "0" + m;
                }
                tvSelectTime.setText(h + " : " + m);
            }
        }, h, m, true);
        d.show();
    }

    @OnClick(R.id.btn_save_config)
    void save() {
        config.setIp(etIp.getText().toString());
        config.setPort(Integer.valueOf(etPort.getText().toString()));
        config.setOrgName(etOrg.getText().toString());
        config.setPassScore(Float.valueOf(etPassScore.getText().toString()));
        if (rbFingerOn.isChecked()) {
            config.setFingerFlag(true);
        } else if (rbFingerOff.isChecked()) {
            config.setFingerFlag(false);
        }
        if (rbNetOn.isChecked()) {
            config.setNetFlag(true);
        } else if (rbNetOff.isChecked()) {
            config.setNetFlag(false);
        }
        if (rbQueryOn.isChecked()) {
            config.setQueryFlag(true);
        } else if (rbQueryOff.isChecked()) {
            config.setQueryFlag(false);
        }
        config.setUpTime(tvSelectTime.getText().toString());
        config.setIntervalTime(Integer.valueOf(etMonitorInterval.getText().toString()));
        config.setBanner(etBanner.getText().toString());
        ConfigDao configDao = Face_App.getConfigDao();
        configDao.update(config);
        EventBus.getDefault().post(new TimerResetEvent());
        finish();
    }

    @OnClick(R.id.btn_cancel_config)
    void cancel() {
        finish();
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
        throw new RuntimeException();
    }

    String getVersion() {
        String versionName = "";
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }
}
