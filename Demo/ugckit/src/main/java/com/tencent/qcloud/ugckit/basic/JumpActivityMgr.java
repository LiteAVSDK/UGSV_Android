package com.tencent.qcloud.ugckit.basic;

import androidx.annotation.NonNull;

/**
 * Activity跳转的信息保存
 */
public class JumpActivityMgr {

    @NonNull
    private static JumpActivityMgr sInstance     = new JumpActivityMgr();
    private        boolean         mCutVideoFlag = true;
    private        boolean         mQuickImport  = false;

    private JumpActivityMgr() {
        mCutVideoFlag = true;
    }

    @NonNull
    public static JumpActivityMgr getInstance() {
        return sInstance;
    }

    /**
     * 设置"视频裁剪页面"是否进行"视频编辑"
     *
     * @param flag
     */
    public void setEditFlagFromCut(boolean flag) {
        mCutVideoFlag = flag;
    }

    public boolean getEditFlagFromCut() {
        return mCutVideoFlag;
    }

    public void setQuickImport(boolean quickImport) {
        mQuickImport = quickImport;
    }

    public boolean isQuickImport() {
        return mQuickImport;
    }
}
