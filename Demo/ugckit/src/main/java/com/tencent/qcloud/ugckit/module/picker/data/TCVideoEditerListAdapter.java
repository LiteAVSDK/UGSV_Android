package com.tencent.qcloud.ugckit.module.picker.data;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.tencent.qcloud.ugckit.utils.DateTimeUtil;
import com.tencent.qcloud.ugckit.R;


import java.util.ArrayList;

public class TCVideoEditerListAdapter extends RecyclerView.Adapter<TCVideoEditerListAdapter.ViewHolder> {

    private Context mContext;
    private int     mLastSelected = -1;
    private boolean mMultiplePick;
    private boolean mIsOrdered;

    @NonNull
    private ArrayList<TCVideoFileInfo> mData = new ArrayList<TCVideoFileInfo>();
    private ItemView.OnAddListener     mOnAddListener;
    private ArrayList<TCVideoFileInfo> mOrderFileList;

    public TCVideoEditerListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.ugckit_item_ugc_video, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final TCVideoFileInfo fileInfo = mData.get(position);
        if (fileInfo.getFileType() == TCVideoFileInfo.FILE_TYPE_VIDEO) {
            holder.duration.setText(DateTimeUtil.formattedTime(fileInfo.getDuration() / 1000));
        }
        if (!TextUtils.isEmpty(fileInfo.getFilePath())) {
            Glide.with(mContext).load(fileInfo.getFilePath()).into(holder.thumb);
        } else {
            Glide.with(mContext).load(fileInfo.getFileUri()).into(holder.thumb);
        }
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
        return mData.size();
    }

    /**
     * 设置选择模式
     *
     * @param multiplePick 是否多选
     * @param order        是否排序
     */
    public void setMultiplePick(boolean multiplePick, boolean order) {
        mMultiplePick = multiplePick;
        mIsOrdered = order;
    }

    public void setOnItemAddListener(ItemView.OnAddListener onAddListener) {
        this.mOnAddListener = onAddListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ImageView thumb;
        @NonNull
        private final TextView  duration;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            thumb = (ImageView) itemView.findViewById(R.id.iv_thumb);
            duration = (TextView) itemView.findViewById(R.id.tv_duration);
        }
    }

    @NonNull
    public ArrayList<TCVideoFileInfo> getMultiSelected() {
        ArrayList<TCVideoFileInfo> infos = new ArrayList<TCVideoFileInfo>();

        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).isSelected()) {
                infos.add(mData.get(i));
            }
        }
        return infos;
    }

    @Nullable
    public TCVideoFileInfo getSingleSelected() {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).isSelected()) {
                return mData.get(i);
            }
        }
        return null;
    }

    public void addAll(@NonNull ArrayList<TCVideoFileInfo> files) {
        try {
            this.mData.clear();
            this.mData.addAll(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void changeSingleSelection(int position) {
        if (mLastSelected != -1) {
            mData.get(mLastSelected).setSelected(false);
        }
        notifyItemChanged(mLastSelected);

        TCVideoFileInfo info = mData.get(position);
        info.setSelected(true);
        notifyItemChanged(position);

        mLastSelected = position;
    }

    public void changeMultiSelection(int position) {
        if (mIsOrdered) {
            if (mOrderFileList == null) {
                mOrderFileList = new ArrayList<>();
            }
            TCVideoFileInfo fileInfo = mData.get(position);

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
            if (mData.get(position).isSelected()) {
                mData.get(position).setSelected(false);
            } else {
                mData.get(position).setSelected(true);
            }
        }
        notifyItemChanged(position);
    }

    public ArrayList<TCVideoFileInfo> getInOrderMultiSelected() {
        return mOrderFileList;
    }
}
