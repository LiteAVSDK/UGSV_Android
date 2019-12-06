package com.tencent.qcloud.ugckit.component.dialogfragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.utils.ScreenUtils;
import com.tencent.qcloud.ugckit.R;


public class TCConfirmDialog extends DialogFragment {
    private static final String KEY_TITLE = "key_title";
    private static final String KEY_MSG = "key_msg";
    private static final String KEY_CANCEL = "key_cancel";
    private static final String KEY_SURE_TXT = "key_sure_txt";
    private static final String KEY_CANCEL_TXT = "key_cancel_txt";

    private TextView mTvTitle, mTvContent, mTvSure, mTvCancel;


    @NonNull
    public static TCConfirmDialog newInstance(String title, String msg, boolean isHaveCancel, String sureTxt, String cancalTxt) {
        TCConfirmDialog dialog = new TCConfirmDialog();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_MSG, msg);
        bundle.putString(KEY_SURE_TXT, sureTxt);
        bundle.putString(KEY_CANCEL_TXT, cancalTxt);
        bundle.putBoolean(KEY_CANCEL, isHaveCancel);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null)
                window.setLayout((int) (ScreenUtils.getScreenWidth(dialog.getContext()) * 0.9),//设置宽度最小为 90%
                        WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * 去掉标题栏
     */
    private void setDialogStyle() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setDialogStyle();
        return inflater.inflate(R.layout.fragment_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString(KEY_TITLE);
        String msg = getArguments().getString(KEY_MSG, "");
        boolean isHaveCancel = getArguments().getBoolean(KEY_CANCEL, true);
        String cancelTxt = getArguments().getString(KEY_CANCEL_TXT);
        String sureTxt = getArguments().getString(KEY_SURE_TXT);

        mTvTitle = (TextView) view.findViewById(R.id.confirm_tv_title);
        mTvTitle.setText(title);

        mTvContent = (TextView) view.findViewById(R.id.confirm_et_content);
        mTvContent.setText(msg);
        mTvSure = (TextView) view.findViewById(R.id.confirm_tv_done);
        if (sureTxt != null) {
            mTvSure.setText(sureTxt);
        }
        mTvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mCallback != null)
                    mCallback.onSureCallback();
            }
        });

        mTvCancel = (TextView) view.findViewById(R.id.confirm_tv_cancel);
        if (!isHaveCancel) {
            mTvCancel.setVisibility(View.GONE);
        } else {
            mTvCancel.setVisibility(View.VISIBLE);
        }
        if (cancelTxt != null) {
            mTvCancel.setText(cancelTxt);
        }
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mCallback != null) {
                    mCallback.onCancelCallback();
                }
            }
        });
    }

    private OnConfirmCallback mCallback;

    public void setOnConfirmCallback(OnConfirmCallback callback) {
        mCallback = callback;
    }

    public interface OnConfirmCallback {
        void onSureCallback();

        void onCancelCallback();
    }
}
