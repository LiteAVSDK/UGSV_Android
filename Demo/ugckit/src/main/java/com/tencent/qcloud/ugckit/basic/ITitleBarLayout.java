package com.tencent.qcloud.ugckit.basic;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 标题栏接口，标题栏设计为左中右三部分标题，左边为图片+文字，中间为文字，右边也会图片+文字
 */
public interface ITitleBarLayout {

    /**
     * 设置左边标题的图片
     *
     * @param resId
     */
    void setLeftIcon(int resId);

    /**
     * 设置左边标题的点击事件
     *
     * @param listener
     */
    void setOnBackClickListener(View.OnClickListener listener);

    /**
     * 设置右边标题的点击事件
     *
     * @param listener
     */
    void setOnRightClickListener(View.OnClickListener listener);

    /**
     * 设置标题
     *
     * @param title    标题内容
     * @param position 标题位置
     */
    void setTitle(String title, POSITION position);

    /**
     * @param enable
     * @param position
     */
    void setVisible(boolean enable, POSITION position);

    /**
     * 返回左边标题区域
     *
     * @return
     */
    LinearLayout getLeftGroup();

    /**
     * 返回左边标题的图片
     *
     * @return
     */
    ImageView getLeftIcon();

    /**
     * 返回左边标题的文字
     *
     * @return
     */
    TextView getLeftTitle();

    /**
     * 返回中间标题的文字
     *
     * @return
     */
    TextView getMiddleTitle();

    Button getRightButton();

    enum POSITION {
        /**
         * 左边标题
         */
        LEFT,
        /**
         * 中间标题
         */
        MIDDLE,
        /**
         * 右侧
         */
        RIGHT
    }

}
