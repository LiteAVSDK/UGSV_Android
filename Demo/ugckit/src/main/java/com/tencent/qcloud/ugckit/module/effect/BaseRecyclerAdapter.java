package com.tencent.qcloud.ugckit.module.effect;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * 重写的父类Adapter
 * <p>
 * 1. 添加item的点击事件
 * 2. 添加item的长安点击事件
 */
public abstract class BaseRecyclerAdapter<V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> implements View.OnClickListener, View.OnLongClickListener {

    private   WeakReference<RecyclerView> mRecyclerView;
    protected OnItemClickListener         mOnItemClickListener;
    protected OnItemLongClickListener     mOnItemLongClickListener;

    @Override
    public void onBindViewHolder(@Nullable V holder, int position) {
        if (holder != null) {
            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(this);
            }
            if (mOnItemLongClickListener != null) {
                holder.itemView.setOnLongClickListener(this);
            }
            onBindVH(holder, position);
        }
    }

    abstract public void onBindVH(V holder, int position);

    @Override
    public V onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mRecyclerView == null)
            mRecyclerView = new WeakReference<RecyclerView>((RecyclerView) parent);
        return onCreateVH(parent, viewType);
    }

    public abstract V onCreateVH(ViewGroup parent, int viewType);

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        RecyclerView recyclerView = mRecyclerView.get();
        if (recyclerView != null) {
            int position = recyclerView.getChildAdapterPosition(v);
            mOnItemClickListener.onItemClick(v, position);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        RecyclerView recyclerView = mRecyclerView.get();
        if (recyclerView != null) {
            int position = recyclerView.getChildAdapterPosition(v);
            return mOnItemLongClickListener.onItemLongClick(v, position);
        }
        return true;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }
}