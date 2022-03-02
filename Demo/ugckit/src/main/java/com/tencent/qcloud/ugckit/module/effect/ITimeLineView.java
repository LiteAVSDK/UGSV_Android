package com.tencent.qcloud.ugckit.module.effect;

import androidx.annotation.DrawableRes;

import com.tencent.qcloud.ugckit.UGCKitConstants;

/**
 * 定制化图片时间轴UI
 */
public interface ITimeLineView {
    /**
     * 初始化进度布局
     */
    void initVideoProgressLayout();

    /**
     * 根据显示的特效类型更新UI
     *
     * @param type {@link UGCKitConstants#TYPE_EDITER_BGM} 添加背景音<br>
     *             {@link UGCKitConstants#TYPE_EDITER_MOTION} 添加动态滤镜<br>
     *             {@link UGCKitConstants#TYPE_EDITER_SPEED} 添加时间特效<br>
     *             {@link UGCKitConstants#TYPE_EDITER_FILTER} 添加静态滤镜<br>
     *             {@link UGCKitConstants#TYPE_EDITER_PASTER} 添加贴纸<br>
     *             {@link UGCKitConstants#TYPE_EDITER_SUBTITLE} 添加字幕<br>
     *             {@link UGCKitConstants#TYPE_EDITER_TRANSITION} 添加转场特效<br>
     */
    void updateUIByFragment(int type);

    /**
     * 当前时间点的Icon
     */
    void setCurrentProgessIconResource(@DrawableRes int resid);

}
