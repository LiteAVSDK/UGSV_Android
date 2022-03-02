package com.tencent.qcloud.ugckit.module.record;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.R;

/**
 * 录制-拍照ImageView
 */
public class ImageSnapShotView extends RelativeLayout {
    private ImageView mIvSnapshotView;

    public ImageSnapShotView(Context context) {
        super(context);
        initViews();
    }

    public ImageSnapShotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ImageSnapShotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_image_snap_shot_view, this);
        mIvSnapshotView = (ImageView) findViewById(R.id.iv_snapshot_photo);
    }

    public void showSnapshotAnim(Bitmap bitmap) {
        mIvSnapshotView.setTranslationX(0);
        mIvSnapshotView.setTranslationY(0);
        mIvSnapshotView.setScaleX(1);
        mIvSnapshotView.setScaleY(1);
        mIvSnapshotView.setPivotX(0);
        mIvSnapshotView.setPivotY(0);
        mIvSnapshotView.setAlpha(1.0f);
        mIvSnapshotView.setImageBitmap(bitmap);
        mIvSnapshotView.setVisibility(View.VISIBLE);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float screenWidth = dm.widthPixels;

        float vWidth = mIvSnapshotView.getWidth();

        float density = getResources().getDisplayMetrics().density;

        float targetWidthInDP = 80;

        float targetWidth = targetWidthInDP * density;

        float scale = targetWidth / vWidth;

        float targetLocalX = screenWidth - 40 * density - targetWidth;
        float targetLocalY = 40 * density;

        float translationX = targetLocalX - 0;
        float translationY = targetLocalY - 0;

        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(mIvSnapshotView, "scaleX", 1, scale);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(mIvSnapshotView, "scaleY", 1, scale);
        ObjectAnimator animatorTranslationX = ObjectAnimator.ofFloat(mIvSnapshotView, "translationX", 0, translationX);
        ObjectAnimator animatorTranslationY = ObjectAnimator.ofFloat(mIvSnapshotView, "translationY", 0, translationY);

        AnimatorSet animatorSet1 = new AnimatorSet();
        animatorSet1.setDuration(500);
        animatorSet1.setInterpolator(new DecelerateInterpolator());
        animatorSet1.play(animatorScaleX).with(animatorScaleY).with(animatorTranslationX).with(animatorTranslationY);

        ObjectAnimator animatorFadeOut = ObjectAnimator.ofFloat(mIvSnapshotView, "alpha", 1.0f, 1.0f, 0.0f);


        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.setDuration(500);
        animatorSet2.setInterpolator(new LinearInterpolator());
        animatorSet2.play(animatorFadeOut);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorSet1);
        animatorSet.play(animatorSet2).after(animatorSet1);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIvSnapshotView.setVisibility(View.INVISIBLE);
                ToastUtil.toastShortMessage(getResources().getString(R.string.ugckit_activity_video_record_take_photo_success));
//                mIsTakingPhoto = false;
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
}
