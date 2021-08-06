package com.tencent.qcloud.ugckit.module.effect.bubble;

import java.util.ArrayList;
import java.util.List;

/**
 * 仅用于退出和进入气泡字幕编辑Activity的时候使用：
 * <p>
 * 1. 退出：保存所有View的参数信息
 * 2. 进入：将保存的View的参数信息恢复
 */

public class TCBubbleViewInfoManager {
    private static TCBubbleViewInfoManager mManager;
    private        List<TCBubbleViewInfo>  mList;

    public static TCBubbleViewInfoManager getInstance() {
        if (mManager == null) {
            synchronized (TCBubbleViewInfoManager.class) {
                if (mManager == null) {
                    mManager = new TCBubbleViewInfoManager();
                }
            }
        }
        return mManager;
    }

    private TCBubbleViewInfoManager() {
        mList = new ArrayList<>();
    }

    public void add(TCBubbleViewInfo info) {
        mList.add(info);
    }

    public void remove(int index) {
        mList.remove(index);
    }

    public void clear() {
        mList.clear();
    }

    public TCBubbleViewInfo get(int index) {
        return mList.get(index);
    }

    public int size() {
        return mList.size();
    }
}
