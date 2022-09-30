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

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.xiaoshipin.manager.PermissionManager;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.effect.bgm.TCMusicActivity;
import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.qcloud.ugckit.module.record.interfaces.IVideoRecordKit;
import com.tencent.qcloud.ugckit.module.record.MusicInfo;
import com.tencent.qcloud.ugckit.UGCKitVideoRecord;
import com.tencent.qcloud.xiaoshipin.videoeditor.TCVideoEditerActivity;

import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.PERMISSION_STORAGE;
import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.REQUEST_CODE_AUDIO;
import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.REQUEST_CODE_CAMERA;
import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.REQUEST_CODE_STORAGE;

/**
 * 小视频录制界面
 */
public class TCVideoRecordActivity extends FragmentActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        PermissionManager.OnCameraPermissionGrantedListener,
        PermissionManager.OnAudioPermissionGrantedListener,
        PermissionManager.OnStoragePermissionGrantedListener {

    private UGCKitVideoRecord mUGCKitVideoRecord;

    private PermissionManager mAudioPermissionManager;

    private PermissionManager mCameraPermissionManager;

    private PermissionManager mStoragePermissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWindowParam();

        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.RecordActivityTheme);

        setContentView(R.layout.activity_video_record);

        mUGCKitVideoRecord = (UGCKitVideoRecord) findViewById(R.id.video_record_layout);


        UGCKitRecordConfig ugcKitRecordConfig = UGCKitRecordConfig.getInstance();
        mUGCKitVideoRecord.setConfig(ugcKitRecordConfig);

        mUGCKitVideoRecord.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mUGCKitVideoRecord.setOnRecordListener(new IVideoRecordKit.OnRecordListener() {
            @Override
            public void onRecordCanceled() {
                finish();
            }

            @Override
            public void onRecordCompleted(UGCKitResult ugcKitResult) {
                if (ugcKitResult.errorCode == 0) {
                    startEditActivity(ugcKitResult);
                } else {
                    ToastUtil.toastShortMessage("record video failed. error code:" + ugcKitResult.errorCode + ",desc msg:" + ugcKitResult.descMsg);
                }
            }
        });
        mUGCKitVideoRecord.setOnMusicChooseListener(new IVideoRecordKit.OnMusicChooseListener() {
            @Override
            public void onChooseMusic(int position) {
                Intent bgmIntent = new Intent(TCVideoRecordActivity.this, TCMusicActivity.class);
                bgmIntent.putExtra(UGCKitConstants.MUSIC_POSITION, position);
                startActivityForResult(bgmIntent, UGCKitConstants.ACTIVITY_MUSIC_REQUEST_CODE);
            }
        });
        mAudioPermissionManager = new PermissionManager(this, PermissionManager.PermissionType.AUDIO);
        mCameraPermissionManager = new PermissionManager(this, PermissionManager.PermissionType.CAMERA);
        mStoragePermissionManager = new PermissionManager(this, PermissionManager.PermissionType.STORAGE);

        mCameraPermissionManager.setOnCameraPermissionGrantedListener(this);
        mAudioPermissionManager.setOnAudioPermissionGrantedListener(this);
        mCameraPermissionManager.checkoutIfShowPermissionIntroductionDialog();
        mStoragePermissionManager.setOnStoragePermissionGrantedListener(this);
    }


    private void initWindowParam() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void startEditActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.CAMERA)) {
                mUGCKitVideoRecord.start();
            }
        } else {
            mUGCKitVideoRecord.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUGCKitVideoRecord.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoRecord.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUGCKitVideoRecord.screenOrientationChange();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUGCKitVideoRecord.onActivityResult(requestCode, resultCode, data);
        if (requestCode != UGCKitConstants.ACTIVITY_MUSIC_REQUEST_CODE) {
            return;
        }
        if (data == null) {
            return;
        }
        MusicInfo musicInfo = new MusicInfo();

        musicInfo.path = data.getStringExtra(UGCKitConstants.MUSIC_PATH);
        musicInfo.name = data.getStringExtra(UGCKitConstants.MUSIC_NAME);
        musicInfo.position = data.getIntExtra(UGCKitConstants.MUSIC_POSITION, -1);

        mUGCKitVideoRecord.setRecordMusicInfo(musicInfo);
    }

    @Override
    public void onBackPressed() {
        mUGCKitVideoRecord.backPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults != null) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                mCameraPermissionManager.onRequestPermissionsResult(requestCode, grantResults);
            } else if (requestCode == REQUEST_CODE_AUDIO) {
                mAudioPermissionManager.onRequestPermissionsResult(requestCode, grantResults);
            } else if (requestCode == REQUEST_CODE_STORAGE) {
                mStoragePermissionManager.onRequestPermissionsResult(requestCode,grantResults);
            }
        }
    }

    @Override
    public void onCameraPermissionGranted() {
        mUGCKitVideoRecord.start();
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