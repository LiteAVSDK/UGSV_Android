package com.tencent.qcloud.ugckit.module.effect.paster.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


import com.tencent.qcloud.ugckit.module.effect.paster.IPasterPannel;
import com.tencent.qcloud.ugckit.module.effect.paster.TCPasterInfo;
import com.tencent.qcloud.ugckit.R;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PasterAdapter extends RecyclerView.Adapter<PasterAdapter.PasterViewHolder> implements View.OnClickListener {

    @Nullable
    private List<TCPasterInfo>                mPasterInfoList;
    private WeakReference<RecyclerView>       mRecyclerView;
    private IPasterPannel.OnItemClickListener mOnItemClickListener;

    public PasterAdapter(@Nullable List<TCPasterInfo> pasterInfoList) {
        if (pasterInfoList == null) {
            mPasterInfoList = new ArrayList<TCPasterInfo>();
        } else {
            mPasterInfoList = pasterInfoList;
        }
    }

    @NonNull
    @Override
    public PasterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mRecyclerView == null) {
            mRecyclerView = new WeakReference<RecyclerView>((RecyclerView) parent);
        }
        return new PasterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ugckit_layout_paster_view, null));
    }

    @Override
    public void onBindViewHolder(@NonNull PasterViewHolder holder, int position) {
        holder.itemView.setOnClickListener(this);
        Glide.with(holder.itemView.getContext()).load(mPasterInfoList.get(position).getIconPath()).into(holder.ivPaster);
    }

    @Override
    public int getItemCount() {
        return mPasterInfoList.size();
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener == null) {
            return;
        }
        RecyclerView recyclerView = mRecyclerView.get();
        if (recyclerView != null) {
            int position = recyclerView.getChildAdapterPosition(view);
            mOnItemClickListener.onItemClick(mPasterInfoList.get(position), position);
        }
    }

    class PasterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPaster;

        public PasterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPaster = (ImageView) itemView.findViewById(R.id.iv_paster);
        }
    }

    public void setOnItemClickListener(IPasterPannel.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

}
