package com.tencent.qcloud.ugckit;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.View;


import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.join.IVideoJoinKit;
import com.tencent.qcloud.ugckit.utils.DialogUtil;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.utils.VideoPathUtil;
import com.tencent.qcloud.ugckit.component.dialogfragment.ProgressFragmentUtil;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;

import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoJoiner;

import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云短视频UGCKit:多个视频合成组件</p>
 * <p>
 * VideoJoinKit功能： <p>
 * 1、调用{@link UGCKitVideoJoin#setVideoJoinList(ArrayList)} 设置多个视频路径<p>
 * 2、调用{@link UGCKitVideoJoin#setVideoJoinListener(IVideoJoinKit.OnVideoJoinListener)} 设置视频合成监听器<p>
 * {@link IVideoJoinKit.OnVideoJoinListener#onJoinCompleted(UGCKitResult)} ()} 表示视频合成完成，返回合成的视频路径<p>
 * {@link IVideoJoinKit.OnVideoJoinListener#onJoinCanceled()} ()} 表示当前合成视频动作取消。<p>
 * <p>
 * SDK调用步骤：<p>
 * 1、创建TXVideoJoiner
 * 2、调用{@link TXVideoJoiner#setVideoPathList(List)} 设置多个视频路径
 * 3、调用{@link TXVideoJoiner#setVideoJoinerListener(TXVideoJoiner.TXVideoJoinerListener)} 设置合成的监听器
 * 4、调用{@link TXVideoJoiner#joinVideo(int, String)}
 *
 *
 * Tencent Cloud UGCKit: Splicing Videos</p>
 * <p>
 * VideoJoinKit capabilities: <p>
 * 1. Call {@link UGCKitVideoJoin#setVideoJoinList(ArrayList)} to specify video paths.<p>
 * 2. Call {@link UGCKitVideoJoin#setVideoJoinListener(IVideoJoinKit.OnVideoJoinListener)}
 * to set the video splicing listener.<p>
 * {@link IVideoJoinKit.OnVideoJoinListener#onJoinCompleted(UGCKitResult)} ()}
 * indicates that the videos have been spliced, and the path of the splicing result is returned.<p>
 * {@link IVideoJoinKit.OnVideoJoinListener#onJoinCanceled()} ()} indicates that the splicing has been canceled.<p>
 * <p>
 * Directions:<p>
 * 1. Create TXVideoJoiner.
 * 2. Call {@link TXVideoJoiner#setVideoPathList(List)} to specify the paths of videos to splice.
 * 3. Call {@link TXVideoJoiner#setVideoJoinerListener(TXVideoJoiner.TXVideoJoinerListener)}
 * to set the video splicing listener.
 * 4. Call {@link TXVideoJoiner#joinVideo(int, String)}.
 */


public class UGCKitVideoJoin implements IVideoJoinKit, TXVideoJoiner.TXVideoJoinerListener {
    private static final String TAG = "UGCKitVideoJoin";

    private final FragmentActivity                  mContext;
    private       TXVideoJoiner                     mTXVideoJoiner;
    private       boolean                           mGenerateSuccess;
    private       String                            mOutputPath;
    private       IVideoJoinKit.OnVideoJoinListener mOnVideoJoinListener;
    private       ProgressFragmentUtil              mProgressFragmentUtil;
    private       ArrayList<TCVideoFileInfo>        mTCVideoFileInfoList;

    public UGCKitVideoJoin(FragmentActivity context) {
        mContext = context;
    }

    @Override
    public void setVideoJoinList(ArrayList<TCVideoFileInfo> videoList) {
        mTCVideoFileInfoList = videoList;
        if (mTCVideoFileInfoList == null || mTCVideoFileInfoList.size() == 0) {
            if (mOnVideoJoinListener != null) {
                mOnVideoJoinListener.onJoinCanceled();
            }
            return;
        }
        startJoin();
    }

    @Override
    public void setVideoJoinListener(IVideoJoinKit.OnVideoJoinListener videoJoinListener) {
        mOnVideoJoinListener = videoJoinListener;
    }

    /**
     * 开始视频合成
     */
    private void startJoin() {
        ArrayList<String> videoSourceList = convertJoinPathList();
        mTXVideoJoiner = new TXVideoJoiner(mContext);
        int ret = mTXVideoJoiner.setVideoPathList(videoSourceList);
        if (ret == TXVideoEditConstants.ERR_UNSUPPORT_VIDEO_FORMAT) {
            DialogUtil.showDialog(mContext, mContext.getString(R.string.ugckit_video_join_fail_title),
                    mContext.getString(R.string.ugckit_video_join_not_support_video_format), null);
        } else if (ret == TXVideoEditConstants.ERR_UNSUPPORT_AUDIO_FORMAT) {
            DialogUtil.showDialog(mContext, mContext.getString(R.string.ugckit_video_join_fail_title),
                    mContext.getString(R.string.ugckit_video_join_not_support_non_mono_format),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnVideoJoinListener != null) {
                                mOnVideoJoinListener.onJoinCanceled();
                            }
                        }
                    });
        }
        mTXVideoJoiner.setVideoJoinerListener(this);
        mOutputPath = VideoPathUtil.generateVideoPath();

        mProgressFragmentUtil = new ProgressFragmentUtil(mContext, mContext.getResources().getString(R.string.ugckit_video_joining));
        mProgressFragmentUtil.showLoadingProgress(new ProgressFragmentUtil.IProgressListener() {
            @Override
            public void onStop() {
                mProgressFragmentUtil.dismissLoadingProgress();
                ToastUtil.toastShortMessage(mContext.getString(R.string.ugckit_cancel_joining));
                cancelJoin();
            }
        });
        mTXVideoJoiner.joinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mOutputPath);
    }

    @NonNull
    private ArrayList<String> convertJoinPathList() {
        ArrayList<String> sourceList = new ArrayList<String>();
        for (int i = 0; i < mTCVideoFileInfoList.size(); i++) {
            sourceList.add(mTCVideoFileInfoList.get(i).getFilePath());
        }
        return sourceList;
    }

    /**
     * 取消视频合成
     */
    private void cancelJoin() {
        if (!mGenerateSuccess) {
            if (mTXVideoJoiner != null) {
                mTXVideoJoiner.cancel();
            }

            if (mOnVideoJoinListener != null) {
                mOnVideoJoinListener.onJoinCanceled();
            }
        }
    }

    /************************************************************************/
    /*****                     SDK回调函数                                *****/
    /************************************************************************/
    @Override
    public void onJoinProgress(float progress) {
        Log.d(TAG, "onJoinProgress = " + progress);
        mProgressFragmentUtil.updateGenerateProgress((int) (progress * 100));
    }

    /**
     * 功能：合成视频回调函数<p/>
     * 您可以通过{@link com.tencent.ugc.TXVideoEditConstants.TXJoinerResult#retCode} 来判断合成成功或者失败<p/>
     * 当retCode值为 TXVideoEditConstants.JOIN_RESULT_OK 表示生成成功<p/>
     * 当retCode值为 TXVideoEditConstants.JOIN_RESULT_FAILED 表示生成失败，此时您可以用 {@link com.tencent.ugc.TXVideoEditConstants.TXGenerateResult#descMsg} 来获取失败原因的详细描述<p/>
     * 当retCode值为 TXVideoEditConstants.JOIN_RESULT_LICENCE_VERIFICATION_FAILED 表示Licence校验失败
     *
     * @param result 返回生成视频的结果
     */
    @Override
    public void onJoinComplete(@NonNull TXVideoEditConstants.TXJoinerResult result) {
        LogReport.getInstance().reportVideoJoin(result.retCode);

        mProgressFragmentUtil.dismissLoadingProgress();

        if (result.retCode == TXVideoEditConstants.JOIN_RESULT_OK) {
            if (mOnVideoJoinListener != null) {
                UGCKitResult ugcKitResult = new UGCKitResult();
                ugcKitResult.errorCode = result.retCode;
                ugcKitResult.descMsg = result.descMsg;
                ugcKitResult.outputPath = mOutputPath;
                mOnVideoJoinListener.onJoinCompleted(ugcKitResult);
            }
            mGenerateSuccess = true;
        }
    }

}
