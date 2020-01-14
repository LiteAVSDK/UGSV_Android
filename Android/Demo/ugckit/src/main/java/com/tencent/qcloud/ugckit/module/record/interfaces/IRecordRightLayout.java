package com.tencent.qcloud.ugckit.module.record.interfaces;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Size;

import com.tencent.ugc.TXRecordCommon;

/**
 * 录制右侧工具栏定制化UI，包括"音乐Icon"，"屏比Icon"，"美颜Icon"，"音效Icon"
 */
public interface IRecordRightLayout {

    /**
     * 设置点击监听器
     *
     * @param listener
     */
    void setOnItemClickListener(OnItemClickListener listener);

    interface OnItemClickListener {
        /**
         * 显示美颜面板
         */
        void onShowBeautyPanel();

        /**
         * 显示调整音量面板
         */
        void onShowMusicPanel();

        /**
         * 显示音效面板（包括变声、混响）
         */
        void onShowSoundEffectPanel();

        /**
         * 切换屏比
         *
         * @param aspectType {@link TXRecordCommon#VIDEO_ASPECT_RATIO_1_1}
         *                   {@link TXRecordCommon#VIDEO_ASPECT_RATIO_3_4}
         *                   {@link TXRecordCommon#VIDEO_ASPECT_RATIO_9_16}
         */
        void onAspectSelect(int aspectType);
    }

    /**
     * 设置"音乐"按钮是否可用
     *
     * @param enable {@code true} 可点击<br>
     *               {@code false} 不可点击
     */
    void setMusicIconEnable(boolean enable);

    /**
     * 设置"屏比"按钮是否可用
     *
     * @param enable {@code true} 可点击<br>
     *               {@code false} 不可点击
     */
    void setAspectIconEnable(boolean enable);

    /**
     * 设置"音效"按钮是否可用
     *
     * @param enable {@code true} 清除背景音后，音效Icon变为可点击<br>
     *               {@code false} 录制添加BGM后是录制不了人声的，而音效是针对人声有效的，此时开启音效遮罩层，音效Icon变为不可用
     */
    void setSoundEffectIconEnable(boolean enable);

    /************************************************************************/
    /*****                    定制化"音乐图标"                             *****/
    /************************************************************************/

    /**
     * 音乐图标ResId：<pre>R.drawable.icon_music</pre>
     */
    void setMusicIconResource(@DrawableRes int resid);

    /**
     * 音乐图标文字大小
     */
    void setMusicTextSize(int size);

    /**
     * 音乐图标文字颜色
     */
    void setMusicTextColor(@ColorRes int color);
    /************************************************************************/
    /*****                    定制化"屏比图标"                             *****/
    /************************************************************************/

    /**
     * 屏比Icon文字大小
     */
    void setAspectTextSize(int size);

    /**
     * 屏比Icon文字颜色
     */
    void setAspectTextColor(@ColorRes int color);

    /**
     * 设置屏比图标列表，屏比16:9 Icon/屏比4:3 Icon/屏比1:1 Icon
     *
     * @param residList residList[0] 16：9
     *                  residList[1] 4:3
     *                  residList[2] 1:1
     */
    void setAspectIconList(@Size(3) @DrawableRes int[] residList);

    /************************************************************************/
    /*****                    定制化"美颜图标"                             *****/
    /************************************************************************/

    /**
     * 美颜图标ResId：<pre>R.drawable.icon_beauty</pre>
     */
    void setBeautyIconResource(@DrawableRes int resid);

    /**
     * 美颜图标文字大小
     */
    void setBeautyTextSize(int size);

    /**
     * 美颜图标文字颜色
     */
    void setBeautyTextColor(@ColorRes int color);

    /************************************************************************/
    /*****                    定制化"音效图标"                             *****/
    /************************************************************************/

    /**
     * 音效图标ResId：<pre>R.drawable.icon_soundeffect</pre>
     */
    void setSoundEffectIconResource(@DrawableRes int resid);

    /**
     * 音效图标文字大小
     */
    void setSoundEffectTextSize(int size);

    /**
     * 音效图标文字颜色
     */
    void setSoundEffectTextColor(@ColorRes int color);
}
