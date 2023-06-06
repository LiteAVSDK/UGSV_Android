package com.tencent.qcloud.xiaoshipin.mainui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.PermissionIntroductionDialog;
import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.utils.SharedPreferenceUtils;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.common.ShortVideoDialog;
import com.tencent.qcloud.xiaoshipin.config.TCConfigManager;
import com.tencent.qcloud.xiaoshipin.login.TCLoginActivity;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCUGCListFragment;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.ugckit.utils.FileUtils;
import com.tencent.qcloud.ugckit.module.record.draft.RecordDraftInfo;
import com.tencent.qcloud.ugckit.module.record.draft.RecordDraftManager;
import com.tencent.qcloud.ugckit.utils.NetworkUtil;
import com.tencent.qcloud.xiaoshipin.userinfo.TCUserInfoFragment;
import com.tencent.qcloud.xiaoshipin.videorecord.TCVideoRecordActivity;

import java.util.List;
import java.util.Locale;

/**
 * 主界面: 短视频列表，用户信息页
 */
public class TCMainActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "TCMainActivity";

    private Button mBtnVideo, mBtnSelect, mBtnUser;
    private Fragment mCurrentFragment;
    private Fragment mTCLiveListFragment, mTCUserInfoFragment;

    private long mLastClickPubTS = 0;

    private ShortVideoDialog mShortVideoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLanguage();

        setContentView(R.layout.activity_main);

        initView();

        showVideoFragment();

        checkLastRecordPart();
    }

    private void checkLastRecordPart() {
        final RecordDraftManager recordDraftManager = new RecordDraftManager(this);
        RecordDraftInfo lastDraftInfo = recordDraftManager.getLastDraftInfo();
        if (lastDraftInfo == null) {
            return;
        }
        final List<RecordDraftInfo.RecordPart> recordPartList = lastDraftInfo.getPartList();
        if (recordPartList != null && recordPartList.size() > 0) {
            TXCLog.i(TAG, "checkLastRecordPart, recordPartList");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog alertDialog = builder.setCancelable(false).setMessage(R.string.record_part_exist)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(TCMainActivity.this, TCVideoRecordActivity.class));
                        }
                    })
                    .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            recordDraftManager.deleteLastRecordDraft();
                            for (final RecordDraftInfo.RecordPart recordPart : recordPartList) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        FileUtils.deleteFile(recordPart.getPath());
                                    }
                                }).start();
                            }
                        }
                    }).create();
            alertDialog.show();
        }
    }

    private void setLanguage() {
        int lang = TCConfigManager.SystemConfig.getLanguage();

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        Resources appResources = UGCKit.getAppContext().getResources();
        Configuration appConfiguration = appResources.getConfiguration();

        switch (lang) {
            case TCConfigManager.SystemConfig.Languages.FOLLOW_SYSTEM:
                configuration.locale = getDefaultLocal();
                appConfiguration.locale = getDefaultLocal();
                break;
            case TCConfigManager.SystemConfig.Languages.SIMPLIFIED_CHINESE:
                configuration.locale = Locale.SIMPLIFIED_CHINESE;
                appConfiguration.locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case TCConfigManager.SystemConfig.Languages.TRADITIONAL_CHINESE:
                configuration.locale = Locale.TRADITIONAL_CHINESE;
                appConfiguration.locale = Locale.TRADITIONAL_CHINESE;
                break;
            case TCConfigManager.SystemConfig.Languages.ENGLISH:
                configuration.locale = Locale.ENGLISH;
                appConfiguration.locale = Locale.ENGLISH;
                break;
            default:
                configuration.locale = getDefaultLocal();
                appConfiguration.locale = getDefaultLocal();
                break;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        appResources.updateConfiguration(configuration, appResources.getDisplayMetrics());
    }

    private void initView() {
        mShortVideoDialog = new ShortVideoDialog();

        mBtnVideo = (Button) findViewById(R.id.btn_home_left);
        mBtnSelect = (Button) findViewById(R.id.btn_home_select);
        mBtnUser = (Button) findViewById(R.id.btn_home_right);

        mBtnUser.setOnClickListener(this);
        mBtnVideo.setOnClickListener(this);
        mBtnSelect.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (TextUtils.isEmpty(TCUserMgr.getInstance().getUserToken())) {
            if (NetworkUtil.isNetworkAvailable(this) && TCUserMgr.getInstance().hasUser()) {
                TCUserMgr.getInstance().autoLogin(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_home_left:
                showVideoFragment();
                break;
            case R.id.btn_home_select:
                showSelect();
                break;
            case R.id.btn_home_right:
                showUserFragment();
                break;
        }
    }

    private void showSelect() {
        if (!TCUserMgr.getInstance().hasUser()) {
            Intent intent = new Intent(TCMainActivity.this, TCLoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // 防止多次点击
            if (System.currentTimeMillis() - mLastClickPubTS > 1000) {
                mLastClickPubTS = System.currentTimeMillis();
                if (mShortVideoDialog.isAdded()) {
                    mShortVideoDialog.dismiss();
                } else {
                    mShortVideoDialog.show(getFragmentManager(), "");
                }
            }
        }
    }

    private void showUserFragment() {
        mBtnVideo.setBackgroundResource(R.drawable.ic_home_video_normal);
        mBtnUser.setBackgroundResource(R.drawable.ic_user_selected);
        if (mTCUserInfoFragment == null) {
            mTCUserInfoFragment = new TCUserInfoFragment();
        }
        showFragment(mTCUserInfoFragment, "user_fragment");
    }

    private void showVideoFragment() {
        mBtnVideo.setBackgroundResource(R.drawable.ic_home_video_selected);
        mBtnUser.setBackgroundResource(R.drawable.ic_user_normal);
        if (mTCLiveListFragment == null) {
            mTCLiveListFragment = new TCUGCListFragment();
        }
        showFragment(mTCLiveListFragment, "live_list_fragment");
    }

    private void showFragment(Fragment fragment, String tag) {
        if (fragment == mCurrentFragment) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        if (!fragment.isAdded()) {
            transaction.add(R.id.contentPanel, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        mCurrentFragment = fragment;
        transaction.commit();
    }


    private Locale getDefaultLocal() {
        return TCConfigManager.getSystemLocal();
    }
}
