package com.tencent.qcloud.ugckit.module.effect.transition;

import com.tencent.qcloud.ugckit.component.timeline.ColorfulProgress;

import java.util.ArrayList;
import java.util.List;

public class TCTransitionViewInfoManager {
    private List<ColorfulProgress.MarkInfo> mMarkInfoList;

    public static TCTransitionViewInfoManager getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static TCTransitionViewInfoManager instance = new TCTransitionViewInfoManager();
    }

    private TCTransitionViewInfoManager() {
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
