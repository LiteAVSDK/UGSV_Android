package com.tencent.qcloud.xiaoshipin.videoeditor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.module.effect.IVideoEffectKit;
import com.tencent.qcloud.ugckit.UGCKitVideoEffect;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.xiaoshipin.R;


public class TCVideoEffectActivity extends FragmentActivity {
    private static final String TAG = "TCVideoEffectActivity";
    private int mFragmentType;
    private UGCKitVideoEffect mUGCKitVideoEffect;
    private IVideoEffectKit.OnVideoEffectListener mOnVideoEffectListener = new IVideoEffectKit.OnVideoEffectListener() {
        @Override
        public void onEffectApply() {
            finish();
        }

        @Override
        public void onEffectCancel() {
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowParam();
        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.EditerActivityTheme);
        setContentView(R.layout.activity_video_effect);
        mFragmentType = getIntent().getIntExtra(UGCKitConstants.KEY_FRAGMENT, 0);

        mUGCKitVideoEffect = (UGCKitVideoEffect) findViewById(R.id.video_effect_layout);
        mUGCKitVideoEffect.setEffectType(mFragmentType);
    }

    private void initWindowParam() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUGCKitVideoEffect.setOnVideoEffectListener(mOnVideoEffectListener);
        mUGCKitVideoEffect.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUGCKitVideoEffect.stop();
        mUGCKitVideoEffect.setOnVideoEffectListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoEffect.release();
    }


    @Override
    public void onBackPressed() {
        mUGCKitVideoEffect.backPressed();
    }

}
