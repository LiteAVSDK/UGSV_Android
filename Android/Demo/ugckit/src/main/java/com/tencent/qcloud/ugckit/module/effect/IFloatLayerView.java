package com.tencent.qcloud.ugckit.module.effect;

import android.graphics.Bitmap;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

/**
 * 定制化气泡字幕/贴纸的浮层View
 */
public interface IFloatLayerView {
    /**
     * 图片的最大缩放比例
     */
    float MAX_SCALE = 4.0f;
    /**
     * 图片的最小缩放比例
     */
    float MIN_SCALE = 0.3f;

    /**
     * 是否显示删除浮层icon
     *
     * @param flag {@code true} 显示
     *             {@code false } 不显示
     */
    void showDelete(boolean flag);

    /**
     * 是否显示编辑浮层icon
     *
     * @param flag {@code true} 显示
     *             {@code false } 不显示
     */
    void showEdit(boolean flag);

    /**
     * 设置添加的浮层开始在视频上的开始时间和结束时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    void setStartToEndTime(long startTime, long endTime);

    /**
     * 添加的浮层开始在视频上的开始时间
     *
     * @return
     */
    long getStartTime();

    /**
     * 添加的浮层开始在视频上的结束时间
     *
     * @return
     */
    long getEndTime();

    /**
     * 设置浮层x方向 在父控件位置
     *
     * @param x
     */
    void setCenterX(float x);

    /**
     * 获取浮层x方向中心点
     *
     * @return
     */
    float getCenterX();

    /**
     * 设置浮层y方向 在父控件位置
     *
     * @param y
     */
    void setCenterY(float y);

    /**
     * 获取浮层y方向中心点
     *
     * @return
     */
    float getCenterY();

    /**
     * 设置经过旋转的图片
     *
     * @param bitmap
     */
    void setImageBitamp(@Nullable Bitmap bitmap);

    /**
     * 设置FloatLayerView监听器
     *
     * @param listener
     */
    void setIOperationViewClickListener(IOperationViewClickListener listener);

    interface IOperationViewClickListener {
        /**
         * 点击删除
         */
        void onDeleteClick();

        /**
         * 点击编辑
         */
        void onEditClick();

        /**
         * 按住拖动旋转
         */
        void onRotateClick();
    }

    /**
     * 外边框与图片之间的间距
     */
    void setPadding(int padding);

    /**
     * 外边框线条宽度
     */
    void setBorderWidth(int borderWidth);

    /**
     * 外边框线条颜色
     */
    void setBorderColor(@ColorRes int color);

    /**
     * 编辑Icon
     */
    void setEditIconResource(@DrawableRes int resid);

    /**
     * 旋转Icon
     */
    void setRotateIconResource(@DrawableRes int resid);
}
