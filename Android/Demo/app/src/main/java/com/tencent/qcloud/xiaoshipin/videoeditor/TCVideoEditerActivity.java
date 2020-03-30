package com.tencent.qcloud.xiaoshipin.videoeditor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.module.editer.UGCKitEditConfig;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.mainui.TCMainActivity;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.editer.IVideoEditKit;
import com.tencent.qcloud.ugckit.UGCKitVideoEdit;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.xiaoshipin.videopublish.TCVideoPublisherActivity;


public class TCVideoEditerActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "TCVideoEditerActivity";
    /**
     * 视频路径
     */
    private String mVideoPath;
    private UGCKitVideoEdit mUGCKitVideoEdit;
    // 背景音
    private TextView mTvBgm;
    // 动态滤镜
    private TextView mTvMotion;
    // 时间特效
    private TextView mTvSpeed;
    // 静态滤镜
    private TextView mTvFilter;
    // 贴纸
    private TextView mTvPaster;
    // 字幕
    private TextView mTvSubtitle;
    private IVideoEditKit.OnEditListener mOnVideoEditListener = new IVideoEditKit.OnEditListener() {
        @Override
        public void onEditCompleted(UGCKitResult ugcKitResult) {
            if (ugcKitResult.errorCode == 0) {
                startPreviewActivity(ugcKitResult);
            } else {
                ToastUtil.toastShortMessage("edit video failed. error code:" + ugcKitResult.errorCode + ",desc msg:" + ugcKitResult.descMsg);
            }
        }

        @Override
        public void onEditCanceled() {
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowParam();
        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.EditerActivityTheme);
        setContentView(R.layout.activity_video_editer);
        initData();
        mUGCKitVideoEdit = (UGCKitVideoEdit) findViewById(R.id.video_edit);

        UGCKitEditConfig config = new UGCKitEditConfig();
        config.isPublish = true;
        mUGCKitVideoEdit.setConfig(config);
        if (!TextUtils.isEmpty(mVideoPath)) {
            mUGCKitVideoEdit.setVideoPath(mVideoPath);
        }
        // 初始化播放器
        mUGCKitVideoEdit.initPlayer();

        mTvBgm = (TextView) findViewById(R.id.tv_bgm);
        mTvMotion = (TextView) findViewById(R.id.tv_motion);
        mTvSpeed = (TextView) findViewById(R.id.tv_speed);
        mTvFilter = (TextView) findViewById(R.id.tv_filter);
        mTvPaster = (TextView) findViewById(R.id.tv_paster);
        mTvSubtitle = (TextView) findViewById(R.id.tv_subtitle);

        mTvBgm.setOnClickListener(this);
        mTvMotion.setOnClickListener(this);
        mTvSpeed.setOnClickListener(this);
        mTvFilter.setOnClickListener(this);
        mTvPaster.setOnClickListener(this);
        mTvSubtitle.setOnClickListener(this);
    }

    private void initWindowParam() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initData() {
        mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUGCKitVideoEdit.initPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUGCKitVideoEdit.setOnVideoEditListener(mOnVideoEditListener);
        mUGCKitVideoEdit.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUGCKitVideoEdit.stop();
        mUGCKitVideoEdit.setOnVideoEditListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoEdit.release();
    }

    private void startPreviewActivity(UGCKitResult ugcKitResult) {
        if (TextUtils.isEmpty(ugcKitResult.outputPath)) {
            return;
        }
        long duration = VideoEditerSDK.getInstance().getVideoDuration();
        if (ugcKitResult.isPublish) {
            Intent intent = new Intent(getApplicationContext(), TCVideoPublisherActivity.class);
            intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
            if (!TextUtils.isEmpty(ugcKitResult.coverPath)) {
                intent.putExtra(UGCKitConstants.VIDEO_COVERPATH, ugcKitResult.coverPath);
            }
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_DURATION, duration);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, TCMainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        mUGCKitVideoEdit.backPressed();
    }

    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();
        if (id == R.id.tv_bgm) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_BGM);
        } else if (id == R.id.tv_motion) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_MOTION);
        } else if (id == R.id.tv_speed) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_SPEED);
        } else if (id == R.id.tv_filter) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_FILTER);
        } else if (id == R.id.tv_paster) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_PASTER);
        } else if (id == R.id.tv_subtitle) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_SUBTITLE);
        }
    }

    /**
     * 跳转到视频特效编辑界面
     *
     * @param effectType {@link UGCKitConstants#TYPE_EDITER_BGM} 添加背景音</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_MOTION} 添加动态滤镜</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_SPEED} 添加时间特效</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_FILTER} 添加静态滤镜</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_PASTER} 添加贴纸</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_SUBTITLE} 添加字幕</p>
     */
    private void startEffectActivity(int effectType) {
        Intent intent = new Intent(this, TCVideoEffectActivity.class);
        intent.putExtra(UGCKitConstants.KEY_FRAGMENT, effectType);
        startActivityForResult(intent, UGCKitConstants.ACTIVITY_OTHER_REQUEST_CODE);
    }
}
