package com.tencent.qcloud.ugckit.module.effect.time;

public class TCTimeViewInfoManager {
    private static TCTimeViewInfoManager sInstance;
    private        int                   mEffect = TimeEffect.NONE_EFFECT;
    private        long                  mCurrentStartMs;

    public static TCTimeViewInfoManager getInstance() {
        if (sInstance == null) {
            synchronized (TCTimeViewInfoManager.class) {
                if (sInstance == null) {
                    sInstance = new TCTimeViewInfoManager();
                }
            }
        }
        return sInstance;
    }

    public void setCurrentEffect(int effect, long startMs) {
        mEffect = effect;
        mCurrentStartMs = startMs;
    }

    public int getCurrentEffect() {
        return mEffect;
    }

    public long getCurrentStartMs() {
        return mCurrentStartMs;
    }

    public void clearEffect() {
        mEffect = TimeEffect.NONE_EFFECT;
        mCurrentStartMs = 0;
    }

}
