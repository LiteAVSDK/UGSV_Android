package com.tencent.qcloud.xiaoshipin.userinfo;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.ImageView;

import com.tencent.qcloud.xiaoshipin.R;

public class TCAboutIntroductionActivity extends Activity {
    private ImageView mIVBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_introduction);
        initView();
    }

    private void initView() {
        mIVBack = (ImageView) findViewById(R.id.iv_about_back);
        mIVBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
