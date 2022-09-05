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
 * 腾讯云短视频UGCKit:图片选择控件
 * <p>
 * 功能：用于实现相册图片的选择。<p/>
 * <p>
 * 1、加载本地了本地相册所有图片<br>
 */
public class UGCKitPicturePicker extends AbsPickerUI {
    private static final int MIN_SELECTED_PIC_COUNT = 3;
    private Activity mActivity;
    @NonNull
    private Handler mHandlder = new Handler();

    public UGCKitPicturePicker(Context context) {
        super(context);
        initDefault();
    }

    public UGCKitPicturePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public UGCKitPicturePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
    }

    @Override
    public void initDefault() {
        // 设置标题
        getTitleBar().setTitle(getResources().getString(R.string.ugckit_picture_choose), ITitleBarLayout.POSITION.MIDDLE);
        getTitleBar().setVisible(false, ITitleBarLayout.POSITION.RIGHT);
        getPickerListLayout().setOnItemAddListener(new ItemView.OnAddListener() {
            @Override
            public void onAdd(TCVideoFileInfo fileInfo) {
                // 选中一个图片
                getPickedLayout().addItem(fileInfo);
            }
        });
        // 加载图片
        getPickedLayout().setMinSelectedItemCount(MIN_SELECTED_PIC_COUNT);
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

    /**
     * 功能：加载本地所有图片</p>
     * 包含：<p/>
     * 1、已授权读取手机SD卡权限，则通过{@link PickerManagerKit#getAllPictrue()} 读取本地相册扫描出的所有图片
     * 2、未授权读取手机SD卡权限，在Android6.0需要动态获取权限
     */
    public void loadPictureList() {
        mActivity = (Activity) getContext();
        mHandlder.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<TCVideoFileInfo> list = PickerManagerKit.getInstance(mActivity).getAllPictrue();
                getPickerListLayout().updateItems(list);
            }
        });
    }

    @Override
    public void setOnPickerListener(@Nullable final OnPickerListener listener) {
        getPickedLayout().setOnNextStepListener(new PickedLayout.OnNextStepListener() {
            @Override
            public void onNextStep() {
                if (listener != null) {
                    ArrayList<TCVideoFileInfo> arrayList = getPickedLayout().getSelectItems(PickedLayout.TYPE_PICTURE);
                    listener.onPickedList(arrayList);
                }
            }
        });
    }
}
