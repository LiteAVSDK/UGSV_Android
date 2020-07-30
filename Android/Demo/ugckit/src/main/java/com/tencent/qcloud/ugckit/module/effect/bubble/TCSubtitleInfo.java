package com.tencent.qcloud.ugckit.module.effect.bubble;


/**
 * 保存 从{@link BubbleSubtitlePannel} 之后的设定的 气泡字幕index、以及字体颜色的数据结构
 */
public class TCSubtitleInfo {
    private int          bubblePos;
    private int          textColor;
    private TCBubbleInfo bubbleInfo;


    public int getBubblePos() {
        return bubblePos;
    }

    public void setBubblePos(int bubblePos) {
        this.bubblePos = bubblePos;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public TCBubbleInfo getBubbleInfo() {
        return bubbleInfo;
    }

    public void setBubbleInfo(TCBubbleInfo bubbleInfo) {
        this.bubbleInfo = bubbleInfo;
    }

}
