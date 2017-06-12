package com.miaxis.face.view.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.bean.Record;
import com.miaxis.face.view.fragment.BaseDialogFragment;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by xu.nan on 2016/10/14.
 */

public class RecordDialog extends BaseDialogFragment {

    Unbinder unbinder;
    @BindView(R.id.iv_id_photo)
    ImageView ivIdPhoto;
    @BindView(R.id.iv_result)
    ImageView ivResult;
    @BindView(R.id.iv_camera_photo)
    ImageView ivCameraPhoto;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_cardNo)
    TextView tvCardNo;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.tv_location)
    TextView tvLocation;
    @BindView(R.id.tv_sex)
    TextView tvSex;
    @BindView(R.id.tv_birthday)
    TextView tvBirthday;
    @BindView(R.id.tv_has_up)
    TextView tvHasUp;
    @BindView(R.id.tv_address)
    TextView tvAddress;

    @BindColor(R.color.green_dark)
    int darkGreen;
    @BindColor(R.color.red)
    int red;

    private Record record;

    public void setRecord(Record record) {
        this.record = record;
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
        hideNavigationBar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_record, container);
        unbinder = ButterKnife.bind(this, view);

        initView();

        return view;
    }

    private void initView() {

        if (record == null) {
            return;
        }

        tvName.setText(record.getName());
        tvCardNo.setText(record.getCardNo());
        tvSex.setText(record.getSex());
        tvResult.setText(record.getStatus());
        if ("人脸通过".equals(record.getStatus())) {
            ivResult.setImageResource(R.mipmap.result_true);
            tvResult.setTextColor(darkGreen);
        } else if ("指纹通过".equals(record.getStatus())) {
            ivResult.setImageResource(R.mipmap.result_false);
            tvResult.setTextColor(darkGreen);
        } else {
            ivResult.setImageResource(R.mipmap.result_false);
            tvResult.setTextColor(red);
        }
        if (record.isHasUp()) {
            tvHasUp.setText("已上传");
            tvHasUp.setTextColor(darkGreen);
        } else {
            tvHasUp.setText("未上传");
            tvHasUp.setTextColor(red);
        }
        if (null != record.getCardImg() && record.getCardImg().length() > 0) {
            byte[] idData = Base64.decode(record.getCardImg(), Base64.DEFAULT);
            Bitmap bmpId = BitmapFactory.decodeByteArray(idData, 0, idData.length);
            ivIdPhoto.setImageBitmap(bmpId);
        }
        if (null != record.getFaceImg() && record.getFaceImg().length() > 0) {
            byte[] cameraData = Base64.decode(record.getFaceImg(), Base64.DEFAULT);
            Bitmap bmpCamera = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.length);
            ivCameraPhoto.setImageBitmap(bmpCamera);
        }

        tvLocation.setText(record.getLocation());
        tvAddress.setText(record.getAddress());
        tvBirthday.setText(record.getBirthday());

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
