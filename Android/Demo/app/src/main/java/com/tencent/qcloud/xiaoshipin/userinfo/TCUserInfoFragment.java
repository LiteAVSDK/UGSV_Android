package com.tencent.qcloud.xiaoshipin.userinfo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.login.TCLoginActivity;
import com.tencent.qcloud.xiaoshipin.mainui.TCSettingActivity;
import com.tencent.qcloud.ugckit.module.upload.TCUserMgr;
import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.BitmapUtils;

import org.json.JSONObject;

/**
 * 用户资料展示页面
 */
public class TCUserInfoFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "TCUserInfoFragment";
    private ImageView mHeadPic;
    private TextView mNickName;
    private TextView mUserId;
    private RelativeLayout mLayoutAbout;
    private LinearLayout mLayoutQuit;
    private RelativeLayout mLayoutUser;
    private RelativeLayout mLayoutTechnicalSupport;

    private RelativeLayout mLayoutSetting;
    private TextView mTvDownloadSDK;
    private TextView mTvProductIntroduction;

    public TCUserInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        mLayoutUser = (RelativeLayout) view.findViewById(R.id.rl_user_info);
        mHeadPic = (ImageView) view.findViewById(R.id.iv_ui_head);
        mNickName = (TextView) view.findViewById(R.id.tv_ui_nickname);
        mUserId = (TextView) view.findViewById(R.id.tv_ui_user_id);
        mLayoutAbout = (RelativeLayout) view.findViewById(R.id.layout_about);
        mLayoutQuit = (LinearLayout) view.findViewById(R.id.layout_quit);

        mLayoutSetting = (RelativeLayout) view.findViewById(R.id.layout_setting);
        mLayoutTechnicalSupport = (RelativeLayout) view.findViewById(R.id.layout_get_technical_support);
        mTvDownloadSDK = (TextView) view.findViewById(R.id.tv_download_sdk);
        mTvProductIntroduction = (TextView) view.findViewById(R.id.tv_product_introduction);

        mTvDownloadSDK.setMovementMethod(LinkMovementMethod.getInstance());
        mTvProductIntroduction.setMovementMethod(LinkMovementMethod.getInstance());

        mLayoutUser.setOnClickListener(this);
        mLayoutAbout.setOnClickListener(this);
        mLayoutQuit.setOnClickListener(this);

        mLayoutSetting.setOnClickListener(this);
        mLayoutTechnicalSupport.setOnClickListener(this);
        showUnLoginView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //当前已经登录。 那么页面展示之前，更新一下用户信息
        TCUserMgr.getInstance().fetchUserInfo(new TCUserMgr.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String nickname = TCUserMgr.getInstance().getNickname();
                        if ("null".equals(nickname) || TextUtils.isEmpty(nickname)) {
                            mNickName.setVisibility(View.GONE);
                        } else {
                            mNickName.setText(nickname);
                        }
                        mUserId.setText("ID:" + TCUserMgr.getInstance().getUserId());
                        BitmapUtils.showPicWithUrl(getActivity(), mHeadPic, TCUserMgr.getInstance().getHeadPic(), R.drawable.face);
                        mLayoutQuit.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(int code, final String msg) {

            }
        });
    }

    /**
     * 未登录时的样式
     */
    public void showUnLoginView() {
        if (!TCUserMgr.getInstance().hasUser()) {
            mUserId.setText(getResources().getString(R.string.fragment_user_info_please_lonin));
            mHeadPic.setImageResource(R.drawable.face);
            mLayoutQuit.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_quit:  //注销APP
                TCUserMgr.getInstance().logout();
                showUnLoginView();
                break;
            case R.id.layout_about: {//显示 APP SDK 的版本信息
                Intent intent = new Intent(getContext(), TCAboutActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.rl_user_info:
                if (!TCUserMgr.getInstance().hasUser()) {
                    Intent intent1 = new Intent(getContext(), TCLoginActivity.class);
                    startActivity(intent1);
                }
                break;
            case R.id.layout_setting: {
                Intent intent = new Intent(getContext(), TCSettingActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.layout_get_technical_support:
                showTechnicalSupport();
                break;
            default:
                break;
        }
    }

    private void showTechnicalSupport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog alertDialog = builder.setCancelable(false).setMessage(R.string.follow_and_send_msg_to_public_number)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }
}
