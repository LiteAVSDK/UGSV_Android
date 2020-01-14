package com.tencent.qcloud.xiaoshipin.videochoose;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.tencent.qcloud.ugckit.basic.ITitleBarLayout;
import com.tencent.qcloud.ugckit.module.picker.data.ItemView;
import com.tencent.qcloud.ugckit.module.picker.data.PickerManagerKit;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.module.picker.view.AbsPickerUI;
import com.tencent.qcloud.ugckit.module.picker.view.PickedLayout;

import java.util.ArrayList;

public class TripleRecordVideoPicker extends AbsPickerUI implements ActivityCompat.OnRequestPermissionsResultCallback {
    private Activity mActivity;
    @NonNull
    private Handler mHandlder = new Handler();

    private OnPickerListener  mListener;

    public TripleRecordVideoPicker(Context context) {
        super(context);
        initDefault();
    }

    public TripleRecordVideoPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public TripleRecordVideoPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
    }

    @Override
    public void initDefault() {
        // 设置标题
        getTitleBar().setTitle(getResources().getString(com.tencent.qcloud.ugckit.R.string.video_choose), ITitleBarLayout.POSITION.MIDDLE);
        getTitleBar().setVisible(false, ITitleBarLayout.POSITION.RIGHT);
        getPickedLayout().setVisibility(GONE);
        getPickerListLayout().setOnItemAddListener(new ItemView.OnAddListener() {
            @Override
            public void onAdd(TCVideoFileInfo fileInfo) {
                // 选中一个视频
                if(mListener!=null){
                    ArrayList<TCVideoFileInfo> list = new ArrayList<>();
                    list.add(fileInfo);
                    mListener.onPickedList(list);
                }
            }
        });
        // 加载视频
        loadVideoList();
    }

    private void loadVideoList() {
        mActivity = (Activity) getContext();
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mHandlder.post(new Runnable() {
                @Override
                public void run() {
                    ArrayList<TCVideoFileInfo> list = PickerManagerKit.getInstance(mActivity).getAllVideo();
                    getPickerListLayout().updateItems(list);
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    /**
     * Glide停止加载图片
     */
    @Override
    public void pauseRequestBitmap() {
        getPickerListLayout().pauseRequestBitmap();
    }

    /**
     * Glide继续加载图片
     */
    @Override
    public void resumeRequestBitmap() {
        getPickerListLayout().resumeRequestBitmap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadVideoList();
        }
    }

    @Override
    public void setOnPickerListener(@Nullable final OnPickerListener listener) {
        mListener = listener;
    }
}
