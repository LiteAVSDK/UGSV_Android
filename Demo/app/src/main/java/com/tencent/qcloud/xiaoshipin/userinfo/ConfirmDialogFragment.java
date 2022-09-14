package com.tencent.qcloud.xiaoshipin.userinfo;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.qcloud.xiaoshipin.R;


public class ConfirmDialogFragment extends DialogFragment {
    private PositiveClickListener mPositiveClickListener;
    private NegativeClickListener mNegativeClickListener;

    private String mMessageText;
    private String mPositiveText;
    private String mNegativeText;
    private Button mButtonNegative;
    private Button mButtonPositive;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogFragment);
        dialog.setContentView(R.layout.app_dialog_confirm);
        mButtonPositive = (Button) dialog.findViewById(R.id.btn_positive);
        mButtonNegative = (Button) dialog.findViewById(R.id.btn_negative);
        dialog.setCancelable(false);
        initTextMessage(dialog);
        initButtonPositive(dialog);
        initButtonNegative(dialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置背景半透明
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getDialog().getWindow().setLayout(dm.widthPixels, getDialog().getWindow().getAttributes().height);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initTextMessage(Dialog dialog) {
        TextView textMessage = (TextView) dialog.findViewById(R.id.tv_message);
        textMessage.setText(mMessageText);
    }

    private void initButtonPositive(Dialog dialog) {
        if (mPositiveClickListener == null) {
            mButtonPositive.setVisibility(View.GONE);
            return;
        }
        if (!TextUtils.isEmpty(mPositiveText)) {
            mButtonPositive.setText(mPositiveText);
        }
        mButtonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPositiveClickListener.onClick();
            }
        });
    }

    private void initButtonNegative(Dialog dialog) {
        if (mNegativeClickListener == null) {
            mButtonNegative.setVisibility(View.GONE);
            return;
        }
        if (!TextUtils.isEmpty(mNegativeText)) {
            mButtonNegative.setText(mNegativeText);
        }
        mButtonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNegativeClickListener.onClick();
            }
        });
    }

    public void setMessage(String message) {
        mMessageText = message;
    }

    public void setPositiveText(String text) {
        mPositiveText = text;
    }

    public void setNegativeText(String text) {
        mNegativeText = text;
    }

    public void setPositiveClickListener(PositiveClickListener listener) {
        this.mPositiveClickListener = listener;
    }

    public void setNegativeClickListener(NegativeClickListener listener) {
        this.mNegativeClickListener = listener;
    }

    public interface PositiveClickListener {
        void onClick();
    }


    public interface NegativeClickListener {
        void onClick();
    }

}

