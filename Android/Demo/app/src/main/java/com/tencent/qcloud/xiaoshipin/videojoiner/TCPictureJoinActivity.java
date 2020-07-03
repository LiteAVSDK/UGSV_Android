package com.tencent.qcloud.xiaoshipin.videojoiner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.picturetransition.IPictureJoinKit;
import com.tencent.qcloud.ugckit.UGCKitPictureJoin;
import com.tencent.qcloud.xiaoshipin.videoeditor.TCVideoEditerActivity;

import java.util.ArrayList;

/**
 * 图片生成视频类，用于设置图片转场动画，并生成一个视频，返回生成的视频路径
 */
public class TCPictureJoinActivity extends FragmentActivity {

    private UGCKitPictureJoin mPictureTransition;
    private ArrayList<String> mPicPathList;
    private IPictureJoinKit.OnPictureJoinListener mOnPictureJoinListener = new IPictureJoinKit.OnPictureJoinListener() {

        @Override
        public void onPictureJoinCompleted(UGCKitResult ugcKitResult) {
            /**
             * 跳转到视频裁剪页面
             */
            if (ugcKitResult.errorCode == 0) {
                startEditActivity(ugcKitResult);
            } else {
                ToastUtil.toastShortMessage("join picture failed. error code:" + ugcKitResult.errorCode + ",desc msg:" + ugcKitResult.descMsg);
            }
        }

        @Override
        public void onPictureJoinCanceled() {
            /**
             * 生成视频操作取消
             */
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowParam();
        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.PictureTransitionActivityStyle);
        setContentView(R.layout.picture_join_layout);
        mPictureTransition = (UGCKitPictureJoin) findViewById(R.id.picture_transition);
        /**
         *  获取从图片路径集合，并设置给TUIKit {@link UGCKitPictureJoin#setInputPictureList(ArrayList)}
         */
        mPicPathList = getIntent().getStringArrayListExtra(UGCKitConstants.INTENT_KEY_MULTI_PIC_LIST);
        mPictureTransition.setInputPictureList(mPicPathList);
        mPictureTransition.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPictureTransition.stopPlay();
                finish();
            }
        });
    }

    private void initWindowParam() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void startEditActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        mPictureTransition.stopPlay();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPictureTransition.setOnPictureJoinListener(mOnPictureJoinListener);
        mPictureTransition.resumePlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPictureTransition.pausePlay();
        mPictureTransition.setOnPictureJoinListener(null);
    }

}
