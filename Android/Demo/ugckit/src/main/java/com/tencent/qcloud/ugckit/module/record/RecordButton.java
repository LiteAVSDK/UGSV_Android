package com.tencent.qcloud.ugckit.module.record;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.record.interfaces.IRecordButton;

/**
 * 多种拍摄模式的按钮
 */
public class RecordButton extends RelativeLayout implements IRecordButton, View.OnTouchListener {
    private Activity  mActivity;
    private ViewGroup mRootLayout;
    private View      mViewPhotoModeOutter;
    private View      mViewPhotoModeInner;
    private View      mViewTapModeOutter;
    private View      mViewTapModeInner;
    private View      mViewPressModeOutter;
    private View      mViewPressModeInner;
    private ImageView mImageRecordPause;
    private int       mRecordMode;
    private boolean   mIsRecording;

    private OnRecordButtonListener mOnRecordButtonListener;

    public RecordButton(Context context) {
        super(context);
        initViews();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.ugckit_record_button, this);
        setOnTouchListener(this);

        mRootLayout = (ViewGroup) findViewById(R.id.layout_compose_record_btn);

        mViewPhotoModeOutter = findViewById(R.id.view_take_photo_bkg);
        mViewPhotoModeInner = findViewById(R.id.view_take_photo);

        mViewTapModeOutter = findViewById(R.id.view_record_click_shot_bkg);
        mViewTapModeInner = findViewById(R.id.view_record_click_shot);

        mImageRecordPause = (ImageView) findViewById(R.id.iv_record_pause);

        mViewPressModeOutter = findViewById(R.id.view_record_touch_shot_bkg);
        mViewPressModeInner = findViewById(R.id.view_record_touch_shot);

        mViewPhotoModeOutter.setVisibility(GONE);
        mViewPhotoModeInner.setVisibility(GONE);

        mViewTapModeOutter.setVisibility(VISIBLE);
        mViewTapModeInner.setVisibility(VISIBLE);
        mImageRecordPause.setVisibility(GONE);

        mViewPressModeOutter.setVisibility(GONE);
        mViewPressModeInner.setVisibility(GONE);
    }

    @Override
    public boolean onTouch(View view, @NonNull MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                switch (mRecordMode) {
                    case RecordModeView.RECORD_MODE_TAKE_PHOTO:
                        startTakePhotoAnim();
                        break;
                    case RecordModeView.RECORD_MODE_CLICK:
                        toggleRecordAnim();
                        break;
                    case RecordModeView.RECORD_MODE_LONG_TOUCH:
                        startRecordAnim();
                        mIsRecording = true;
                        break;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                switch (mRecordMode) {
                    case RecordModeView.RECORD_MODE_TAKE_PHOTO:
                        endTakePhotoAnim();
                        break;
                    case RecordModeView.RECORD_MODE_LONG_TOUCH:
                        pauseRecordAnim();
                        break;
                }
                break;
            }
        }
        return true;
    }

    /**
     * 更新视频拍摄模式
     *
     * @param recordMode
     */
    @Override
    public void setCurrentRecordMode(int recordMode) {
        mRecordMode = recordMode;

        mViewPhotoModeOutter.setVisibility(GONE);
        mViewPhotoModeInner.setVisibility(GONE);

        mViewTapModeOutter.setVisibility(GONE);
        mViewTapModeInner.setVisibility(GONE);

        mViewPressModeOutter.setVisibility(GONE);
        mViewPressModeInner.setVisibility(GONE);

        switch (mRecordMode) {
            case RecordModeView.RECORD_MODE_TAKE_PHOTO:
                mViewPhotoModeOutter.setVisibility(VISIBLE);
                mViewPhotoModeInner.setVisibility(VISIBLE);
                break;
            case RecordModeView.RECORD_MODE_CLICK:
                mViewTapModeOutter.setVisibility(VISIBLE);
                mViewTapModeInner.setVisibility(VISIBLE);
                break;
            case RecordModeView.RECORD_MODE_LONG_TOUCH:
                mViewPressModeOutter.setVisibility(VISIBLE);
                mViewPressModeInner.setVisibility(VISIBLE);
                break;
        }
    }

    /**
     * 开始"拍照"操作执行的动画
     */
    private void startTakePhotoAnim() {
        ObjectAnimator btnBkgZoomOutXAn = ObjectAnimator.ofFloat(mViewPhotoModeOutter, "scaleX", ((float) mRootLayout.getWidth()) / mViewPhotoModeOutter.getWidth());
        ObjectAnimator btnBkgZoomOutYAn = ObjectAnimator.ofFloat(mViewPhotoModeOutter, "scaleY", ((float) mRootLayout.getHeight()) / mViewPhotoModeOutter.getHeight());

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "scaleX", 0.95f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "scaleY", 0.95f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomOutXAn).with(btnBkgZoomOutYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnRecordButtonListener != null) {
                    mOnRecordButtonListener.onTakePhoto();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    /**
     * 结束"拍照"操作时执行的动画
     */
    public void endTakePhotoAnim() {
        ObjectAnimator btnBkgZoomInXAn = ObjectAnimator.ofFloat(mViewPhotoModeOutter, "scaleX", 1f);
        ObjectAnimator btnBkgZoomIntYAn = ObjectAnimator.ofFloat(mViewPhotoModeOutter, "scaleY", 1f);

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "scaleX", 1f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "scaleY", 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomInXAn).with(btnBkgZoomIntYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.start();
    }

    /**
     * 切换录制"开始"和"暂停"状态 执行的动画
     */
    private void toggleRecordAnim() {
        if (mIsRecording) {
            pauseRecordAnim();
        } else {
            startRecordAnim();
        }
    }

    /**
     * 开始录制操作执行的动画
     */
    public void startRecordAnim() {
        if (mRecordMode == RecordModeView.RECORD_MODE_CLICK) {
            startRecordAnimByClick();
        } else {
            startRecordAnimByLongTouch();
        }
    }

    /**
     * 暂停录制操作执行的动画
     */
    public void pauseRecordAnim() {
        if (mRecordMode == RecordModeView.RECORD_MODE_CLICK) {
            pauseRecordAnimByClick();
        } else {
            pauseRecordAnimByLongTouch();
        }
    }

    /**
     * 拍摄模式为"长按"录制下。开始录制操作执行的动画
     */
    private void startRecordAnimByLongTouch() {
        ObjectAnimator btnBkgZoomOutXAn = ObjectAnimator.ofFloat(mViewPressModeOutter, "scaleX",
                ((float) mRootLayout.getWidth()) / mViewPressModeOutter.getWidth());
        ObjectAnimator btnBkgZoomOutYAn = ObjectAnimator.ofFloat(mViewPressModeOutter, "scaleY",
                ((float) mRootLayout.getHeight()) / mViewPressModeOutter.getHeight());

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewPressModeInner, "scaleX", 0.95f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewPressModeInner, "scaleY", 0.95f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomOutXAn).with(btnBkgZoomOutYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnRecordButtonListener != null) {
                    mOnRecordButtonListener.onRecordStart();
                    mIsRecording = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    /**
     * 拍摄模式为"单击"录制下。开始录制操作执行的动画
     */
    private void startRecordAnimByClick() {
        ObjectAnimator btnBkgZoomOutXAn = ObjectAnimator.ofFloat(mViewTapModeOutter, "scaleX",
                ((float) mRootLayout.getWidth()) / mViewTapModeOutter.getWidth());
        ObjectAnimator btnBkgZoomOutYAn = ObjectAnimator.ofFloat(mViewTapModeOutter, "scaleY",
                ((float) mRootLayout.getHeight()) / mViewTapModeOutter.getHeight());

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewTapModeInner, "scaleX", 0.95f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewTapModeInner, "scaleY", 0.95f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomOutXAn).with(btnBkgZoomOutYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnRecordButtonListener != null) {
                    mOnRecordButtonListener.onRecordStart();
                    mIsRecording = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
        mImageRecordPause.setVisibility(View.VISIBLE);
    }

    /**
     * 拍摄模式为"长按"录制下。暂停录制操作执行的动画
     */
    private void pauseRecordAnimByLongTouch() {
        ObjectAnimator btnBkgZoomInXAn = ObjectAnimator.ofFloat(mViewPressModeOutter, "scaleX", 1f);
        ObjectAnimator btnBkgZoomIntYAn = ObjectAnimator.ofFloat(mViewPressModeOutter, "scaleY", 1f);

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewPressModeInner, "scaleX", 1f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewPressModeInner, "scaleY", 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomInXAn).with(btnBkgZoomIntYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnRecordButtonListener != null) {
                    mOnRecordButtonListener.onRecordPause();
                    mIsRecording = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
        mImageRecordPause.setVisibility(View.GONE);
    }

    /**
     * 拍摄模式为"单击"录制下。暂停录制操作执行的动画
     */
    private void pauseRecordAnimByClick() {
        ObjectAnimator btnBkgZoomInXAn = ObjectAnimator.ofFloat(mViewTapModeOutter, "scaleX", 1f);
        ObjectAnimator btnBkgZoomIntYAn = ObjectAnimator.ofFloat(mViewTapModeOutter, "scaleY", 1f);

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewTapModeInner, "scaleX", 1f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewTapModeInner, "scaleY", 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomInXAn).with(btnBkgZoomIntYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mOnRecordButtonListener != null) {
                    mOnRecordButtonListener.onRecordPause();
                    mIsRecording = false;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
        mImageRecordPause.setVisibility(View.GONE);
    }

    @Override
    public void setOnRecordButtonListener(OnRecordButtonListener listener) {
        mOnRecordButtonListener = listener;
    }

    @Override
    public void setPhotoOutterColor(int color) {
        mViewPhotoModeOutter.setBackgroundResource(color);
    }

    @Override
    public void setPhotoInnerColor(int color) {
        mViewPhotoModeInner.setBackgroundResource(color);
    }

    @Override
    public void setClickRecordOutterColor(int color) {
        mViewTapModeOutter.setBackgroundResource(color);
    }

    @Override
    public void setClickRecordInnerColor(int color) {
        mViewTapModeInner.setBackgroundResource(color);
    }

    @Override
    public void setTouchRecordOutterColor(int color) {
        mViewPressModeOutter.setBackgroundResource(color);
    }

    @Override
    public void setTouchRecordInnerColor(int color) {
        mViewPressModeInner.setBackgroundResource(color);
    }

    @Override
    public void setPauseIconResource(int resid) {
        mImageRecordPause.setImageResource(resid);
    }

}
