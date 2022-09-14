package com.tencent.qcloud.ugckit.component.dialogfragment;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.progressbar.NumberProgressBar;

public class VideoWorkProgressFragment extends DialogFragment {
    private static final String               KEY_TITLE  = "key_title";
    private              View                 mViewContent;
    private              ImageView            mImageStop;
    private              TextView             mTextTips;
    private              NumberProgressBar    mProgressLoading;
    private              View.OnClickListener mListener;
    private              int                  mProgress;
    private              boolean              mCanCancel = true;

    @NonNull
    public static VideoWorkProgressFragment newInstance(String title) {
        VideoWorkProgressFragment fragment = new VideoWorkProgressFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(R.style.UGCKitConfirmDialogStyle, R.style.UGCKitDialogFragmentStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mViewContent = inflater.inflate(R.layout.ugckit_layout_joiner_progress, null);
        mTextTips = (TextView) mViewContent.findViewById(R.id.joiner_tv_msg);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String msg = bundle.getString(KEY_TITLE);
            if (!TextUtils.isEmpty(msg)) {
                mTextTips.setText(msg);
            }
        }
        mImageStop = (ImageView) mViewContent.findViewById(R.id.joiner_iv_stop);
        mProgressLoading = (NumberProgressBar) mViewContent.findViewById(R.id.joiner_pb_loading);
        mProgressLoading.setMax(100);
        mProgressLoading.setProgress(mProgress);
        mImageStop.setOnClickListener(mListener);
        if (mCanCancel) {
            mImageStop.setVisibility(View.VISIBLE);
        } else {
            mImageStop.setVisibility(View.INVISIBLE);
        }
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        return mViewContent;
    }


    /**
     * 设置停止按钮的监听
     *
     * @param listener
     */
    public void setOnClickStopListener(View.OnClickListener listener) {
        if (mImageStop == null) {
            mListener = listener;
        } else {
            mListener = listener;
            mImageStop.setOnClickListener(listener);
        }
    }

    /**
     * 设置进度条
     *
     * @param progress
     */
    public void setProgress(int progress) {
        if (mProgressLoading == null) {
            mProgress = progress;
            return;
        }
        mProgressLoading.setProgress(progress);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            if (!isAdded() && null == manager.findFragmentByTag(tag)) {
                manager.beginTransaction().add(this, tag).commitAllowingStateLoss();
            } else {
                manager.beginTransaction().show(this).commitAllowingStateLoss();
            }
            //Fragment already added FIXBUG:commit()并不立即执行transaction中包含的动作,而是把它加入到UI线程队列中.
            //如果想要立即执行,可以在commit之后立即调用FragmentManager的executePendingTransactions()方法
            manager.executePendingTransactions();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                manager.beginTransaction().remove(this).add(this, tag).commitAllowingStateLoss();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void dismiss() {
        // 和show对应
        if (getFragmentManager() != null && isAdded()) {
            getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }
        if (mProgressLoading != null) {
            mProgressLoading.setProgress(0);
        }
    }

    public void setCanCancel(boolean canCancel) {
        mCanCancel = canCancel;
        if (mImageStop == null) {
        } else {
            if (canCancel) {
                mImageStop.setVisibility(View.VISIBLE);
            } else {
                mImageStop.setVisibility(View.INVISIBLE);
            }
        }
    }
}
