package com.tencent.qcloud.ugckit.module.picker.view;

import com.tencent.qcloud.ugckit.basic.ITitleBar;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;

import java.util.ArrayList;

public interface IPickerLayout extends ITitleBar {

    /**
     * 初始化参数
     */
    void initDefault();

    /**
     * 暂停加载图片列表中图片
     */
    void pauseRequestBitmap();

    /**
     * 继续加载图片列表中图片
     */
    void resumeRequestBitmap();

    /**
     * 设置选择器坚挺着
     *
     * @param listener
     */
    void setOnPickerListener(OnPickerListener listener);

    interface OnPickerListener {
        /**
         * 返回已选中的列表
         *
         * @param list
         */
        void onPickedList(ArrayList<TCVideoFileInfo> list);
    }

    /**
     * 获取图片列表区域Layout
     */
    PickerListLayout getPickerListLayout();

    /**
     * 获取选择图片列表
     */
    PickedLayout getPickedLayout();
}
