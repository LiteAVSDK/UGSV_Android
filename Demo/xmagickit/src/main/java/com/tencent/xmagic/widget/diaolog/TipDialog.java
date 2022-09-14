package com.tencent.xmagic.widget.diaolog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.tencent.xmagic.demo.R;


/**
 * 用于展示提示语的dialog
 * 可以修改 title，提示信息，左右按钮文字，以及按钮的响应信息
 */
public class TipDialog extends AlertDialog {

    private TextView titleTv;
    private TextView msgTv;
    private Button leftBtn;
    private Button rightBtn;
    private TipDialogClickListener tipDialogClickListener = null;

    private String dialogTitle;
    private String dialogMsg;
    private String dialogLeftBtnStr;
    private String dialogRightBtnStr;


    public TipDialog(@NonNull Context context) {
        super(context);
    }

    public TipDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public TipDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_dialog_layout);
        titleTv = findViewById(R.id.tip_dialog_title);
        msgTv = findViewById(R.id.tip_dialog_msg_tv);
        leftBtn = findViewById(R.id.tip_dialog_left_btn);
        rightBtn = findViewById(R.id.tip_dialog_right_btn);

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipDialogClickListener != null) {
                    tipDialogClickListener.onLeftBtnClick(leftBtn);
                }
                dismiss();
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipDialogClickListener != null) {
                    tipDialogClickListener.onRightBtnCLick(rightBtn);
                }
                dismiss();
            }
        });
        setData();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void setData() {
        if (!TextUtils.isEmpty(dialogTitle)) {
            titleTv.setText(dialogTitle);
        } else {
            titleTv.setText("");
        }
        if (!TextUtils.isEmpty(dialogMsg)) {
            msgTv.setText(dialogMsg);
        } else {
            msgTv.setText("");
        }
        if (!TextUtils.isEmpty(dialogLeftBtnStr)) {
            leftBtn.setText(dialogLeftBtnStr);
        } else {
            leftBtn.setText("");
        }
        if (!TextUtils.isEmpty(dialogRightBtnStr)) {
            rightBtn.setText(dialogRightBtnStr);
        } else {
            rightBtn.setText("");
        }
    }

    public TipDialog setData(String title, String msg, String leftTxt, String rightTxt) {
        dialogTitle = title;
        dialogMsg = msg;
        dialogLeftBtnStr = leftTxt;
        dialogRightBtnStr = rightTxt;
        return this;
    }

    public TipDialog setClickListener(TipDialogClickListener clickListener) {
        tipDialogClickListener = clickListener;
        return this;
    }


    public interface TipDialogClickListener {
        void onLeftBtnClick(Button btn);

        void onRightBtnCLick(Button btn);
    }


}
