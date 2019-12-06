package com.tencent.qcloud.ugckit.module.followRecord;

import android.content.Context;
import android.support.annotation.NonNull;


import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.utils.VideoPathUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoJoiner;

import java.util.ArrayList;
import java.util.List;

/**
 * Module：两个视频画面左右拼接一个视频
 */
public class FollowRecordJoiner implements TXVideoJoiner.TXVideoJoinerListener {
    private Context mContext;
    private TXVideoJoiner mTXVideoJoiner;

    private static FollowRecordJoiner instance;
    private String mVideoOutputPath;
    private OnFollowRecordJoinListener mListener;

    public static FollowRecordJoiner getInstance(Context context) {
        if (instance == null) {
            instance = new FollowRecordJoiner(context);
        }
        return instance;
    }

    private FollowRecordJoiner(Context context) {
        mContext = context.getApplicationContext();
        mTXVideoJoiner = new TXVideoJoiner(context);
        mTXVideoJoiner.setVideoJoinerListener(this);
    }

    @Override
    public void onJoinProgress(float progress) {
        if (mListener != null) {
            mListener.onChorusProgress(progress);
        }
    }

    @Override
    public void onJoinComplete(@NonNull TXVideoEditConstants.TXJoinerResult result) {
        if (result.retCode == TXVideoEditConstants.JOIN_RESULT_OK) {
            if (mListener != null) {
                mListener.onChorusCompleted(mVideoOutputPath);
            }
        } else {
            ToastUtil.toastShortMessage(mContext.getResources().getString(R.string.tc_video_record_activity_on_join_complete_synthesis_failed));
        }
    }

    /**
     * 合成合唱视频
     */
    public void joinChorusVideo() {
        FollowRecordInfo followRecordInfo = FollowRecordConfig.getInstance().videoInfo;
        TXVideoEditConstants.TXVideoInfo recordInfo = FollowRecordConfig.getInstance().recordVideoInfo;
        TXVideoEditConstants.TXVideoInfo playInfo = FollowRecordConfig.getInstance().playVideoInfo;

        List<String> videoSourceList = new ArrayList<>();
        videoSourceList.add(followRecordInfo.recordPath);
        videoSourceList.add(followRecordInfo.playPath);

        mTXVideoJoiner.setVideoPathList(videoSourceList);
        mVideoOutputPath = VideoPathUtil.getCustomVideoOutputPath("Follow_Shot_");

        // 以左边录制的视频宽高为基准，右边视频等比例缩放
        int playWidth;
        int playHeight;
        if ((float) playInfo.width / playInfo.height >= (float) recordInfo.width / recordInfo.height) {
            playWidth = recordInfo.width;
            playHeight = (int) ((float) recordInfo.width * playInfo.height / playInfo.width);
        } else {
            playWidth = (int) ((float) recordInfo.height * playInfo.width / playInfo.height);
            playHeight = recordInfo.height;
        }

        TXVideoEditConstants.TXAbsoluteRect rect1 = new TXVideoEditConstants.TXAbsoluteRect();
        rect1.x = 0;
        rect1.y = 0;
        rect1.width = recordInfo.width;
        rect1.height = recordInfo.height;

        TXVideoEditConstants.TXAbsoluteRect rect2 = new TXVideoEditConstants.TXAbsoluteRect();
        rect2.x = rect1.x + rect1.width;
        rect2.y = (recordInfo.height - playHeight) / 2;
        rect2.width = playWidth;
        rect2.height = playHeight;

        List<TXVideoEditConstants.TXAbsoluteRect> list = new ArrayList<>();
        list.add(rect1);
        list.add(rect2);
        mTXVideoJoiner.setSplitScreenList(list, recordInfo.width + playWidth, recordInfo.height);
        mTXVideoJoiner.splitJoinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
    }

    public void setChorusJoinListener(OnFollowRecordJoinListener listener) {
        mListener = listener;
    }

    public interface OnFollowRecordJoinListener {

        void onChorusProgress(float progress);

        void onChorusCompleted(String outputPath);
    }
}
