package com.tencent.qcloud.ugckit.component.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tencent.qcloud.ugckit.R;


public class CommonDialog {

    private Context mContext;
    private Dialog mDialog;
    private TextView mTextTitle;
    private TextView mTextContent;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    private View mVerticalLine;
    private OnCommonDialogBtnClickListener mOnCommonDialogBtnClickListener = null;

    public CommonDialog(@NonNull Context context) {
        this.mContext = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initViews();
    }

    @NonNull
    private void initViews() {
        // 获取Dialog布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.ugckit_view_common_dialog, null);

        mTextTitle = (TextView) view.findViewById(R.id.txt_title);
        mTextContent = view.findViewById(R.id.content_txt);
        mLeftBtn = (TextView) view.findViewById(R.id.left_btn);
        mRightBtn = (TextView) view.findViewById(R.id.right_btn);
        mVerticalLine = view.findViewById(R.id.vertical_line);
        mLeftBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (mOnCommonDialogBtnClickListener != null) {
                    mOnCommonDialogBtnClickListener.onLeftBtnClick();
                }
            }
        });
        mRightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (mOnCommonDialogBtnClickListener != null) {
                    mOnCommonDialogBtnClickListener.onRightBtnClick();
                }
            }
        });

        // 定义Dialog布局和参数
        mDialog = new Dialog(mContext, R.style.UGCKitActionSheetDialogStyle);
        mDialog.setContentView(view);
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);

    }

    @NonNull
    public CommonDialog setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            mTextTitle.setVisibility(View.GONE);
        } else {
            mTextTitle.setText(title);
        }
        return this;
    }

    public CommonDialog setContent(String content) {
        mTextContent.setText(content);
        return this;
    }

    public CommonDialog setContent(CharSequence content) {
        mTextContent.setMovementMethod(LinkMovementMethod.getInstance());
        mTextContent.setText(content);
        mTextContent.setHighlightColor(Color.TRANSPARENT);
        return this;
    }

    @NonNull
    public CommonDialog setCancelable(boolean cancel) {
        mDialog.setCancelable(cancel);
        return this;
    }

    public CommonDialog setBtnTxt(String leftTxt, String rightTxt) {
        if (!TextUtils.isEmpty(leftTxt)) {
            mLeftBtn.setText(leftTxt);
            mLeftBtn.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(rightTxt)) {
            mRightBtn.setText(rightTxt);
            mRightBtn.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public CommonDialog setOnClickListener(OnCommonDialogBtnClickListener clickListener) {
        mOnCommonDialogBtnClickListener = clickListener;
        return this;
    }

    @NonNull
    public CommonDialog setCanceledOnTouchOutside(boolean cancel) {
        mDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }


    public void show() {
        if (mLeftBtn.getVisibility() == View.GONE) {
            mVerticalLine.setVisibility(View.GONE);
        }
        mDialog.show();
    }

    public interface OnCommonDialogBtnClickListener {
        void onLeftBtnClick();

        void onRightBtnClick();
    }


}
