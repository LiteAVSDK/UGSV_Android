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
import com.tencent.qcloud.ugckit.UGCKitVideoMixRecord;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.mixrecord.IVideoMixRecordKit;
import com.tencent.qcloud.ugckit.module.mixrecord.MixRecordActionData;
import com.tencent.qcloud.ugckit.module.mixrecord.MixRecordConfigBuildInfo;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.videochoose.TCTripleRecordVideoPickerActivity;
import com.tencent.qcloud.xiaoshipin.videoeditor.TCVideoEditerActivity;
import com.tencent.ugc.TXRecordCommon;

import java.util.ArrayList;
import java.util.List;

public class TCVideoTripleScreenActivity extends FragmentActivity {

    private static final String TAG = "TCVideoTripleScreenActivity";
    public static int  REQUEST_CODE = 100;
    private UGCKitVideoMixRecord mUGCKitVideoMixRecord;
    // 三屏视频合唱跟拍的视频路径
    private String mFollowShotVideoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWindowParam();

        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.MixRecordActivityTheme);

        initData();
        setContentView(R.layout.activity_video_triple);
        mUGCKitVideoMixRecord = (UGCKitVideoMixRecord) findViewById(R.id.video_chorus);

        List<String> paths = new ArrayList<>();
        paths.add(mFollowShotVideoPath);
        paths.add(mFollowShotVideoPath);
        MixRecordConfigBuildInfo buildInfo = new MixRecordConfigBuildInfo(paths,1,1080,1920, TXRecordCommon.VIDEO_ASPECT_RATIO_16_9);
        mUGCKitVideoMixRecord.setMixRecordInfo(buildInfo);
        mUGCKitVideoMixRecord.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mUGCKitVideoMixRecord.setOnMixRecordListener(new IVideoMixRecordKit.OnMixRecordListener() {
            @Override
            public void onMixRecordCanceled() {
                finish();
            }

            @Override
            public void onMixRecordCompleted(UGCKitResult ugcKitResult) {
                startEditActivity();
            }

            @Override
            public void onMixRecordAction(IVideoMixRecordKit.MixRecordActionT actionT, Object object) {
                if(actionT== IVideoMixRecordKit.MixRecordActionT.MIX_RECORD_ACTION_T_SELECT){
                    MixRecordActionData data =(MixRecordActionData)object;
                    Intent intent = new Intent(TCVideoTripleScreenActivity.this, TCTripleRecordVideoPickerActivity.class);
                    startActivityForResult(intent,REQUEST_CODE);
                }
            }
        });
    }

    private void startEditActivity() {
        // 更新一下VideoInfo的时间
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE && data!=null){
            String path = data.getStringExtra("file");
//            int index = data.getIntExtra("index",-1);
//            TXCLog.d(TAG,"path="+path+" "+index);
            mUGCKitVideoMixRecord.updateMixFile(-1,path);
        }
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
            mUGCKitVideoMixRecord.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUGCKitVideoMixRecord.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoMixRecord.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUGCKitVideoMixRecord.screenOrientationChange();
    }

    @Override
    public void onBackPressed() {
        mUGCKitVideoMixRecord.backPressed();
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
            mUGCKitVideoMixRecord.start();
        }
    }
}
