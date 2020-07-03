package com.tencent.qcloud.xiaoshipin.videojoiner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.join.IVideoJoinKit;
import com.tencent.qcloud.ugckit.UGCKitVideoJoin;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.videoeditor.TCVideoCutActivity;

import java.util.ArrayList;

/**
 * 小视频合成界面
 */
public class TCVideoJoinerActivity extends FragmentActivity {

    private ArrayList<TCVideoFileInfo> mTCVideoFileInfoList;
    private UGCKitVideoJoin mUGCKitVideoJoin;
    private IVideoJoinKit.OnVideoJoinListener mOnPictureListener = new IVideoJoinKit.OnVideoJoinListener() {
        @Override
        public void onJoinCanceled() {
            /**
             * 视频合成被取消，退出视频合成界面
             */
            finish();
        }

        @Override
        public void onJoinCompleted(UGCKitResult ugcKitResult) {
            /**
             * 视频合成完成，返回合成后的视频地址，跳转到视频裁剪页面
             */
            if (ugcKitResult.errorCode == 0) {
                startCutActivity(ugcKitResult);
            } else {
                ToastUtil.toastShortMessage("join video failed. error code:" + ugcKitResult.errorCode + ",desc msg:" + ugcKitResult.descMsg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowParam();
        mTCVideoFileInfoList = (ArrayList<TCVideoFileInfo>) getIntent().getSerializableExtra(UGCKitConstants.INTENT_KEY_MULTI_CHOOSE);

        mUGCKitVideoJoin = new UGCKitVideoJoin(this);
        /**
         * 设置合成的视频源
         */
        mUGCKitVideoJoin.setVideoJoinList(mTCVideoFileInfoList);
    }

    private void initWindowParam() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 跳转到视频裁剪界面
     *
     * @param ugcKitResult
     */
    private void startCutActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent(TCVideoJoinerActivity.this, TCVideoCutActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 设置合成视频的监听器
         */
        mUGCKitVideoJoin.setVideoJoinListener(mOnPictureListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUGCKitVideoJoin.setVideoJoinListener(null);
    }
}
