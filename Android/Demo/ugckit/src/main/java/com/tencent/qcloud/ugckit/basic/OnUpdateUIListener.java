package com.tencent.qcloud.ugckit.basic;

/**
 * 更新UI监听者
 */
public interface OnUpdateUIListener {
    /**
     * 更新进度条进度
     *
     * @param progress
     */
    void onUIProgress(float progress);

    /**
     * 操作执行完成，更新UI
     *
     * @param retCode
     * @param descMsg
     */
    void onUIComplete(int retCode, String descMsg);

    /**
     * 操作取消，更新UI
     */
    void onUICancel();
}
