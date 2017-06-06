package com.miaxis.face.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.bean.Record;

import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xu.nan on 2017/5/26.
 */

public class RecordAdapter extends BaseAdapter {

    private List<Record> recordList;
    private Context context;

    public RecordAdapter(List<Record> recordList, Context context) {
        this.recordList = recordList;
        this.context = context;
    }

    public void setRecordList(List<Record> recordList) {
        this.recordList = recordList;
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Record getItem(int i) {
        return recordList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_record, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        Record record = recordList.get(i);
        holder.tvName.setText(record.getName());
        holder.tvCardNo.setText(record.getCardNo());
        holder.tvOrg.setText(record.getBusEntity());
        holder.tvResult.setText(record.getStatus());
        holder.tvOpdate.setText(record.getCreateDate());

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_cardNo)
        TextView tvCardNo;
        @BindView(R.id.tv_result)
        TextView tvResult;
        @BindView(R.id.tv_org)
        TextView tvOrg;
        @BindView(R.id.tv_opdate)
        TextView tvOpdate;
        @BindView(R.id.ll_item)
        LinearLayout llItem;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
