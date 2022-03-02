package com.tencent.qcloud.ugckit.module.effect;

public class TimelineViewUtil {

    private TimeLineView mTimeLineView;

    private TimelineViewUtil() {
    }

    private static TimelineViewUtil instance;

    public static TimelineViewUtil getInstance() {
        if (instance == null) {
            instance = new TimelineViewUtil();
        }
        return instance;
    }

    public void setTimelineView(TimeLineView timeLineView) {
        mTimeLineView = timeLineView;
    }

    public TimeLineView getTimeLineView() {
        return mTimeLineView;
    }

    public void release() {
        mTimeLineView = null;
    }
}
