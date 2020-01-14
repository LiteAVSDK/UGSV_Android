package com.tencent.qcloud.ugckit.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.basic.ITitleBarLayout;
import com.tencent.qcloud.ugckit.R;

public class TitleBarLayout extends LinearLayout implements ITitleBarLayout {

    private LinearLayout mLeftGroup;
    private TextView mLeftTitle;
    private TextView mCenterTitle;
    private ImageView mLeftIcon;
    private Button mRightButton;
    private RelativeLayout mTitleLayout;

    public TitleBarLayout(Context context) {
        super(context);
        init();
    }

    public TitleBarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TitleBarLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.title_bar_layout, this);
        mTitleLayout = (RelativeLayout) findViewById(R.id.page_title_layout);
        mLeftGroup = (LinearLayout) findViewById(R.id.page_title_left_group);
        mLeftTitle = (TextView) findViewById(R.id.page_title_left_text);
        mCenterTitle = (TextView) findViewById(R.id.page_title);
        mLeftIcon = (ImageView) findViewById(R.id.page_title_left_icon);
        mRightButton = (Button) findViewById(R.id.btn_next);
    }

    @Override
    public void setLeftIcon(int resId) {
        mLeftIcon.setImageResource(resId);
    }

    @Override
    public void setOnBackClickListener(OnClickListener listener) {
        mLeftGroup.setOnClickListener(listener);
    }

    @Override
    public void setOnRightClickListener(OnClickListener listener) {
        mRightButton.setOnClickListener(listener);
    }

    @Override
    public void setTitle(String title, @NonNull POSITION position) {
        switch (position) {
            case LEFT:
                mLeftTitle.setText(title);
                break;
            case MIDDLE:
                mCenterTitle.setText(title);
                break;
            case RIGHT:
                mRightButton.setText(title);
                break;
        }
    }

    @Override
    public void setVisible(boolean enable, @NonNull POSITION position) {
        switch (position) {
            case LEFT:
                mLeftIcon.setVisibility(enable ? View.VISIBLE : View.GONE);
                break;
            case RIGHT:
                mRightButton.setVisibility(enable ? View.VISIBLE : View.GONE);
                break;
        }
    }

    @Override
    public LinearLayout getLeftGroup() {
        return mLeftGroup;
    }

    @Override
    public ImageView getLeftIcon() {
        return mLeftIcon;
    }

    @Override
    public Button getRightButton() {
        return mRightButton;
    }

    @Override
    public TextView getLeftTitle() {
        return mCenterTitle;
    }

    @Nullable
    @Override
    public TextView getMiddleTitle() {
        return null;
    }


}
