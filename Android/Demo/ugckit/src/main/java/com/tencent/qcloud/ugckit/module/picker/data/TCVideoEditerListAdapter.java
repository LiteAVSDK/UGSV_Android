package com.tencent.qcloud.ugckit.module.picker.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.tencent.qcloud.ugckit.utils.DateTimeUtil;
import com.tencent.qcloud.ugckit.R;


import java.io.File;
import java.util.ArrayList;

public class TCVideoEditerListAdapter extends RecyclerView.Adapter<TCVideoEditerListAdapter.ViewHolder> {

    private Context mContext;
    @NonNull
    private ArrayList<TCVideoFileInfo> data = new ArrayList<TCVideoFileInfo>();
    private int mLastSelected = -1;
    private boolean mMultiplePick;
    private boolean isOrdered;
    private ItemView.OnAddListener mOnAddListener;
    private ArrayList<TCVideoFileInfo> mOrderFileList;

    public TCVideoEditerListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_ugc_video, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final TCVideoFileInfo fileInfo = data.get(position);
        if (fileInfo.getFileType() == TCVideoFileInfo.FILE_TYPE_VIDEO) {
            holder.duration.setText(DateTimeUtil.formattedTime(fileInfo.getDuration() / 1000));
        }
        Glide.with(mContext).load(fileInfo.getFileUri()).into(holder.thumb);
        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnAddListener != null) {
                    mOnAddListener.onAdd(fileInfo);
                }
                if (mMultiplePick) {
                    changeMultiSelection(position);
                } else {
                    changeSingleSelection(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * 设置选择模式
     *
     * @param multiplePick 是否多选
     * @param order        是否排序
     */
    public void setMultiplePick(boolean multiplePick, boolean order) {
        mMultiplePick = multiplePick;
        isOrdered = order;
    }

    public void setOnItemAddListener(ItemView.OnAddListener onAddListener) {
        this.mOnAddListener = onAddListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ImageView thumb;
        @NonNull
        private final TextView duration;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            thumb = (ImageView) itemView.findViewById(R.id.iv_thumb);
            duration = (TextView) itemView.findViewById(R.id.tv_duration);
        }
    }

    @NonNull
    public ArrayList<TCVideoFileInfo> getMultiSelected() {
        ArrayList<TCVideoFileInfo> infos = new ArrayList<TCVideoFileInfo>();

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSelected()) {
                infos.add(data.get(i));
            }
        }
        return infos;
    }

    @Nullable
    public TCVideoFileInfo getSingleSelected() {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSelected()) {
                return data.get(i);
            }
        }
        return null;
    }

    public void addAll(@NonNull ArrayList<TCVideoFileInfo> files) {
        try {
            this.data.clear();
            this.data.addAll(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void changeSingleSelection(int position) {
        if (mLastSelected != -1) {
            data.get(mLastSelected).setSelected(false);
        }
        notifyItemChanged(mLastSelected);

        TCVideoFileInfo info = data.get(position);
        info.setSelected(true);
        notifyItemChanged(position);

        mLastSelected = position;
    }

    public void changeMultiSelection(int position) {
        if (isOrdered) {
            if (mOrderFileList == null) {
                mOrderFileList = new ArrayList<>();
            }
            TCVideoFileInfo fileInfo = data.get(position);

            if (fileInfo.isSelected()) {
                fileInfo.setSelected(false);
                for (int i = 0; i < mOrderFileList.size(); i++) {
                    TCVideoFileInfo tcVideoFileInfo = mOrderFileList.get(i);
                    if (tcVideoFileInfo.getFilePath().equals(fileInfo.getFilePath())) {
                        mOrderFileList.remove(i);
                        break;
                    }
                }
            } else {
                fileInfo.setSelected(true);
                mOrderFileList.add(fileInfo);
            }
        } else {
            if (data.get(position).isSelected()) {
                data.get(position).setSelected(false);
            } else {
                data.get(position).setSelected(true);
            }
        }
        notifyItemChanged(position);
    }

    public ArrayList<TCVideoFileInfo> getInOrderMultiSelected() {
        return mOrderFileList;
    }
}
