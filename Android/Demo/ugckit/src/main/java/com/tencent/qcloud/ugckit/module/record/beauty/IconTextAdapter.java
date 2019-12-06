package com.tencent.qcloud.ugckit.module.record.beauty;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;


import java.util.ArrayList;

public class IconTextAdapter extends BaseAdapter {
    private static final String TAG = "IconTextAdapter";
    private final Context mContext;
    private final int mTextColorPrimary;
    @NonNull
    private ArrayList<BeautyData> data = new ArrayList<BeautyData>();
    private BeautyPannel.OnItemClickListener mItemClickListener;
    // 当前选中
    private int mSelectPos;

    public IconTextAdapter(Context context) {
        mContext = context;
        mTextColorPrimary = UIAttributeUtil.getColorRes(mContext, R.attr.ugckitColorPrimary, R.color.colorRed2);
    }

    public void addAll(ArrayList<BeautyData> beautyDataList) {
        TXCLog.d(TAG, "list size:" + beautyDataList.size());
        data.clear();
        data.addAll(beautyDataList);
        notifyDataSetChanged();
    }

    public void clearAllData() {
        data.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_beauty, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final BeautyData beautyData = data.get(position);
        holder.icon.setImageResource(beautyData.icon);
        holder.title.setText(beautyData.text);
        if (mSelectPos == position) {
            holder.title.setTextColor(mTextColorPrimary);
        } else {
            holder.title.setTextColor(Color.WHITE);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(beautyData, position);
                }
                if (mSelectPos != position) {
                    mSelectPos = position;
                    notifyDataSetChanged();
                }
            }
        });
        return convertView;
    }

    public void setOnItemClickListener(BeautyPannel.OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private ImageView icon;
        private TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

}
