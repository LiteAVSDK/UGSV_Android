package com.tencent.qcloud.ugckit.module.picker.view;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;

import java.util.ArrayList;

/**
 * 选择器UI定制化
 */
public interface IPickedLayout {
    int TYPE_PICTURE = 1;
    int TYPE_VIDEO   = 0;

    /**
     * 添加一个到已选中列表
     *
     * @param item
     */
    void addItem(TCVideoFileInfo item);

    /**
     * 获取所有选中的Item
     *
     * @param type {@link #TYPE_VIDEO}
     *             {@link #TYPE_PICTURE}
     */
    ArrayList<TCVideoFileInfo> getSelectItems(int type);

    /**
     * 设置下一步监听器
     *
     * @param listener
     */
    void setOnNextStepListener(PickedLayout.OnNextStepListener listener);

    /**
     * 设置拖拽文字提示
     *
     * @param dragTipText
     */
    void setDragTipText(String dragTipText);

    /**
     * 设置已选中的删除按钮Icon
     *
     * @param resId
     */
    void setRemoveIconResource(@DrawableRes int resId);

    /**
     * 设置"下一步"文字大小
     *
     * @param textSize
     */
    void setNextTextSize(int textSize);

    /**
     * "下一步"文字颜色
     *
     * @param textColor
     */
    void setTextColor(@ColorRes int textColor);

    /**
     * 设置"下一步"文字背景色（选中/不可选择）
     *
     * @param resid
     */
    void setButtonBackgroundResource(@DrawableRes int resid);

    /**
     * 设置已选中的图片宽
     *
     * @param bitmapWidth
     */
    void setBitmapWidth(int bitmapWidth);

    /**
     * 设置已选中的图片高
     *
     * @param bitmapHeight
     */
    void setBitmapHeight(int bitmapHeight);

    /**
     * 设置最少选中的item数量 (少于此数量，下一步按钮将置灰)
     *
     * @param minSelectedItemCount
     */
    void setMinSelectedItemCount(int minSelectedItemCount);
}
