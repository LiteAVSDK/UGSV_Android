package com.tencent.qcloud.xiaoshipin.videorecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.UGCKitVideoMixRecord;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.xiaoshipin.manager.PermissionManager;
import com.tencent.qcloud.ugckit.module.mixrecord.IVideoMixRecordKit;
import com.tencent.qcloud.ugckit.module.mixrecord.MixRecordActionData;
import com.tencent.qcloud.ugckit.module.mixrecord.MixRecordConfigBuildInfo;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.videochoose.TCTripleRecordVideoPickerActivity;
import com.tencent.qcloud.xiaoshipin.videoeditor.TCVideoEditerActivity;
import com.tencent.ugc.TXRecordCommon;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.PERMISSION_STORAGE;
import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.REQUEST_CODE_AUDIO;
import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.REQUEST_CODE_CAMERA;
import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.REQUEST_CODE_STORAGE;

public class TCVideoTripleScreenActivity
        extends FragmentActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        PermissionManager.OnCameraPermissionGrantedListener,
        PermissionManager.OnAudioPermissionGrantedListener,
        PermissionManager.OnStoragePermissionGrantedListener {

    private static final String TAG = "TCVideoTripleScreenActivity";
    public static int REQUEST_CODE = 100;
    private UGCKitVideoMixRecord mUGCKitVideoMixRecord;
    // 三屏视频合唱跟拍的视频路径
    private String mFollowShotVideoPath;

    private PermissionManager mAudioPermissionManager;

    private PermissionManager mCameraPermissionManager;

    private PermissionManager mStoragePermissionManager;


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
        MixRecordConfigBuildInfo buildInfo = new MixRecordConfigBuildInfo(paths,
                1, 1080, 1920, TXRecordCommon.VIDEO_ASPECT_RATIO_16_9);
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
                startEditActivity(ugcKitResult);
            }

            @Override
            public void onMixRecordAction(IVideoMixRecordKit.MixRecordActionT actionT, Object object) {
                if (actionT == IVideoMixRecordKit.MixRecordActionT.MIX_RECORD_ACTION_T_SELECT) {
                    MixRecordActionData data = (MixRecordActionData) object;
                    Intent intent = new Intent(TCVideoTripleScreenActivity.this, TCTripleRecordVideoPickerActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
        mAudioPermissionManager = new PermissionManager(this, PermissionManager.PermissionType.AUDIO);
        mCameraPermissionManager = new PermissionManager(this, PermissionManager.PermissionType.CAMERA);
        mStoragePermissionManager = new PermissionManager(this, PermissionManager.PermissionType.STORAGE);

        mCameraPermissionManager.setOnCameraPermissionGrantedListener(this);
        mAudioPermissionManager.setOnAudioPermissionGrantedListener(this);
        mStoragePermissionManager.setOnStoragePermissionGrantedListener(this);
        mCameraPermissionManager.checkoutIfShowPermissionIntroductionDialog();
    }

    private void startEditActivity(UGCKitResult ugcKitResult) {
        // 更新一下VideoInfo的时间
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUGCKitVideoMixRecord.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_CODE && data != null) {
            String path = data.getStringExtra("file");
//            int index = data.getIntExtra("index",-1);
//            TXCLog.d(TAG,"path="+path+" "+index);
            mUGCKitVideoMixRecord.updateMixFile(-1, path);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.CAMERA)) {
                mUGCKitVideoMixRecord.start();
            }
        } else {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults != null) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                mCameraPermissionManager.onRequestPermissionsResult(requestCode, grantResults);
            } else if (requestCode == REQUEST_CODE_AUDIO) {
                mAudioPermissionManager.onRequestPermissionsResult(requestCode,grantResults);
            } else if (requestCode == REQUEST_CODE_STORAGE) {
                mStoragePermissionManager.onRequestPermissionsResult(requestCode,grantResults);
            }
        }
    }

    @Override
    public void onCameraPermissionGranted() {
        mUGCKitVideoMixRecord.start();
        mAudioPermissionManager.checkoutIfShowPermissionIntroductionDialog();
    }

    @Override
    public void onAudioPermissionGranted() {
        mStoragePermissionManager.checkoutIfShowPermissionIntroductionDialog();
    }

    @Override
    public void onStoragePermissionGranted() {

    }
}
