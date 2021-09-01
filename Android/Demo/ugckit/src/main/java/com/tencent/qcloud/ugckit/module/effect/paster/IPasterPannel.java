package com.tencent.qcloud.ugckit.module.effect.paster;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import java.util.List;

/**
 * 贴纸面板
 */
public interface IPasterPannel {
    int TAB_ANIMATED_PASTER = 1;
    int TAB_PASTER          = 2;

    /**
     * 获取当前Tab
     *
     * @return {@link #TAB_PASTER} <br>
     * {@link #TAB_ANIMATED_PASTER}
     */
    int getCurrentTab();

    /**
     * 设置切换Tab监听器
     *
     * @param listener
     */
    void setOnTabChangedListener(IPasterPannel.OnTabChangedListener listener);

    interface OnTabChangedListener {
        /**
         * Tab切换
         *
         * @param currentTab 静态贴纸/动态贴纸
         */
        void onTabChanged(int currentTab);
    }

    /**
     * 根据当前Tab设置贴纸列表
     *
     * @param pasterInfoList 静态贴纸列表或动态贴纸列表
     */
    void setPasterInfoList(List<TCPasterInfo> pasterInfoList);

    /**
     * 显示贴纸面板
     *
     * @param
     */
    void show();

    /**
     * 隐藏贴纸面板
     */
    void dismiss();

    /**
     * 设置贴纸item点击监听器
     *
     * @param listener
     */
    void setOnItemClickListener(OnItemClickListener listener);

    interface OnItemClickListener {
        /**
         * 点击贴纸
         *
         * @param tcPasterInfo 贴纸信息
         * @param position     添加列表的位置
         */
        void onItemClick(TCPasterInfo tcPasterInfo, int position);
    }


    /**
     * 设置添加贴纸监听器
     */
    void setOnAddClickListener(IPasterPannel.OnAddClickListener listener);

    interface OnAddClickListener {
        /**
         * 添加一个贴纸到SDK中
         */
        void onAddPaster();
    }
}
