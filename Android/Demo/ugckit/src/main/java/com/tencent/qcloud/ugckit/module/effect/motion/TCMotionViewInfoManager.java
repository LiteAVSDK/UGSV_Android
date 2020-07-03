package com.tencent.qcloud.ugckit.module.effect.motion;

import com.tencent.qcloud.ugckit.component.timeline.ColorfulProgress;

import java.util.ArrayList;
import java.util.List;

public class TCMotionViewInfoManager {
    private static TCMotionViewInfoManager instance;
    private List<ColorfulProgress.MarkInfo> mMarkInfoList;

    public static TCMotionViewInfoManager getInstance() {
        if (instance == null) {
            synchronized (TCMotionViewInfoManager.class) {
                if (instance == null) {
                    instance = new TCMotionViewInfoManager();
                }
            }
        }
        return instance;
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
