package com.tencent.qcloud.xiaoshipin.userinfo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.xiaoshipin.R;

import org.json.JSONObject;

public class ModifyUserNameDialog extends BottomSheetDialog {
    private EditText mEditUserName;
    private TextView mTextInputTips;
    private Context mContext;
    private ModifySuccessListener mListener;


    public ModifyUserNameDialog(Context context, ModifySuccessListener listener) {
        super(context, R.style.AppDialogTheme);
        if (TCUserMgr.getInstance().getUserId() == null) {
            dismiss();
            return;
        }
        mContext = context;
        mListener = listener;
        setContentView(R.layout.app_view_modify_nickname_dialog);
        mTextInputTips = (TextView) findViewById(R.id.tips);
        mEditUserName = (EditText) findViewById(R.id.et_nickname);
        mEditUserName.setText(TCUserMgr.getInstance().getNickname());
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProfile();
            }
        });
    }

    private void setProfile() {
        String userName = mEditUserName.getText().toString().trim();
        if (TextUtils.isEmpty(userName) || TCUserMgr.getInstance().getUserId() == null) {
            ToastUtils.showLong(mContext.getString(R.string.app_toast_set_username));
            return;
        }
        String reg = "^[a-z0-9A-Z\\u4e00-\\u9fa5\\_]{2,20}$";
        if (!userName.matches(reg)) {
            mTextInputTips.setTextColor(mContext.getResources().getColor(R.color.app_color_input_no_match));
            return;
        }
        mTextInputTips.setTextColor(mContext.getResources().getColor(R.color.app_color_input_normal));
        TCUserMgr.getInstance().setNickName(userName, new TCUserMgr.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                ToastUtils.showLong(mContext.getString(R.string.app_toast_success_to_set_username));
                mListener.onSuccess();
                dismiss();
            }

            @Override
            public void onFailure(int code, String msg) {
                ToastUtils.showLong(mContext.getString(R.string.app_toast_failed_to_set_username, msg));
            }
        });
    }

    public interface ModifySuccessListener {
        void onSuccess();
    }
}