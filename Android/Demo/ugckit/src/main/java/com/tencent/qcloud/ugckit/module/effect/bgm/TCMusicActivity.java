package com.tencent.qcloud.ugckit.module.effect.bgm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.tencent.liteav.basic.log.TXCLog;

import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.progressbutton.SampleProgressButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TCMusicActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private final String TAG = "TCMusicActivity";
    private LinearLayout mBackLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private View mEmptyView;

    private TCMusicAdapter mTCMusicAdapter;
    private TCMusicManager.LoadMusicListener mLoadMusicListener;
    private List<TCMusicInfo> mTCMusicInfoList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bgm_select);

        initData();
        initView();
        initListener();
        prepareToRefresh();
    }

    private void prepareToRefresh() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        this.onRefresh();
    }

    private void initData() {
        mTCMusicInfoList = new ArrayList<>();
    }

    private void initListener() {
        mLoadMusicListener = new TCMusicManager.LoadMusicListener() {
            @Override
            public void onBgmList(@Nullable final ArrayList<TCMusicInfo> tcBgmInfoList) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTCMusicInfoList.clear();
                        if (tcBgmInfoList != null) {
                            mTCMusicInfoList.addAll(tcBgmInfoList);
                        }
                        mTCMusicAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);

                        mEmptyView.setVisibility(mTCMusicAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                });
            }

            @Override
            public void onBgmDownloadSuccess(final int position, final String filePath) {
                Log.d(TAG, "onBgmDownloadSuccess filePath:" + filePath);
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TCMusicInfo info = mTCMusicInfoList.get(position);
                        info.status = TCMusicInfo.STATE_DOWNLOADED;
                        info.localPath = filePath;
                        mTCMusicAdapter.updateItem(position, info);
                    }
                });
            }

            @Override
            public void onDownloadFail(final int position, final String errorMsg) {
                Log.d(TAG, "onDownloadFail errorMsg:" + errorMsg);
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TCMusicInfo info = mTCMusicInfoList.get(position);
                        info.status = TCMusicInfo.STATE_UNDOWNLOAD;
                        info.progress = 0;
                        mTCMusicAdapter.updateItem(position, info);

                        ToastUtil.toastShortMessage(getResources().getString(R.string.bgm_select_activity_download_failed));
                    }
                });
            }

            @Override
            public void onDownloadProgress(final int position, final int progress) {
                Log.d(TAG, "onDownloadProgress progress:" + progress);
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mTCMusicInfoList != null && mTCMusicInfoList.size() > 0) {
                            TCMusicInfo info = mTCMusicInfoList.get(position);
                            if (info != null) {
                                info.status = TCMusicInfo.STATE_DOWNLOADING;
                                info.progress = progress;
                                mTCMusicAdapter.updateItem(position, info);
                            }
                        }
                    }
                });
            }
        };
        TCMusicManager.getInstance().setOnLoadMusicListener(mLoadMusicListener);
    }

    private void initView() {
        mBackLayout = (LinearLayout) findViewById(R.id.back_ll);
        mBackLayout.setOnClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setEnabled(false);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(5));

        mTCMusicAdapter = new TCMusicAdapter(this, mTCMusicInfoList);
        mTCMusicAdapter.setOnClickSubItemListener(new TCMusicAdapter.OnClickSubItemListener() {
            @Override
            public void onClickUseBtn(SampleProgressButton button, int position) {
                TCMusicInfo musicInfo = mTCMusicInfoList.get(position);
                if (musicInfo.status == TCMusicInfo.STATE_UNDOWNLOAD) {
                    musicInfo.status = TCMusicInfo.STATE_DOWNLOADING;
                    mTCMusicAdapter.updateItem(position, musicInfo);
                    downloadMusic(position);
                } else if (musicInfo.status == TCMusicInfo.STATE_DOWNLOADED) {
                    backToEditActivity(position, musicInfo.localPath);
                }
            }
        });
        mRecyclerView.setAdapter(mTCMusicAdapter);
        mEmptyView = findViewById(R.id.tv_bgm_empty);
    }

    private void downloadMusic(int position) {
        TCMusicInfo musicInfo = mTCMusicInfoList.get(position);
        TXCLog.i(TAG, "tcBgmInfo name = " + musicInfo.name + ", url = " + musicInfo.url);
        if (TextUtils.isEmpty(musicInfo.localPath)) {
            downloadMusicInfo(position, musicInfo);
            musicInfo.status = TCMusicInfo.STATE_DOWNLOADING;
            musicInfo.progress = 0;
            mTCMusicAdapter.updateItem(position, musicInfo);
            return;
        }
        File file = new File(musicInfo.localPath);
        if (!file.isFile()) {
            musicInfo.localPath = "";
            musicInfo.status = TCMusicInfo.STATE_DOWNLOADING;
            musicInfo.progress = 0;
            mTCMusicAdapter.updateItem(position, musicInfo);
            downloadMusicInfo(position, musicInfo);
            return;
        }
    }

    private void backToEditActivity(int position, String path) {
        Intent intent = new Intent();
        intent.putExtra(UGCKitConstants.MUSIC_POSITION, position);
        intent.putExtra(UGCKitConstants.MUSIC_PATH, path);
        intent.putExtra(UGCKitConstants.MUSIC_NAME, mTCMusicInfoList.get(position).name);
        setResult(UGCKitConstants.ACTIVITY_MUSIC_REQUEST_CODE, intent);
        finish();
    }

    @Override
    public void onRefresh() {
        TXCLog.i(TAG, "onRefresh");
        TCMusicManager.getInstance().loadMusicList();
    }

    private void downloadMusicInfo(int position, @NonNull TCMusicInfo TCMusicInfo) {
        TCMusicManager.getInstance().downloadMusicInfo(TCMusicInfo.name, position, TCMusicInfo.url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TCMusicManager.getInstance().setOnLoadMusicListener(null);
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.back_ll) {
            finish();
        }
    }

    private class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpace;

        @Override
        public void getItemOffsets(@NonNull Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.left = 0;
            outRect.right = 0;
            outRect.bottom = mSpace;
            outRect.top = 0;

        }

        public SpaceItemDecoration(int space) {
            this.mSpace = space;
        }
    }

}
