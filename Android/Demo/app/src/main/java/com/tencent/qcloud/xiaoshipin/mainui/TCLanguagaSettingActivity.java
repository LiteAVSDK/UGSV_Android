package com.tencent.qcloud.xiaoshipin.mainui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.config.TCConfigManager;

public class TCLanguagaSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioButton mRdoBtnFollowSystem;
    private RadioButton mRdoBtnSimplifiedChinese;
    private RadioButton mRdoBtnTraditionalChinese;
    private RadioButton mRdoBtnEnglish;

    private ImageButton mImgBtnBack;

    private TextView mTvSave;

    private int mLang = -1;

    //通过此变量屏蔽RadioButton的setChecked方法对OnCheckedChangeListener的影响
    private boolean mIgnoreChange = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_setting);

        mLang = TCConfigManager.SystemConfig.getLanguage();
        initView();
    }

    private void unCheckedAllLangs() {
        mIgnoreChange = true;
        mRdoBtnFollowSystem.setChecked(false);
        mRdoBtnSimplifiedChinese.setChecked(false);
        mRdoBtnTraditionalChinese.setChecked(false);
        mRdoBtnEnglish.setChecked(false);
        mIgnoreChange = false;
    }

    private void initView() {

        mImgBtnBack = (ImageButton) findViewById(R.id.imgBtn_back);
        mImgBtnBack.setOnClickListener(this);

        mTvSave = (TextView)findViewById(R.id.tv_save);
        mTvSave.setOnClickListener(this);

        mRdoBtnFollowSystem = (RadioButton) findViewById(R.id.rdoBtn_follow_system);
        mRdoBtnSimplifiedChinese = (RadioButton) findViewById(R.id.rdoBtn_simplified_chinese);
        mRdoBtnTraditionalChinese = (RadioButton) findViewById(R.id.rdoBtn_traditional_chinese);
        mRdoBtnEnglish = (RadioButton) findViewById(R.id.rdoBtn_english);

        mRdoBtnFollowSystem.setTag(TCConfigManager.SystemConfig.Languages.FOLLOW_SYSTEM);
        mRdoBtnSimplifiedChinese.setTag(TCConfigManager.SystemConfig.Languages.SIMPLIFIED_CHINESE);
        mRdoBtnTraditionalChinese.setTag(TCConfigManager.SystemConfig.Languages.TRADITIONAL_CHINESE);
        mRdoBtnEnglish.setTag(TCConfigManager.SystemConfig.Languages.ENGLISH);

        mRdoBtnFollowSystem.setOnCheckedChangeListener(mLangsRadioBtnsOnCheckedChangeListener);
        mRdoBtnSimplifiedChinese.setOnCheckedChangeListener(mLangsRadioBtnsOnCheckedChangeListener);
        mRdoBtnTraditionalChinese.setOnCheckedChangeListener(mLangsRadioBtnsOnCheckedChangeListener);
        mRdoBtnEnglish.setOnCheckedChangeListener(mLangsRadioBtnsOnCheckedChangeListener);

        setCheckedRadio();
    }

    private void setCheckedRadio() {
        unCheckedAllLangs();

        switch (mLang) {
            case TCConfigManager.SystemConfig.Languages.FOLLOW_SYSTEM:
                mIgnoreChange = true;
                mRdoBtnFollowSystem.setChecked(true);
                mIgnoreChange = false;
                break;
            case TCConfigManager.SystemConfig.Languages.SIMPLIFIED_CHINESE:
                mIgnoreChange = true;
                mRdoBtnSimplifiedChinese.setChecked(true);
                mIgnoreChange = false;
                break;
            case TCConfigManager.SystemConfig.Languages.TRADITIONAL_CHINESE:
                mIgnoreChange = true;
                mRdoBtnTraditionalChinese.setChecked(true);
                mIgnoreChange = false;
                break;
            case TCConfigManager.SystemConfig.Languages.ENGLISH:
                mIgnoreChange = true;
                mRdoBtnEnglish.setChecked(true);
                mIgnoreChange = false;
                break;
            default:
                break;
        }
    }

    private CompoundButton.OnCheckedChangeListener mLangsRadioBtnsOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mIgnoreChange) {
                return;
            }

            if (isChecked) {
                mLang = (int) buttonView.getTag();
                setCheckedRadio();
            }

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_save: {
                TCConfigManager.SystemConfig.setLanguage(mLang);
                Intent intent = new Intent(this, TCMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            }
            case R.id.imgBtn_back: {
                finish();
                break;
            }
            default:
                break;
        }
    }

}