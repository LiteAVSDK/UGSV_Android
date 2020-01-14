package com.tencent.qcloud.ugckit.component.dialogfragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.progressbar.NumberProgressBar;

public class VideoWorkProgressFragment extends DialogFragment {
    private static final String KEY_TITLE = "key_title";
    private View mContentView;
    private ImageView mIvStop;
    private TextView mTvTips;
    private NumberProgressBar mPbLoading;
    private int mProgress;
    private boolean mCanCancel = true;
    private View.OnClickListener mListener;

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
        setStyle(R.style.ConfirmDialogStyle, R.style.DialogFragmentStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.layout_joiner_progress, null);
        mTvTips = (TextView) mContentView.findViewById(R.id.joiner_tv_msg);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String msg = bundle.getString(KEY_TITLE);
            if (!TextUtils.isEmpty(msg)) {
                mTvTips.setText(msg);
            }
        }
        mIvStop = (ImageView) mContentView.findViewById(R.id.joiner_iv_stop);
        mPbLoading = (NumberProgressBar) mContentView.findViewById(R.id.joiner_pb_loading);
        mPbLoading.setMax(100);
        mPbLoading.setProgress(mProgress);
        mIvStop.setOnClickListener(mListener);
        if (mCanCancel) {
            mIvStop.setVisibility(View.VISIBLE);
        } else {
            mIvStop.setVisibility(View.INVISIBLE);
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
        return mContentView;
    }


    /**
     * 设置停止按钮的监听
     *
     * @param listener
     */
    public void setOnClickStopListener(View.OnClickListener listener) {
        if (mIvStop == null) {
            mListener = listener;
        } else {
            mListener = listener;
            mIvStop.setOnClickListener(listener);
        }
    }

    /**
     * 设置进度条
     *
     * @param progress
     */
    public void setProgress(int progress) {
        if (mPbLoading == null) {
            mProgress = progress;
            return;
        }
        mPbLoading.setProgress(progress);
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
        mPbLoading.setProgress(0);
    }

    public void setCanCancel(boolean canCancel) {
        mCanCancel = canCancel;
        if (mIvStop == null) {
        } else {
            if (canCancel) {
                mIvStop.setVisibility(View.VISIBLE);
            } else {
                mIvStop.setVisibility(View.INVISIBLE);
            }
        }
    }
}
