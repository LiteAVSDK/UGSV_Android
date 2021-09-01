package com.tencent.qcloud.ugckit.module.effect;

import androidx.annotation.NonNull;

import com.tencent.qcloud.ugckit.module.effect.bubble.TCBubbleViewInfoManager;
import com.tencent.qcloud.ugckit.module.effect.filter.TCStaticFilterViewInfoManager;
import com.tencent.qcloud.ugckit.module.effect.motion.TCMotionViewInfoManager;
import com.tencent.qcloud.ugckit.module.effect.paster.TCPasterViewInfoManager;
import com.tencent.qcloud.ugckit.module.effect.time.TCTimeViewInfoManager;
import com.tencent.qcloud.ugckit.module.effect.transition.TCTransitionViewInfoManager;
import com.tencent.qcloud.ugckit.module.effect.utils.DraftEditer;
import com.tencent.qcloud.ugckit.module.effect.utils.EffectEditer;

public class Config {
    @NonNull
    private static Config sInstance = new Config();

    private Config() {
    }

    @NonNull
    public static Config getInstance() {
        return sInstance;
    }

    public void clearConfig() {
        // 清空BGM的设置
        DraftEditer.getInstance().clear();
        EffectEditer.getInstance().clear();
        // 清空保存的气泡字幕参数 （避免下一个视频混入上一个视频的气泡设定)
        TCBubbleViewInfoManager.getInstance().clear();
        // 清空保存的贴纸参数
        TCPasterViewInfoManager.getInstance().clear();
        // 清空滤镜动效的状态
        TCMotionViewInfoManager.getInstance().clearMarkInfoList();
        // 清空转场动效的状态
        TCTransitionViewInfoManager.getInstance().clearMarkInfoList();
        // 清空时间特效的状态
        TCTimeViewInfoManager.getInstance().clearEffect();
        // 清空色调（滤镜）的状态
        TCStaticFilterViewInfoManager.getInstance().clearCurrentPosition();
    }
}
