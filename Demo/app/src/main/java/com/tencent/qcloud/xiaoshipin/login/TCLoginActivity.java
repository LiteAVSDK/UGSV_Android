package com.tencent.qcloud.xiaoshipin.login;

import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.SHARED_PREFERENCE_FILE_NAME_PERMISSION;
import static com.tencent.qcloud.xiaoshipin.manager.PermissionManager.SHARED_PREFERENCE_FIRST_START;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.NetworkUtil;
import com.tencent.qcloud.ugckit.utils.SharedPreferenceUtils;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.TCApplication;
import com.tencent.qcloud.xiaoshipin.mainui.TCMainActivity;
import com.tencent.qcloud.xiaoshipin.manager.LicenseManager;
import com.tencent.qcloud.xiaoshipin.userinfo.UserInfoUtil;
import com.tencent.ugc.TXUGCBase;

import org.json.JSONObject;

public class TCLoginActivity extends Activity {

    private static final String TAG = "TCLoginActivity";

    private ProgressBar progressBar;

    private EditText etPassword;

    private AutoCompleteTextView etLogin;

    private Button btnLogin;

    private TextInputLayout tilLogin, tilPassword;

    private TextView tvRegister;

    private TextView tvBack;

    private TextView mTvUserProtocol;

    private CheckBox mCbAgreeProtocol;

    private int[][] chineseScrollProtocol = {{96, 107}, {285, 296}, {424, 438}, {509, 520}, {599, 610}};

    private int[][] englishScrollProtocol = {{385, 419}, {1054, 1093}, {1536, 1584}, {1969, 2019}, {2166, 2206}};

    private int[][] chineseDownProtocol = {{4, 13}, {14, 25}, {26, 41}, {42, 56}, {58, 68}};

    private int[][] englishDownProtocol = {{23, 40}, {42, 61}, {63, 104}, {102, 135}, {146, 172}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = (AutoCompleteTextView) findViewById(R.id.et_login);


        etPassword = (EditText) findViewById(R.id.et_password);

        tvRegister = (TextView) findViewById(R.id.btn_register);

        btnLogin = (Button) findViewById(R.id.btn_login);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        tilLogin = (TextInputLayout) findViewById(R.id.til_login);

        tilPassword = (TextInputLayout) findViewById(R.id.til_password);

        tvBack = (TextView) findViewById(R.id.login_tv_back);

        mTvUserProtocol = (TextView) findViewById(R.id.tv_protocol);
        mCbAgreeProtocol = (CheckBox) findViewById(R.id.cb_protocol);

        findViewById(R.id.checkbox_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCbAgreeProtocol.setChecked(!mCbAgreeProtocol.isChecked());
            }
        });

        mCbAgreeProtocol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    btnLogin.setBackgroundResource(R.drawable.btn_login_pressed);
                    btnLogin.setTextColor(getResources().getColor(R.color.actionsheet_gray));
                } else {
                    btnLogin.setBackgroundResource(R.drawable.btn_login_normal);
                    btnLogin.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });

        if (isFirstStart()) {
            showStatementDialog();
        }

        btnLogin.setBackgroundResource(R.drawable.btn_login_pressed);
        btnLogin.setTextColor(getResources().getColor(R.color.actionsheet_gray));

        userNameLoginViewInit();

        updateStatement(this, mTvUserProtocol);

        //检测是否存在缓存
        checkLogin();
    }

    public boolean isFirstStart() {
        SharedPreferenceUtils preferences = new SharedPreferenceUtils(
                this, SHARED_PREFERENCE_FILE_NAME_PERMISSION);
        Boolean isFirst = (Boolean) preferences.getSharedPreference(SHARED_PREFERENCE_FIRST_START, true);
        if (isFirst) {
            // 第一次
            preferences.put(SHARED_PREFERENCE_FIRST_START, false);
            return true;
        } else {
            return false;
        }
    }

    private SpannableString showDownSpan() {
        final SpannableString builder = new SpannableString(getString(R.string.login_protocol_down));
        int[][] tempProtocolArray = null;
        if (isChina()) {
            tempProtocolArray = chineseDownProtocol;
        } else {
            tempProtocolArray = englishDownProtocol;
        }
        ClickableSpan privacyClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://web.sdk.qcloud.com/document/Tencent-UGSV-User-Agreement.html");
            }
        };
        builder.setSpan(privacyClickableSpan, tempProtocolArray[0][0], tempProtocolArray[0][1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan protocolPrivacyProtectionClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/document/preview/cd1aaba55e1548c7975ef10fbe9785f7");
            }
        };
        builder.setSpan(protocolPrivacyProtectionClickableSpan, tempProtocolArray[1][0],
                tempProtocolArray[1][1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        ClickableSpan protocolPersonalInformationClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/document/preview/ac0e6b4500c442839d632828a35083da");
            }
        };
        builder.setSpan(protocolPersonalInformationClickableSpan, tempProtocolArray[2][0], tempProtocolArray[2][1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan protocolThirdPartEndClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/document/preview/ac99514d96824473aff08e88dba7ee92");
            }
        };
        builder.setSpan(protocolThirdPartEndClickableSpan, tempProtocolArray[3][0], tempProtocolArray[3][1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan protocolChildrenPrivacyClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/privacy-children.htm");
            }
        };
        builder.setSpan(protocolChildrenPrivacyClickableSpan, tempProtocolArray[4][0], tempProtocolArray[4][1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private void showStatementDialog() {
        final Dialog mDialog = new Dialog(this, R.style.LoginShowTipTheme);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.login_show_tip_dialog_confirm, null);
        TextView tvDownProtocol = (TextView) view.findViewById(R.id.tv_all_protocol);
        tvDownProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableString builder = showDownSpan();
        tvDownProtocol.setText(builder);
        tvDownProtocol.setHighlightColor(Color.TRANSPARENT);
        TextView tvScrollProtocol = (TextView) view.findViewById(R.id.tv_message);
        tvScrollProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableStringBuilder scrollBuilder = showScrollSpan();
        tvScrollProtocol.setText(scrollBuilder);
        tvScrollProtocol.setHighlightColor(Color.TRANSPARENT);
        Button btnNeg = (Button) view.findViewById(R.id.btn_negative);
        Button btnPos = (Button) view.findViewById(R.id.btn_positive);
        btnNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
        btnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCbAgreeProtocol.setChecked(true);
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
        mDialog.setContentView(view);
        mDialog.show();
    }


    private boolean isChina() {
        return getResources().getConfiguration().locale.getLanguage().equals("zh");
    }




    private SpannableStringBuilder showScrollSpan() {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getString(R.string.login_protocol_scroll));
        int[][] tempProtocolArray = null;
        if (isChina()) {
            tempProtocolArray = chineseScrollProtocol;
        } else {
            tempProtocolArray = englishScrollProtocol;
        }
        ClickableSpan privacyClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/document/preview/ea00e5256ad442c483cd685d27b2e49f");
            }
        };
        builder.setSpan(privacyClickableSpan, tempProtocolArray[0][0], tempProtocolArray[0][1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan protocolPrivacyProtectionClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/document/preview/ac0e6b4500c442839d632828a35083da");
            }
        };
        builder.setSpan(protocolPrivacyProtectionClickableSpan, tempProtocolArray[1][0],
                tempProtocolArray[1][1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        ClickableSpan protocolPersonalInformationClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/document/preview/ac99514d96824473aff08e88dba7ee92");
            }
        };
        builder.setSpan(protocolPersonalInformationClickableSpan, tempProtocolArray[2][0], tempProtocolArray[2][1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan protocolThirdPartEndClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/document/preview/cd1aaba55e1548c7975ef10fbe9785f7");
            }
        };
        builder.setSpan(protocolThirdPartEndClickableSpan, tempProtocolArray[3][0], tempProtocolArray[3][1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan protocolChildrenPrivacyClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                setProtocolText(ds);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(TCLoginActivity.this, null, "https://privacy.qq.com/document/preview/cd1aaba55e1548c7975ef10fbe9785f7");
            }
        };
        builder.setSpan(protocolChildrenPrivacyClickableSpan, tempProtocolArray[4][0], tempProtocolArray[4][1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private void setProtocolText(TextPaint ds) {
        ds.setColor(getResources().getColor(R.color.login_color_blue));
        ds.setUnderlineText(false);
    }

    /**
     * 用户名密码登录界面init
     */
    public void userNameLoginViewInit() {
        etLogin.setInputType(EditorInfo.TYPE_CLASS_TEXT);

        etLogin.setText("");
        etLogin.setError(null, null);

        etPassword.setText("");
        etPassword.setError(null, null);

        tilLogin.setHint(getString(R.string.activity_login_username));

        tilPassword.setHint(getString(R.string.activity_login_password));

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //注册界面 phoneView 与 normalView跳转逻辑一致
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), TCRegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mCbAgreeProtocol.isChecked()) {
                    Toast.makeText(TCLoginActivity.this, getString(R.string.login_protocol_tip),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                //调用normal登录逻辑
                showOnLoading(true);

                attemptNormalLogin(etLogin.getText().toString(), etPassword.getText().toString());
            }
        });
    }

    /**
     * trigger loading模式
     *
     * @param active
     */
    private void showOnLoading(boolean active) {
        if (active) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);
            etLogin.setEnabled(false);
            etPassword.setEnabled(false);
            tvRegister.setClickable(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            etLogin.setEnabled(true);
            etPassword.setEnabled(true);
            tvRegister.setClickable(true);
            tvRegister.setTextColor(getResources().getColor(R.color.colorTransparentGray));
        }

    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.toastShortMessage(msg);
            }
        });
    }

    private void showOnLoadingInUIThread(final boolean active) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showOnLoading(active);
            }
        });
    }

    private void showLoginError(String errorString) {
        etLogin.setError(errorString);
        showOnLoading(false);
    }

    private void showPasswordError(String errorString) {
        etPassword.setError(errorString);
        showOnLoading(false);
    }

    /**
     * 登录成功后被调用，跳转至TCMainActivity
     */
    private void jumpToHomeActivity() {
        Intent intent = new Intent(this, TCMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void login(final String username, String password) {
        final TCUserMgr tcLoginMgr = TCUserMgr.getInstance();
        // ELK数据上报：启动次数
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_START_UP, 0, "");
        tcLoginMgr.login(username, password, new TCUserMgr.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                showToast(getResources().getString(R.string.tc_login_activity_login_successfully));
                LicenseManager.setLicense(TCLoginActivity.this);
                jumpToHomeActivity();
            }

            @Override
            public void onFailure(int code, final String msg) {
                showToast(msg);
                showOnLoadingInUIThread(false);
            }
        });
    }

    private void checkLogin() {
        if (NetworkUtil.isNetworkAvailable(this)) {
            //返回true表示存在本地缓存，进行登录操作，显示loadingFragment
            if (TCUserMgr.getInstance().hasUser()) {
                showOnLoadingInUIThread(true);
                TCUserMgr.getInstance().autoLogin(new TCUserMgr.Callback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        jumpToHomeActivity();
                    }

                    @Override
                    public void onFailure(int code, final String msg) {
                        showToast(getResources().getString(R.string.tc_login_activity_auto_login_failed));
                        showOnLoadingInUIThread(false);
                    }
                });
            }
        }
    }

    /**
     * 用户名密码登录
     *
     * @param username 用户名
     * @param password 密码
     */
    public void attemptNormalLogin(String username, String password) {
        if (UserInfoUtil.isUsernameVaild(username)) {
            if (UserInfoUtil.isPasswordValid(password)) {
                if (NetworkUtil.isNetworkAvailable(this)) {
                    login(username, password);
                } else {
                    ToastUtil.toastShortMessage(getResources().getString(R.string.tc_login_activity_no_network_connection));
                }
            } else {
                showPasswordError(getResources().getString(R.string.tc_login_activity_password_length_tip));
            }
        } else {
            showLoginError(getResources().getString(R.string.tc_login_activity_username_does_not_meet_specifications));
        }
    }

    public static void updateStatement(final Context context, TextView tVUserProtocol) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        String protocolStart = context.getString(R.string.login_protocol_start);
        String privacyProtocol = context.getString(R.string.login_privacy_protocol_detail);
        String userAgreement = context.getString(R.string.login_user_agreement_detail);
        String protocolAnd = context.getString(R.string.login_protocol_and);
        builder.append(protocolStart);
        builder.append(privacyProtocol);
        builder.append(protocolAnd);
        builder.append(userAgreement);

        int privacyStartIndex = protocolStart.length();
        int privacyEndIndex = privacyStartIndex + privacyProtocol.length();
        ClickableSpan privacyClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(context.getResources().getColor(R.color.login_color_blue));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(context, context.getString(R.string.login_privacy_protocol), "https://privacy.qq.com/document/preview/cd1aaba55e1548c7975ef10fbe9785f7");
            }
        };
        builder.setSpan(privacyClickableSpan, privacyStartIndex, privacyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int userAgreementStartIndex = privacyEndIndex + protocolAnd.length();
        int userAgreementEndIndex = userAgreementStartIndex + userAgreement.length();
        ClickableSpan userAgreementClickableSpan = new NoRefCopyClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(context.getResources().getColor(R.color.login_color_blue));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {
                jumpToWebViewActivity(context, context.getString(R.string.login_user_agreement), "https://web.sdk.qcloud.com/document/Tencent-UGSV-User-Agreement.html");
            }
        };
        builder.setSpan(userAgreementClickableSpan, userAgreementStartIndex, userAgreementEndIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tVUserProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        tVUserProtocol.setText(builder);
        tVUserProtocol.setHighlightColor(Color.TRANSPARENT);
    }

    public static void jumpToWebViewActivity(Context context, String title, String url) {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction("com.tencent.liteav.action.webview");
        intent.setPackage(context.getPackageName());
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }


    public static class NoRefCopyClickableSpan extends ClickableSpan implements NoCopySpan {

        @Override
        public void onClick(@NonNull View widget) {

        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
        }

    }
}
