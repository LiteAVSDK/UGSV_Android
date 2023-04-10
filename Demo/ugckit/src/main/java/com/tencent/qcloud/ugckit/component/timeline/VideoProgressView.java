package com.tencent.qcloud.ugckit.component.timeline;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


import com.tencent.qcloud.ugckit.R;

import java.util.List;

public class VideoProgressView extends FrameLayout {

    private Context          mContext;
    private View             mRootView;
    private RecyclerView     mRecyclerView;
    private int              mViewWidth;
    private ThumbnailAdapter mThumbnailAdapter;

    public VideoProgressView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoProgressView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.ugckit_layout_video_progress, this);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_video_thumbnail);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    }

    public void setViewWidth(int viewWidth) {
        mViewWidth = viewWidth;
    }

    public void setThumbnailData(List<Bitmap> thumbnailList) {
        mThumbnailAdapter = new ThumbnailAdapter(mViewWidth, thumbnailList);
        mRecyclerView.setAdapter(mThumbnailAdapter);
        mThumbnailAdapter.notifyDataSetChanged();
    }

    public void addThumbnailDate(Bitmap thumbnail) {
        mThumbnailAdapter.addThumbnail(thumbnail);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public int getThumbnailCount() {
        return mThumbnailAdapter.getItemCount() - 2;
    }

    public float getSingleThumbnailWidth() {
        return mContext.getResources().getDimension(R.dimen.ugckit_video_thumbnail_width);
    }

    @NonNull
    public ViewGroup getParentView() {
        return (ViewGroup) mRootView;
    }

}
