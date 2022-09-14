package com.tencent.qcloud.ugckit.module.effect.bubble;


import com.tencent.qcloud.ugckit.component.bubbleview.BubbleViewParams;

/**
 * 用于保存 气泡字幕控件相关参数的类
 * <p>
 * 主要是用于恢复编辑字幕的场景
 */
public class TCBubbleViewInfo {
    private float            viewCenterX;
    private float            viewCenterY;// 控件的x y
    private float            imageRotation;
    private long             startTime;
    private long             endTime;
    private float            scale;
    private BubbleViewParams viewParams; //气泡字幕配置参数

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getViewCenterX() {
        return viewCenterX;
    }

    public void setViewCenterX(float viewCenterX) {
        this.viewCenterX = viewCenterX;
    }

    public float getViewCenterY() {
        return viewCenterY;
    }

    public void setViewCenterY(float viewCenterY) {
        this.viewCenterY = viewCenterY;
    }

    public float getRotation() {
        return imageRotation;
    }

    public void setRotation(float viewRotation) {
        this.imageRotation = viewRotation;
    }

    public BubbleViewParams getViewParams() {
        return viewParams;
    }

    public void setViewParams(BubbleViewParams bubbleInfo) {
        this.viewParams = bubbleInfo;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
