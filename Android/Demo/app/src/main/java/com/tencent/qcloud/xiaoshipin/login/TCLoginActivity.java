package com.tencent.qcloud.xiaoshipin.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.ugckit.module.upload.TCUserMgr;
import com.tencent.qcloud.xiaoshipin.userinfo.UserInfoUtil;
import com.tencent.qcloud.xiaoshipin.mainui.TCMainActivity;
import com.tencent.qcloud.ugckit.utils.NetworkUtil;
import com.tencent.qcloud.ugckit.utils.ToastUtil;

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

        userNameLoginViewInit();

        //检测是否存在缓存
        checkLogin();
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
        tcLoginMgr.login(username, password, new TCUserMgr.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                showToast(getResources().getString(R.string.tc_login_activity_login_successfully));
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

}
