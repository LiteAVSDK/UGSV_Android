package com.tencent.qcloud.xiaoshipin.videoeditor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.module.cut.IVideoCutKit;
import com.tencent.qcloud.ugckit.UGCKitVideoCut;

/**
 * 裁剪视频Activity
 */
public class TCVideoCutActivity extends FragmentActivity {
    private String TAG = "TCVideoCutActivity";
    private UGCKitVideoCut mUGCKitVideoCut;
    private String mInVideoPath;
    private IVideoCutKit.OnCutListener mOnCutListener = new IVideoCutKit.OnCutListener() {
        /**
         * 视频裁剪进度条执行完成后调用
         */
        @Override
        public void onCutterCompleted(UGCKitResult ugcKitResult) {
            Log.i(TAG, "onCutterCompleted");
            if (ugcKitResult.errorCode == 0) {
                startEditActivity();
            } else {
                ToastUtil.toastShortMessage("cut video failed. error code:" + ugcKitResult.errorCode + ",desc msg:" + ugcKitResult.descMsg);
            }
        }

        /**
         * 点击视频裁剪进度叉号，取消裁剪时被调用
         */
        @Override
        public void onCutterCanceled() {
            Log.i(TAG, "onCutterCanceled");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowParam();
        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.EditerActivityTheme);
        setContentView(R.layout.activity_video_cut);
        mUGCKitVideoCut = (UGCKitVideoCut) findViewById(R.id.video_cutter_layout);
        mInVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
        mUGCKitVideoCut.setVideoPath(mInVideoPath);
        mUGCKitVideoCut.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initWindowParam() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUGCKitVideoCut.setOnCutListener(mOnCutListener);
        mUGCKitVideoCut.startPlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUGCKitVideoCut.stopPlay();
        mUGCKitVideoCut.setOnCutListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoCut.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void startEditActivity() {
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        startActivity(intent);
        finish();
    }

}
