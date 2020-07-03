package com.tencent.qcloud.xiaoshipin.videopublish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.module.upload.TCVideoPublishKit;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.UGCKitVideoPublish;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.mainui.TCMainActivity;

/**
 * 小视频发布页面
 */
public class TCVideoPublisherActivity extends Activity {
    private String mVideoPath = null;
    private String mCoverPath = null;
    private boolean mDisableCache;
    private UGCKitVideoPublish mUGCKitVideoPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowParam();
        setContentView(R.layout.activity_video_publisher);
        mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
        mCoverPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_COVERPATH);
        mDisableCache = getIntent().getBooleanExtra(UGCKitConstants.VIDEO_RECORD_NO_CACHE, false);
        mUGCKitVideoPublish = (UGCKitVideoPublish) findViewById(R.id.video_publish_layout);
        /**
         * 设置发布视频的路径和封面
         */
        mUGCKitVideoPublish.setPublishPath(mVideoPath, mCoverPath);
        /**
         * 设置是否开启本地缓存，若关闭本地缓存，则发布完成后删除"已发布"的视频和封面
         */
        mUGCKitVideoPublish.setCacheEnable(mDisableCache);
        /**
         * 设置发布视频的监听器
         */
        mUGCKitVideoPublish.setOnPublishListener(new TCVideoPublishKit.OnPublishListener() {

            @Override
            public void onPublishCompleted() {
                /**
                 * 发布完成，返回主界面
                 */
                backToMainActivity();
            }

            @Override
            public void onPublishCanceled() {
                /**
                 * 发布取消，退出发布页面
                 */
                finish();
            }
        });
    }

    private void initWindowParam() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 返回主界面
     */
    private void backToMainActivity() {
        Intent intent = new Intent(TCVideoPublisherActivity.this, TCMainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoPublish.release();
    }

}
