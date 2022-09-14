package com.tencent.qcloud.ugckit.component.dialog;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtil {

    private Context        mContext;
    private ProgressDialog mProgressDialog;

    public ProgressDialogUtil(Context context) {
        mContext = context;
    }

    public void showProgressDialog() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public void setProgressDialogMessage(String message) {
        if (mProgressDialog != null) {
            mProgressDialog.setMessage(message);
        }
    }
}
