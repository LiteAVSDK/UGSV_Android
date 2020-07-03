package com.tencent.qcloud.xiaoshipin.mainui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tencent.qcloud.xiaoshipin.R;

public class TCSettingActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        findViewById(R.id.layout_language).setOnClickListener(this);
        findViewById(R.id.imgBtn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_language: {
                Intent intent = new Intent(this, TCLanguagaSettingActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.imgBtn_back: {
                finish();
            }
            default:
                break;
        }
    }
}
