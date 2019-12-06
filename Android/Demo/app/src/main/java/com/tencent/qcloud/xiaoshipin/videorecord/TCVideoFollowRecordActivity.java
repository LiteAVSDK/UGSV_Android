package com.tencent.qcloud.xiaoshipin.videorecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.ugckit.module.followRecord.FollowRecordInfo;
import com.tencent.qcloud.ugckit.module.followRecord.IVideoFollowRecordKit;
import com.tencent.qcloud.ugckit.UGCKitVideoFollowRecord;
import com.tencent.qcloud.xiaoshipin.videoeditor.TCVideoEditerActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 合唱
 */
public class TCVideoFollowRecordActivity extends FragmentActivity {

    private static final String TAG = "TCVideoFollowRecordActivity";
    // 视频合唱组件
    private UGCKitVideoFollowRecord mUGCKitVideoFollowRecord;
    // 视频合唱跟拍的视频路径
    private String mFollowShotVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWindowParam();

        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.FollowRecordActivityTheme);

        initData();
        setContentView(R.layout.activity_video_chorus);

        mUGCKitVideoFollowRecord = (UGCKitVideoFollowRecord) findViewById(R.id.video_chorus);

        FollowRecordInfo followRecordInfo = new FollowRecordInfo();
        followRecordInfo.playPath = mFollowShotVideoPath;

        mUGCKitVideoFollowRecord.setFollowRecordInfo(followRecordInfo);
        mUGCKitVideoFollowRecord.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mUGCKitVideoFollowRecord.setOnFollowRecordListener(new IVideoFollowRecordKit.OnFollowRecordListener() {
            @Override
            public void onFollowRecordCanceled() {
                finish();
            }

            @Override
            public void onFollowRecordCompleted(UGCKitResult ugcKitResult) {
                startEditActivity();
            }
        });
    }

    private void startEditActivity() {
        // 更新一下VideoInfo的时间
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        startActivity(intent);
        finish();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent == null) {
            TXCLog.e(TAG, "intent is null");
            return;
        }
        mFollowShotVideoPath = intent.getStringExtra(UGCKitConstants.VIDEO_PATH);
    }

    private void initWindowParam() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasPermission()) {
            mUGCKitVideoFollowRecord.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUGCKitVideoFollowRecord.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoFollowRecord.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUGCKitVideoFollowRecord.screenOrientationChange();
    }

    @Override
    public void onBackPressed() {
        mUGCKitVideoFollowRecord.backPressed();
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mUGCKitVideoFollowRecord.start();
        }
    }
}
