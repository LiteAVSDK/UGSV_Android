package com.tencent.qcloud.xiaoshipin.login;

import static com.tencent.qcloud.xiaoshipin.login.TCLoginActivity.updateStatement;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.NetworkUtil;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.mainui.TCMainActivity;
import com.tencent.qcloud.xiaoshipin.manager.LicenseManager;
import com.tencent.qcloud.xiaoshipin.userinfo.UserInfoUtil;
import com.tencent.ugc.TXUGCBase;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TCRegisterActivity extends Activity {

    public static final String TAG = TCRegisterActivity.class.getSimpleName();

    private String mPassword;

    //共用控件
    private RelativeLayout relativeLayout;

    private ProgressBar progressBar;

    private EditText etPassword;

    private EditText etPasswordVerify;

    private AutoCompleteTextView etRegister;

    private TextInputLayout tilRegister, tilPassword, tilPasswordVerify;

    private Button btnRegister;

    private TextView tvBackBtn;

    private TextView mTvUserProtocol;

    private CheckBox mCbAgreeProtocol;

    //动画
    AlphaAnimation fadeInAnimation, fadeOutAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        relativeLayout = (RelativeLayout) findViewById(R.id.rl_register_root);

        etRegister = (AutoCompleteTextView) findViewById(R.id.et_register);
        etPassword = (EditText) findViewById(R.id.et_password);
        etPasswordVerify = (EditText) findViewById(R.id.et_password_verify);
        tilPasswordVerify = (TextInputLayout) findViewById(R.id.til_password_verify);
        btnRegister = (Button) findViewById(R.id.btn_register);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        tilRegister = (TextInputLayout) findViewById(R.id.til_register);
        tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        tvBackBtn = (TextView) findViewById(R.id.tv_back);

        mTvUserProtocol = (TextView) findViewById(R.id.tv_protocol_register);
        mCbAgreeProtocol = (CheckBox) findViewById(R.id.cb_protocol_register);

        findViewById(R.id.checkbox_group_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCbAgreeProtocol.setChecked(!mCbAgreeProtocol.isChecked());
            }
        });

        btnRegister.setBackgroundResource(R.drawable.btn_login_pressed);

        mCbAgreeProtocol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    btnRegister.setBackgroundResource(R.drawable.btn_login_pressed);
                } else {
                    btnRegister.setBackgroundResource(R.drawable.btn_login_normal);
                }
            }
        });

        updateStatement(this, mTvUserProtocol);

        fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeInAnimation.setDuration(250);
        fadeOutAnimation.setDuration(250);

        LayoutTransition layoutTransition = new LayoutTransition();
        relativeLayout.setLayoutTransition(layoutTransition);
    }


    @Override
    protected void onResume() {
        super.onResume();
        userNameRegisterViewInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showOnLoading(boolean active) {
        if (active) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.INVISIBLE);
            etPassword.setEnabled(false);
            etPasswordVerify.setEnabled(false);
            etRegister.setEnabled(false);
            btnRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setVisibility(View.VISIBLE);
            etPassword.setEnabled(true);
            etPasswordVerify.setEnabled(true);
            etRegister.setEnabled(true);
            btnRegister.setEnabled(true);
        }

    }

    private void userNameRegisterViewInit() {
        etRegister.setText("");
        etRegister.setError(null, null);

        etRegister.setInputType(EditorInfo.TYPE_CLASS_TEXT);

        etPassword.setText("");
        etPassword.setError(null, null);

        etPasswordVerify.setText("");
        etPasswordVerify.setError(null, null);

        tilPasswordVerify.setVisibility(View.VISIBLE);

        tilRegister.setHint(getString(R.string.activity_register_username));

        tilPassword.setHint(getString(R.string.activity_register_password));

        tilPasswordVerify.setHint(getString(R.string.activity_register_password_verify));

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用normal注册逻辑
                attemptNormalRegist(etRegister.getText().toString(),
                        etPassword.getText().toString(), etPasswordVerify.getText().toString());
            }
        });

        tvBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showRegistError(String errorString) {
        etRegister.setError(errorString);
        showOnLoading(false);
    }

    private void showPasswordVerifyError(String errorString) {
        etPassword.setError(errorString);
        showOnLoading(false);
    }

    private void attemptNormalRegist(String username, String password, String passwordVerify) {

        if (!mCbAgreeProtocol.isChecked()) {
            Toast.makeText(TCRegisterActivity.this, getString(R.string.login_protocol_tip), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!UserInfoUtil.isUsernameVaild(username)) {
            showRegistError(getResources().getString(R.string.tc_register_activity_attempt_normal_regist_username_does_not_meet_specifications));
            return;
        }
        if (!UserInfoUtil.isPasswordValid(password)) {
            showPasswordVerifyError(getResources().getString(R.string.tc_register_activity_attempt_normal_regist_password_length_error));
            return;
        }
        if (!password.equals(passwordVerify)) {
            showPasswordVerifyError(getResources().getString(R.string.tc_register_activity_attempt_normal_regist_password_verify_error));
            return;
        }
        if (!NetworkUtil.isNetworkAvailable(this)) {
            ToastUtil.toastShortMessage(getResources().getString(R.string.tc_register_activity_attempt_normal_regist_no_network_connection));
            return;
        }

        mPassword = password;

        register(username, password);
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

    private void jumpToLoginActivity() {
        Intent intent = new Intent(this, TCLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void jumpToHomeActivity() {
        Intent intent = new Intent(this, TCMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void register(final String username, final String password) {
        final TCUserMgr tcLoginMgr = TCUserMgr.getInstance();
        tcLoginMgr.register(username, password, new TCUserMgr.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                showToast(getResources().getString(R.string.tc_register_activity_register_callback_register_successfully));
                tcLoginMgr.uploadLogs(LogReport.ELK_ACTION_REGISTER, username, TCUserMgr.SUCCESS_CODE, "注册成功", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "uploadLogs onFailure");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(TAG, "uploadLogs onResponse");
                    }
                });
                LicenseManager.setLicense(TCRegisterActivity.this);
                // ELK数据上报：启动次数
                LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_START_UP, 0, "");
                tcLoginMgr.login(username, password, new TCUserMgr.Callback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        showToast(getResources().getString(R.string.tc_register_activity_register_callback_auto_login_successfully));
                        jumpToHomeActivity();
                    }

                    @Override
                    public void onFailure(int code, final String msg) {
                        showToast(getResources().getString(R.string.tc_register_activity_register_callback_auto_login_failed));
                        showOnLoadingInUIThread(false);
                        jumpToLoginActivity();
                    }
                });
            }

            @Override
            public void onFailure(int code, final String msg) {
                String errorMsg = msg;
                if (code == 610) {
                    errorMsg = getResources().getString(R.string.tc_register_activity_register_callback_username_format_error);
                } else if (code == 611) {
                    errorMsg = getResources().getString(R.string.tc_register_activity_register_callback_password_format_error);
                } else if (code == 612) {
                    errorMsg = getResources().getString(R.string.tc_register_activity_register_callback_username_already_exists);
                }
                tcLoginMgr.uploadLogs(LogReport.ELK_ACTION_REGISTER, username, code, errorMsg, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "uploadLogs onFailure");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(TAG, "uploadLogs onResponse");
                    }
                });
                showToast(getResources().getString(R.string.tc_register_activity_register_callback_register_failed) + errorMsg);
                showOnLoadingInUIThread(false);
            }
        });
        showOnLoading(true);
    }
}
