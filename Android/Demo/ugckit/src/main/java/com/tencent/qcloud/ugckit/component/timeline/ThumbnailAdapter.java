package com.tencent.qcloud.ugckit.component.timeline;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.tencent.qcloud.ugckit.R;

import java.util.ArrayList;
import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_FOOTER = 2;
    private static final int TYPE_THUMBNAIL = 3;

    private int mViewWidth;
    private int mCount;
    @Nullable
    private List<Bitmap> mThumbnailList;

    public ThumbnailAdapter(int viewWidth, @Nullable List<Bitmap> thumbnailList) {
        mViewWidth = viewWidth;
        if (thumbnailList == null) {
            mThumbnailList = new ArrayList<>();
        }
        mThumbnailList = thumbnailList;
        mCount = mThumbnailList.size();
    }

    @Nullable
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ThumbnailViewHolder viewHolder;
        View itemView;
        switch (viewType) {
            case TYPE_HEADER:
            case TYPE_FOOTER:
                itemView = new View(parent.getContext());
                itemView.setLayoutParams(new ViewGroup.LayoutParams(mViewWidth / 2, ViewGroup.LayoutParams.MATCH_PARENT));
                itemView.setBackgroundColor(Color.TRANSPARENT);
                viewHolder = new ThumbnailViewHolder(itemView);
                return viewHolder;

            case TYPE_THUMBNAIL:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ugckit_item_video_progress_thumbnail, null);
                viewHolder = new ThumbnailViewHolder(itemView);
                viewHolder.ivThumbnail = (ImageView) itemView.findViewById(R.id.iv_video_progress_thumbnail);
                return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        if (position != 0 && position != mCount + 1) {
            Bitmap thumbnailBitmap = mThumbnailList.get(position - 1);
            holder.ivThumbnail.setImageBitmap(thumbnailBitmap);
        }
    }

    @Override
    public int getItemCount() {
        if (mCount == 0) {
            return 0;
        }

        return mCount + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == mCount + 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_THUMBNAIL;
        }
    }

    @Override
    public void onViewRecycled(@NonNull ThumbnailViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.ivThumbnail != null) {
            holder.ivThumbnail.setImageBitmap(null);
        }
    }

    class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;

        public ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
