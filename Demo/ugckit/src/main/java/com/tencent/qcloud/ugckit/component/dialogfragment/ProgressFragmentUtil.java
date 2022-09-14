package com.tencent.qcloud.ugckit.component.dialogfragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.view.View;


import com.tencent.qcloud.ugckit.utils.BackgroundTasks;

public class ProgressFragmentUtil {
    private String                    mTitle;
    private FragmentActivity          mActivity;
    private VideoWorkProgressFragment mLoadingProgress;

    public ProgressFragmentUtil(FragmentActivity context) {
        mActivity = context;
    }

    /**
     * 带标题的进度条
     *
     * @param context
     * @param title
     */
    public ProgressFragmentUtil(FragmentActivity context, String title) {
        mActivity = context;
        mTitle = title;
    }

    /**
     * 显示生成进度条
     */
    public void showLoadingProgress(@Nullable final IProgressListener listener) {
        if (mLoadingProgress == null) {
            mLoadingProgress = VideoWorkProgressFragment.newInstance(mTitle);
            mLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLoadingProgress.setProgress(0);
                    if (listener != null) {
                        listener.onStop();
                    }
                }
            });
        }
        mLoadingProgress.setProgress(0);
        mLoadingProgress.setCancelable(false);

        mLoadingProgress.show(mActivity.getSupportFragmentManager(), "progress_dialog");
    }

    /**
     * 生成进度条取消
     */
    public void dismissLoadingProgress() {
        if (mLoadingProgress != null) {
            mLoadingProgress.setProgress(0);
            mLoadingProgress.dismiss();
        }
    }

    /**
     * 更新生成进度条进度
     *
     * @param progress
     */
    public void updateGenerateProgress(final int progress) {
        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoadingProgress != null) {
                    mLoadingProgress.setProgress(progress);
                }
            }
        });
    }

    public interface IProgressListener {
        void onStop();
    }
}
