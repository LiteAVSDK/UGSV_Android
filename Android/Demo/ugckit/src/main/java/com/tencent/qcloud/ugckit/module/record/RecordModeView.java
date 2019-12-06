package com.tencent.qcloud.ugckit.module.record;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.R;


/**
 * 拍摄模式
 */
public class RecordModeView extends RelativeLayout implements View.OnClickListener {
    private Activity mActivity;
    // 拍摄方式选择，目前支持三种（单击拍照，单击录制，长按录制）
    public static final int RECORD_MODE_TAKE_PHOTO = 1;
    public static final int RECORD_MODE_CLICK = 2;
    public static final int RECORD_MODE_LONG_TOUCH = 3;

    private LinearLayout mLayoutRecordMode;
    private TextView mTvPhoto;
    private TextView mTvClick;
    private TextView mTvTouch;
    private OnRecordModeListener mOnRecordModeListener;
    private boolean mDisableTakePhoto;
    private boolean mDisableLongPressRecord;

    public RecordModeView(Context context) {
        super(context);
        initViews();
    }

    public RecordModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public RecordModeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.record_mode_layout, this);

        mLayoutRecordMode = (LinearLayout) findViewById(R.id.layout_record_mode);
        mTvPhoto = (TextView) findViewById(R.id.tv_photo);
        mTvClick = (TextView) findViewById(R.id.tv_click);
        mTvTouch = (TextView) findViewById(R.id.tv_touch);

        mTvClick.setSelected(true);
        mTvPhoto.setOnClickListener(this);
        mTvClick.setOnClickListener(this);
        mTvTouch.setOnClickListener(this);
    }

    /**
     * 合唱模式：禁用拍照，仅支持"单击拍"和"按住拍"
     */
    public void disableTakePhoto() {
        mTvPhoto.setVisibility(View.INVISIBLE);
    }

    public void disableLongPressRecord() {
        mTvTouch.setVisibility(View.INVISIBLE);
    }

    //如果禁用拍照和长按拍摄，则仅剩下单击拍摄
    public void selectOneRecordMode() {
        mTvClick.setVisibility(View.INVISIBLE);
        mTvTouch.setVisibility(View.INVISIBLE);
        mTvPhoto.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(@NonNull View view) {
        if (view.isSelected()) {
            return;
        }
        float xGap = 0;
        int id = view.getId();

        if (id == R.id.tv_photo) {
            /**
             * 切换为拍照模式
             */
            if (mTvClick.isSelected()) {
                xGap = 1.0f / 3;
            } else if (mTvTouch.isSelected()) {
                xGap = 2.0f / 3;
            }
            mTvPhoto.setSelected(true);
            mTvClick.setSelected(false);
            mTvTouch.setSelected(false);

            if (mOnRecordModeListener != null) {
                mOnRecordModeListener.onRecordModeSelect(RecordModeView.RECORD_MODE_TAKE_PHOTO);
            }
        } else if (id == R.id.tv_click) {
            if (mTvPhoto.isSelected()) {
                xGap = -1.0f / 3;
            } else if (mTvTouch.isSelected()) {
                xGap = 1.0f / 3;
            }
            mTvPhoto.setSelected(false);
            mTvClick.setSelected(true);
            mTvTouch.setSelected(false);

            if (mOnRecordModeListener != null) {
                mOnRecordModeListener.onRecordModeSelect(RecordModeView.RECORD_MODE_CLICK);
            }
        } else if (id == R.id.tv_touch) {
            if (mTvPhoto.isSelected()) {
                xGap = -2.0f / 3;
            } else if (mTvClick.isSelected()) {
                xGap = -1.0f / 3;
            }
            mTvPhoto.setSelected(false);
            mTvClick.setSelected(false);
            mTvTouch.setSelected(true);

            if (mOnRecordModeListener != null) {
                mOnRecordModeListener.onRecordModeSelect(RecordModeView.RECORD_MODE_LONG_TOUCH);
            }
        }
        float x1 = mLayoutRecordMode.getTranslationX();
        float x2 = x1 + mLayoutRecordMode.getWidth() * xGap;

        ObjectAnimator animator = ObjectAnimator.ofFloat(mLayoutRecordMode, "translationX", x1, x2);
        animator.setDuration(400);
        animator.start();
    }

    /**
     * 设置切换"拍摄模式"监听器
     *
     * @param listener
     */
    public void setOnRecordModeListener(RecordModeView.OnRecordModeListener listener) {
        mOnRecordModeListener = listener;
    }

    public interface OnRecordModeListener {
        /**
         * 选择一种拍摄模式
         *
         * @param currentMode 当前拍摄模式
         */
        void onRecordModeSelect(int currentMode);
    }

    public interface OnSnapListener {
        void onSnap(Bitmap bitmap);
    }

}
