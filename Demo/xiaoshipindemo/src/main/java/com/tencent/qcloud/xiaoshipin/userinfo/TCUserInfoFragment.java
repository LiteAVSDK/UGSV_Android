package com.tencent.qcloud.xiaoshipin.userinfo;

import static com.tencent.qcloud.xiaoshipin.login.TCLoginActivity.jumpToWebViewActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.BitmapUtils;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.login.TCLoginActivity;
import com.tencent.qcloud.xiaoshipin.mainui.TCSettingActivity;

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
    private ImageView mEditName;
    private RelativeLayout mLayoutPrivacy;
    private RelativeLayout mLayoutUserAgreement;

    private RelativeLayout mLayoutSetting;

    private RelativeLayout mLayoutShare;

    private RelativeLayout mLayoutCollection;

    public TCUserInfoFragment() {}

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        mLayoutUser = (RelativeLayout) view.findViewById(R.id.rl_user_info);
        mHeadPic = (ImageView) view.findViewById(R.id.iv_ui_head);
        mNickName = (TextView) view.findViewById(R.id.tv_ui_nickname);
        mUserId = (TextView) view.findViewById(R.id.tv_ui_user_id);
        mLayoutAbout = (RelativeLayout) view.findViewById(R.id.layout_about);
        mLayoutQuit = (LinearLayout) view.findViewById(R.id.layout_quit);
        mLayoutPrivacy = (RelativeLayout) view.findViewById(R.id.layout_privacy);
        mLayoutUserAgreement = (RelativeLayout) view.findViewById(R.id.layout_user_agreement);
        mLayoutShare = (RelativeLayout) view.findViewById(R.id.layout_share);
        mLayoutCollection = (RelativeLayout) view.findViewById(R.id.layout_collection);

        mLayoutSetting = (RelativeLayout) view.findViewById(R.id.layout_setting);
        mEditName = (ImageView) view.findViewById(R.id.iv_ui_nickName);

        mLayoutShare.setOnClickListener(this);
        mLayoutCollection.setOnClickListener(this);
        mLayoutUserAgreement.setOnClickListener(this);
        mLayoutPrivacy.setOnClickListener(this);
        mLayoutUser.setOnClickListener(this);
        mLayoutAbout.setOnClickListener(this);
        mLayoutQuit.setOnClickListener(this);
        mEditName.setOnClickListener(this);
        mHeadPic.setOnClickListener(this);

        mLayoutSetting.setOnClickListener(this);
        mNickName.setText(TCUserMgr.getInstance().getNickname());
        mUserId.setText("ID:" + TCUserMgr.getInstance().getUserId());
        BitmapUtils.showPicWithUrl(
                getActivity(), mHeadPic, TCUserMgr.getInstance().getCoverPic(), R.drawable.face);
        mLayoutQuit.setVisibility(View.VISIBLE);
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
                        mNickName.setText(nickname);
                        String userId = TCUserMgr.getInstance().getUserId();
                        mUserId.setText("ID:" + userId);
                        BitmapUtils.showPicWithUrl(getActivity(), mHeadPic,
                                TCUserMgr.getInstance().getHeadPic(), R.drawable.face);
                        mLayoutQuit.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(int code, final String msg) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showUnLoginView();
                    }
                });
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
            mNickName.setVisibility(View.GONE);
            mLayoutQuit.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId(); //显示 APP SDK 的版本信息
        if (id == R.id.layout_quit) {
            TCUserMgr.getInstance().logout();
            Intent intentToLogin = new Intent(getContext(), TCLoginActivity.class);
            startActivity(intentToLogin);
            getActivity().finish();
        } else if (id == R.id.layout_about) {
            Intent intent = new Intent(getContext(), TCAboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.rl_user_info) {
            if (!TCUserMgr.getInstance().hasUser()) {
                Intent intent1 = new Intent(getContext(), TCLoginActivity.class);
                startActivity(intent1);
                getActivity().finish();
            }
        } else if (id == R.id.layout_setting) {
            Intent intent = new Intent(getContext(), TCSettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.iv_ui_nickName) {
            ModifyUserNameDialog modifyUserNameDialog =
                    new ModifyUserNameDialog(getActivity(), () -> {
                        final String userName = TCUserMgr.getInstance().getNickname();
                        getActivity().runOnUiThread(() -> {
                            mNickName.setVisibility(View.VISIBLE);
                            mNickName.setText(userName);
                        });
                    });
            modifyUserNameDialog.show();
        } else if (id == R.id.iv_ui_head) {
            ModifyUserAvatarDialog modifyUserAvatarDialog =
                    new ModifyUserAvatarDialog(getActivity(), () -> {
                        final String userAvatar = TCUserMgr.getInstance().getHeadPic();
                        getActivity().runOnUiThread(
                                ()
                                        -> ImageLoader.loadImage(getContext(), mHeadPic, userAvatar,
                                                R.drawable.app_bg_cover));
                    });
            modifyUserAvatarDialog.show();
        } else if (id == R.id.layout_user_agreement) {
            jumpToWebViewActivity(getContext(),
                    getContext().getString(R.string.login_user_agreement),
                    "https://web.sdk.qcloud.com/document/Tencent-UGSV-User-Agreement.html");
        } else if (id == R.id.layout_privacy) {
            jumpToWebViewActivity(getContext(),
                    getContext().getString(R.string.login_privacy_protocol),
                    "https://privacy.qq.com/document/preview/cd1aaba55e1548c7975ef10fbe9785f7");
        } else if (id == R.id.layout_share) {
            jumpToWebViewActivity(getContext(), getContext().getString(R.string.app_share),
                    "https://privacy.qq.com/document/preview/ac99514d96824473aff08e88dba7ee92");
        } else if (id == R.id.layout_collection) {
            jumpToWebViewActivity(getContext(), getContext().getString(R.string.app_collection),
                    "https://privacy.qq.com/document/preview/ac0e6b4500c442839d632828a35083da");
        }
    }
}
