package com.tencent.qcloud.ugckit;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.AttributeSet;

import com.tencent.qcloud.ugckit.basic.ITitleBarLayout;
import com.tencent.qcloud.ugckit.module.picker.data.ItemView;
import com.tencent.qcloud.ugckit.module.picker.data.PickerManagerKit;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.module.picker.view.AbsPickerUI;
import com.tencent.qcloud.ugckit.module.picker.view.PickedLayout;

import java.util.ArrayList;

/**
 * Module：视频选择
 */
public class UGCKitVideoPicker extends AbsPickerUI {
    private Activity mActivity;
    @NonNull
    private Handler mHandlder = new Handler();

    public UGCKitVideoPicker(Context context) {
        super(context);
        initDefault();
    }

    public UGCKitVideoPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public UGCKitVideoPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
    }

    @Override
    public void initDefault() {
        // 设置标题
        getTitleBar().setTitle(getResources().getString(R.string.ugckit_video_choose), ITitleBarLayout.POSITION.MIDDLE);
        getTitleBar().setVisible(false, ITitleBarLayout.POSITION.RIGHT);
        getPickerListLayout().setOnItemAddListener(new ItemView.OnAddListener() {
            @Override
            public void onAdd(TCVideoFileInfo fileInfo) {
                // 选中一个视频
                getPickedLayout().addItem(fileInfo);
            }
        });
    }

    public void loadVideoList() {
        mActivity = (Activity) getContext();
        mHandlder.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<TCVideoFileInfo> list = PickerManagerKit.getInstance(mActivity).getAllVideo();
                getPickerListLayout().updateItems(list);
            }
        });
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
    public void setOnPickerListener(@Nullable final OnPickerListener listener) {
        getPickedLayout().setOnNextStepListener(new PickedLayout.OnNextStepListener() {
            @Override
            public void onNextStep() {
                if (listener != null) {
                    ArrayList<TCVideoFileInfo> arrayList = getPickedLayout().getSelectItems(PickedLayout.TYPE_VIDEO);
                    listener.onPickedList(arrayList);
                }
            }
        });
    }
}
