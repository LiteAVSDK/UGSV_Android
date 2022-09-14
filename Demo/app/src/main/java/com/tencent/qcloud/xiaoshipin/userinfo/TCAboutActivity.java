package com.tencent.qcloud.xiaoshipin.userinfo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.rtmp.TXLiveBase;


public class TCAboutActivity extends AppCompatActivity {


    private TextView mTvDownloadSDK;
    private TextView mTvProductIntroduction;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView sdkVersion = findViewById(R.id.tv_sdk_value);
        TextView appVersion = findViewById(R.id.tv_app_value);
        sdkVersion.setText(TXLiveBase.getSDKVersionStr());
        appVersion.setText(getAppVersionName(this));
        mTvDownloadSDK = (TextView) findViewById(R.id.tv_download_sdk);
        mTvDownloadSDK.setMovementMethod(LinkMovementMethod.getInstance());
        mTvProductIntroduction = (TextView) findViewById(R.id.tv_product_introduction);
        mTvProductIntroduction.setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.layout_small_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TCAboutActivity.this, TCAboutIntroductionActivity.class));
            }
        });
        findViewById(R.id.layout_get_technical_support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTechnicalSupport();
            }
        });
        findViewById(R.id.layout_small_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TCAboutActivity.this, TCAboutIntroductionActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.iv_about_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showTechnicalSupport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = builder.setCancelable(false).setMessage(R.string.follow_and_send_msg_to_public_number)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    public static String getAppVersionName(Context context) {
        String versionName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            versionName = packageInfo.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.i("Exception",e.getMessage());
        }
        return versionName;
    }
}
