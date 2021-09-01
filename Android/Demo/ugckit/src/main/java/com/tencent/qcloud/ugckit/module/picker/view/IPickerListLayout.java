package com.tencent.qcloud.ugckit.module.picker.view;

import androidx.annotation.NonNull;

import com.tencent.qcloud.ugckit.module.picker.data.ItemView;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;

import java.util.ArrayList;

public interface IPickerListLayout {
    /**
     * 添加item监听器
     *
     * @param listener
     */
    void setOnItemAddListener(ItemView.OnAddListener listener);

    /**
     * 暂停加载图片
     */
    void pauseRequestBitmap();

    /**
     * 继续加载图片
     */
    void resumeRequestBitmap();

    /**
     * 更新列表项
     *
     * @param list
     */
    void updateItems(@NonNull ArrayList<TCVideoFileInfo> list);
}
