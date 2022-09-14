package com.tencent.qcloud.ugckit.module.effect.motion;

import com.tencent.qcloud.ugckit.component.timeline.ColorfulProgress;

import java.util.ArrayList;
import java.util.List;

public class TCMotionViewInfoManager {
    private static TCMotionViewInfoManager         sInstance;
    private        List<ColorfulProgress.MarkInfo> mMarkInfoList;

    public static TCMotionViewInfoManager getInstance() {
        if (sInstance == null) {
            synchronized (TCMotionViewInfoManager.class) {
                if (sInstance == null) {
                    sInstance = new TCMotionViewInfoManager();
                }
            }
        }
        return sInstance;
    }

    private TCMotionViewInfoManager() {
        mMarkInfoList = new ArrayList<>();
    }

    public void setMarkInfoList(List<ColorfulProgress.MarkInfo> markInfoList) {
        this.mMarkInfoList = markInfoList;
    }

    public List<ColorfulProgress.MarkInfo> getMarkInfoList() {
        return mMarkInfoList;
    }

    public void clearMarkInfoList() {
        this.mMarkInfoList.clear();
    }

}
