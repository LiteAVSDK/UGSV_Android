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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.common.ShortVideoDialog;
import com.tencent.qcloud.xiaoshipin.config.TCConfigManager;
import com.tencent.qcloud.xiaoshipin.login.TCLoginActivity;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCUGCListFragment;
import com.tencent.qcloud.ugckit.module.upload.TCUserMgr;
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

        if (checkPermission()) {
            return;
        }

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
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        switch (lang) {
            case TCConfigManager.SystemConfig.Languages.FOLLOW_SYSTEM:
                configuration.locale = Locale.getDefault();
                break;
            case TCConfigManager.SystemConfig.Languages.SIMPLIFIED_CHINESE:
                configuration.locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case TCConfigManager.SystemConfig.Languages.TRADITIONAL_CHINESE:
                configuration.locale = Locale.TRADITIONAL_CHINESE;
                break;
            case TCConfigManager.SystemConfig.Languages.ENGLISH:
                configuration.locale = Locale.ENGLISH;
                break;
            default:
                configuration.locale = Locale.getDefault();
                break;
        }
        resources.updateConfiguration(configuration, displayMetrics);
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return true;
                }
            }
        }
        return false;
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

}
