package com.tencent.qcloud.ugckit.module.picturetransition;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.util.ArrayList;

public class PictureTransitionKit {
    @NonNull
    private static       PictureTransitionKit sInstance          = new PictureTransitionKit();
    private static final int                  DEFAULT_FPS        = 20;  // 默认图片帧率
    public static final  int                  DEFAULT_TRANSITION = TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING;  //  默认转场动画类型

    private PictureTransitionKit() {

    }

    @NonNull
    public static PictureTransitionKit getInstance() {
        return sInstance;
    }

    /**
     * 计算图片加上转场动画后合成视频的时长
     *
     * @param type
     * @return
     */
    public long pictureTransition(int type) {
        long duration = 0;
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer == null) {
            return duration;
        }
        switch (type) {
            case TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING:
                duration = editer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING);
                break;
            case TXVideoEditConstants.TX_TRANSITION_TYPE_UP_DOWN_SLIPPING:
                duration = editer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_UP_DOWN_SLIPPING);
                break;
            case TXVideoEditConstants.TX_TRANSITION_TYPE_ENLARGE:
                duration = editer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_ENLARGE);
                break;
            case TXVideoEditConstants.TX_TRANSITION_TYPE_NARROW:
                duration = editer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_NARROW);
                break;
            case TXVideoEditConstants.TX_TRANSITION_TYPE_ROTATIONAL_SCALING:
                duration = editer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_ROTATIONAL_SCALING);
                break;
            case TXVideoEditConstants.TX_TRANSITION_TYPE_FADEIN_FADEOUT:
                duration = editer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_FADEIN_FADEOUT);
                break;
        }
        return duration;
    }

    public int setPictureList(ArrayList<Bitmap> bitmapList) {
        for (Bitmap bitmap : bitmapList) {
            if (bitmap == null) {
                return TXVideoEditConstants.PICTURE_TRANSITION_FAILED;
            }
        }
        return VideoEditerSDK.getInstance().getEditer().setPictureList(bitmapList, DEFAULT_FPS);
    }
}
