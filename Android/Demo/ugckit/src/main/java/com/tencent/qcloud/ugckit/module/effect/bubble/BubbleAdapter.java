package com.tencent.qcloud.ugckit.module.effect.bubble;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tencent.qcloud.ugckit.R;


import java.lang.ref.WeakReference;
import java.util.List;

public class BubbleAdapter extends RecyclerView.Adapter<BubbleAdapter.BubbleViewHolder> implements View.OnClickListener {

    private List<TCBubbleInfo>          mBubbles;
    private WeakReference<RecyclerView> mRecyclerView;

    public BubbleAdapter(List<TCBubbleInfo> bubbles) {
        mBubbles = bubbles;
    }

    @NonNull
    @Override
    public BubbleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mRecyclerView == null)
            mRecyclerView = new WeakReference<RecyclerView>((RecyclerView) parent);
        return new BubbleViewHolder(View.inflate(parent.getContext(), R.layout.ugckit_item_bubble_img, null));
    }

    @Override
    public void onBindViewHolder(@NonNull BubbleViewHolder holder, int position) {
        holder.itemView.setOnClickListener(this);
        //glide 加在asset目录资源图片需要拼接
        String assetsIconPath = "file:///android_asset/" + mBubbles.get(position).getIconPath();
        Glide.with(holder.itemView.getContext()).load(assetsIconPath).into(holder.ivBubble);
    }

    @Override
    public int getItemCount() {
        return mBubbles.size();
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            RecyclerView recyclerView = mRecyclerView.get();
            if (recyclerView != null) {
                int position = recyclerView.getChildAdapterPosition(v);
                mListener.onItemClick(v, position);
            }
        }
    }

    public static class BubbleViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBubble;

        public BubbleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBubble = (ImageView) itemView.findViewById(R.id.bubble_iv_img);
        }
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
