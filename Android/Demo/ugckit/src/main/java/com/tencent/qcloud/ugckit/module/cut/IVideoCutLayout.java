package com.tencent.qcloud.ugckit.module.cut;

import android.graphics.Bitmap;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.tencent.ugc.TXVideoEditConstants;

/**
 * 定制化裁剪条
 */
public interface IVideoCutLayout {
    /**
     * 默认缩略图宽
     */
    int DEFAULT_THUMBNAIL_WIDTH  = 100;
    /**
     * 默认缩略图高
     */
    int DEFAULT_THUMBNAIL_HEIGHT = 100;
    /**
     * 最大时长，默认16秒
     */
    int MAX_DURATION             = 16;

    /**
     * 设置视频信息
     *
     * @param videoInfo
     */
    void setVideoInfo(TXVideoEditConstants.TXVideoInfo videoInfo);

    /**
     * 添加一张缩略图
     */
    void addThumbnail(int index, Bitmap bitmap);

    /**
     * 设置视频旋转监听器
     *
     * @param listener
     */
    void setOnRotateVideoListener(OnRotateVideoListener listener);

    interface OnRotateVideoListener {
        /**
         * 视频旋转
         *
         * @param rotation 视频旋转角度
         */
        void onRotate(int rotation);
    }

}
