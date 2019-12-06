package com.tencent.qcloud.ugckit.module.effect.time;

public class TCTimeViewInfoManager {
    private static TCTimeViewInfoManager instance;
    private int mEffect = TCTimeFragment.NONE_EFFECT;
    private long mCurrentStartMs;

    public static TCTimeViewInfoManager getInstance(){
        if(instance == null){
            synchronized (TCTimeViewInfoManager.class){
                if(instance == null){
                    instance = new TCTimeViewInfoManager();
                }
            }
        }
        return instance;
    }

    public void setCurrentEffect(int effect, long startMs){
        mEffect = effect;
        mCurrentStartMs = startMs;
    }

    public int getCurrentEffect(){
        return mEffect;
    }

    public long getCurrentStartMs(){
        return mCurrentStartMs;
    }

    public void clearEffect(){
        mEffect = TCTimeFragment.NONE_EFFECT;
        mCurrentStartMs = 0;
    }

}
