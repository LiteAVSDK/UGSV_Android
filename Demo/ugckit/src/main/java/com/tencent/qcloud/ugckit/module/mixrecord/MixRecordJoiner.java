package com.tencent.qcloud.ugckit.module.mixrecord;

import android.content.Context;
import android.text.TextUtils;

import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoJoiner;

import java.util.ArrayList;
import java.util.List;

public class MixRecordJoiner implements TXVideoJoiner.TXVideoJoinerListener {
    private Context                mContext;
    private TXVideoJoiner          mVideoJoiner;
    private IMixRecordJoinListener mListener;
    private String                 mOutputPath;

    public MixRecordJoiner(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        this.mVideoJoiner = new TXVideoJoiner(this.mContext);
        mVideoJoiner.setVideoJoinerListener(this);
    }

    public void setmListener(IMixRecordJoinListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onJoinProgress(float progress) {
        if (mListener != null) {
            mListener.onChorusProgress(progress);
        }
    }

    @Override
    public void onJoinComplete(TXVideoEditConstants.TXJoinerResult result) {
        if (result.retCode != TXVideoEditConstants.JOIN_RESULT_OK) {
            ToastUtil.toastShortMessage(TextUtils.isEmpty(result.descMsg) ? mContext.getResources().getString(R.string.ugckit_video_record_activity_on_join_complete_synthesis_failed)
                    : result.descMsg);
        }
        if (mListener != null) {
            mListener.onChorusCompleted(mOutputPath, result.retCode == TXVideoEditConstants.JOIN_RESULT_OK);
        }
    }

    public void joinVideo(JoinerParams params) {
        mOutputPath = params.mVideoOutputPath;
        mVideoJoiner.setVideoPathList(params.videoSourceList);
        mVideoJoiner.setSplitScreenList(params.mRects, params.mCavasWith, params.mCavasHeight);
        mVideoJoiner.setVideoVolumes(params.mVolumes);
        mVideoJoiner.splitJoinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, params.mVideoOutputPath);
    }

    public void setRecordPath(String recordPath) {
        mVideoJoiner.setRecordPath(recordPath);
    }

    public static class JoinerParams {
        public List<TXVideoEditConstants.TXAbsoluteRect> mRects;
        public int                                       mCavasWith;
        public int                                       mCavasHeight;
        public String                                    mVideoOutputPath;
        public List<String>                              videoSourceList;
        public ArrayList<Float>                          mVolumes;
    }
}
