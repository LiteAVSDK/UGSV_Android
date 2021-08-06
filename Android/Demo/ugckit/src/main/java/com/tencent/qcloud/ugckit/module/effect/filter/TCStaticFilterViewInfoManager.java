package com.tencent.qcloud.ugckit.module.effect.filter;

public class TCStaticFilterViewInfoManager {
    private static TCStaticFilterViewInfoManager sInstance;
    private        int                           mCurrentPosition = 0;

    public static TCStaticFilterViewInfoManager getInstance() {
        if (sInstance == null) {
            synchronized (TCStaticFilterViewInfoManager.class) {
                if (sInstance == null) {
                    sInstance = new TCStaticFilterViewInfoManager();
                }
            }
        }
        return sInstance;
    }

    public void setCurrentPosition(int position) {
        this.mCurrentPosition = position;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void clearCurrentPosition() {
        this.mCurrentPosition = 0;
    }

}
