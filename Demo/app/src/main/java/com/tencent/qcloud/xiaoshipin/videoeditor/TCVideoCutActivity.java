package com.tencent.qcloud.xiaoshipin.videoeditor;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.text.TextUtils;
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
import com.tencent.qcloud.xiaoshipin.common.URIConvert;

/**
 * 裁剪视频Activity
 */
public class TCVideoCutActivity extends FragmentActivity {
    private String TAG = "TCVideoCutActivity";
    private UGCKitVideoCut mUGCKitVideoCut;

    private IVideoCutKit.OnCutListener mOnCutListener = new IVideoCutKit.OnCutListener() {
        /**
         * 视频裁剪进度条执行完成后调用
         */
        @Override
        public void onCutterCompleted(UGCKitResult ugcKitResult) {
            Log.i(TAG, "onCutterCompleted");
            if (ugcKitResult.errorCode == 0) {
                startEditActivity(ugcKitResult);
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
        String inVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
        String inVideoUri = getIntent().getStringExtra(UGCKitConstants.VIDEO_URI);

        String path;
        //由于Android 10上在使用ContentResolver.openFileDescriptor的时候会卡主，系统问题（https://issuetracker.google.com/issues/141496793）
        //所以修改为在Android 10以及之后通过uri转为绝对路径，并且在manifest文件中配置 android:requestLegacyExternalStorage="true"
        if (Build.VERSION.SDK_INT >= 29) {
            path = TextUtils.isEmpty(inVideoUri) ? inVideoPath : URIConvert.getFilePathByUri(inVideoUri);
        } else {
            path = inVideoPath;
        }
        mUGCKitVideoCut.setVideoPath(path);
        mUGCKitVideoCut.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        mUGCKitVideoCut.onBackPressed();
        super.onBackPressed();
    }

    private void startEditActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        startActivity(intent);
        finish();
    }

}
