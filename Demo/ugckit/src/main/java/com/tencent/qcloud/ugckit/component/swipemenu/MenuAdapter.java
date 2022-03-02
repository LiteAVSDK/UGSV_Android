package com.tencent.qcloud.ugckit.component.swipemenu;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.tencent.qcloud.ugckit.module.picker.data.ItemView;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.utils.DateTimeUtil;
import com.tencent.qcloud.ugckit.R;


import java.util.ArrayList;

public class MenuAdapter extends SwipeMenuAdapter<MenuAdapter.DefaultViewHolder> {

    private Context                    mContext;
    private ArrayList<TCVideoFileInfo> mTCVideoFileInfoList;
    private ItemView.OnDeleteListener  mOnDeleteListener;
    public  int                        mBitmapWidth;
    public  int                        mBitmapHeight;
    private int                        mRemoveIconId;

    public MenuAdapter(Context context, ArrayList<TCVideoFileInfo> fileInfos) {
        mContext = context;
        this.mTCVideoFileInfoList = fileInfos;
    }

    public void removeIndex(int position) {
        this.mTCVideoFileInfoList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mTCVideoFileInfoList.size());
    }

    public void addItem(TCVideoFileInfo fileInfo) {
        this.mTCVideoFileInfoList.add(fileInfo);
        notifyItemInserted(mTCVideoFileInfoList.size());
    }

    public boolean contains(TCVideoFileInfo fileInfo) {
        return this.mTCVideoFileInfoList.contains(fileInfo);
    }

    public void setOnItemDeleteListener(ItemView.OnDeleteListener onDeleteListener) {
        this.mOnDeleteListener = onDeleteListener;
    }

    @Override
    public int getItemCount() {
        return mTCVideoFileInfoList == null ? 0 : mTCVideoFileInfoList.size();
    }

    public TCVideoFileInfo getItem(int position) {
        return mTCVideoFileInfoList.get(position);
    }

    @Override
    public View onCreateContentView(@NonNull ViewGroup parent, int viewType) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.ugckit_swipe_menu_item, parent, false);
    }

    @NonNull
    @Override
    public MenuAdapter.DefaultViewHolder onCompatCreateViewHolder(@NonNull View realContentView, int viewType) {
        return new DefaultViewHolder(realContentView);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuAdapter.DefaultViewHolder holder, int position) {
        TCVideoFileInfo fileInfo = mTCVideoFileInfoList.get(position);
        holder.setOnDeleteListener(mOnDeleteListener);

        // 自定义宽高
        if (mBitmapHeight != 0 && mBitmapWidth != 0) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mBitmapWidth, mBitmapHeight);
            holder.ivThumb.setLayoutParams(params);
        }
        // 自定义删除按钮
        if (mRemoveIconId != 0) {
            holder.ivDelete.setImageResource(mRemoveIconId);
        }
        if (fileInfo.getFileType() == TCVideoFileInfo.FILE_TYPE_PICTURE) {
            holder.tvDuration.setVisibility(View.GONE);
        } else {
            holder.setDuration(DateTimeUtil.duration(fileInfo.getDuration()));
            holder.tvDuration.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(fileInfo.getFilePath())) {
            Glide.with(mContext).load(fileInfo.getFilePath()).into(holder.ivThumb);
        } else {
            Glide.with(mContext).load(fileInfo.getFileUri()).into(holder.ivThumb);
        }
    }

    public ArrayList<TCVideoFileInfo> getAll() {
        return mTCVideoFileInfoList;
    }

    static class DefaultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView                 ivThumb;
        TextView                  tvDuration;
        ImageView                 ivDelete;
        ItemView.OnDeleteListener mOnDeleteListener;

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = (ImageView) itemView.findViewById(R.id.iv_icon);

            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
            ivDelete = (ImageView) itemView.findViewById(R.id.iv_close);
            ivDelete.setOnClickListener(this);
        }

        public void setOnDeleteListener(ItemView.OnDeleteListener onDeleteListener) {
            this.mOnDeleteListener = onDeleteListener;
        }

        public void setDuration(String duration) {
            this.tvDuration.setText(duration);
        }

        @Override
        public void onClick(View v) {
            if (mOnDeleteListener != null) {
                mOnDeleteListener.onDelete(getAdapterPosition());
            }
        }
    }

    public void setBitmapWidth(int bitmapWidth) {
        mBitmapWidth = bitmapWidth;
    }

    public void setBitmapHeight(int bitmapHeight) {
        mBitmapHeight = bitmapHeight;
    }

    public void setRemoveIconId(int resId) {
        mRemoveIconId = resId;
    }
}
