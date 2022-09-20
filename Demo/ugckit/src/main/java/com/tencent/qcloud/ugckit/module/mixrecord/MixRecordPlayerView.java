package com.tencent.qcloud.ugckit.module.mixrecord;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tencent.cos.xml.utils.StringUtils;
import com.tencent.qcloud.ugckit.R;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXVideoEditConstants;

import java.util.List;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_BEGIN;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_END;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_PROGRESS;

public class MixRecordPlayerView extends RelativeLayout implements ITXVodPlayListener, IPlayerView {
    private static final String TAG = "MixRecordPlayerView";

    private TXVodPlayer      mVodPlayer;
    private TXCloudVideoView mCloudView;
    private ImageView        mCoverimg;
    private PlayerState      mPlayerState      = PlayerState.STATE_UNINIT;
    private String           mVideoPath;
    private int              mIndex            = -1;
    private float            mContinuePosition = -1;

    public MixRecordPlayerView(Context context) {
        this(context, null);
    }

    public MixRecordPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MixRecordPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.ugckit_mix_record_player_view, this);
        mCloudView = (TXCloudVideoView) findViewById(R.id.mix_player_view);
        mCoverimg = (ImageView) findViewById(R.id.cover);
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (event != PLAY_EVT_PLAY_PROGRESS) {
            Log.i(TAG, "onPlayEvent: " + event);
            if (event == PLAY_EVT_PLAY_END) {
            } else if (event == PLAY_EVT_PLAY_BEGIN) {
            }
        } else {
            Log.d(TAG, "onPlayEvent: PLAY_EVT_PLAY_PROGRESS " + param.toString());
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle status) {

    }

    @Override
    public TXCloudVideoView getVideoView() {
        return mCloudView;
    }

    @Override
    public void updateFile(int index, String videoPath) {
        if (mVodPlayer != null) {
            mVodPlayer.stopPlay(true);
            mVideoPath = videoPath;
            initInner();
        }
    }

    @Override
    public List<TXVideoEditConstants.TXAbsoluteRect> getCombineRects(MixRecordConfig config) {
        return null;
    }

    @Override
    public float getContinuePosition() {
        return mContinuePosition;
    }

    @Override
    public void setContinuePosition(float position) {
        mContinuePosition = position;
    }

    public void setMute(boolean mute) {
        if (mVodPlayer != null) {
            mVodPlayer.setMute(mute);
        }
    }

    private void initInner() {
        mVodPlayer = new TXVodPlayer(getContext().getApplicationContext());
        mVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mVodPlayer.setVodListener(this);
        TXVodPlayConfig config = new TXVodPlayConfig();
        mVodPlayer.setConfig(config);
        mVodPlayer.setAutoPlay(false);
        mVodPlayer.setPlayerView(mCloudView);
        if (mIndex != 0) {
            mVodPlayer.setMute(true);
        }
        mPlayerState = PlayerState.STATE_INITED;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mVideoPath);
        Bitmap bitmap = retriever.getFrameAtTime(0, OPTION_CLOSEST_SYNC);
        mCoverimg.setImageBitmap(bitmap);
        mCoverimg.setVisibility(VISIBLE);
    }

    @Override
    public boolean init(int index, String videoPath) {
        if (StringUtils.isEmpty(videoPath)) {
            mIndex = index;
            mCoverimg.setVisibility(GONE);
            return true;
        }
        mIndex = index;
        mVideoPath = videoPath;
        initInner();
        return true;
    }

    private void hideCover() {
        if (mCoverimg.getVisibility() == VISIBLE) {
            mCoverimg.animate().alpha(0).setDuration(100).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mCoverimg.setVisibility(GONE);
                    mCoverimg.setAlpha(1.0f);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
    }

    @Override
    public boolean startVideo() {
        if (mVodPlayer == null) {
            return false;
        }
        if (mPlayerState == PlayerState.STATE_PAUSED) {
            hideCover();
            mVodPlayer.resume();
            if (mContinuePosition != -1) {
                seekVideo((long) (mContinuePosition * 1000));
                mContinuePosition = -1;
            }
            mPlayerState = PlayerState.STATE_PLAYING;
            return true;
        }
        if (mPlayerState == PlayerState.STATE_PLAYING) {
            hideCover();
            mVodPlayer.resume();
            if (mContinuePosition != -1) {
                seekVideo((long) (mContinuePosition * 1000));
                mContinuePosition = -1;
            }
            return true;
        }
        if (mPlayerState == PlayerState.STATE_STOPED) {
            initInner();
        }
        int result = mVodPlayer.startVodPlay(mVideoPath);
        if (result != 0) {
            return false;
        }
        mPlayerState = PlayerState.STATE_PLAYING;
        return true;
    }

    @Override
    public void stopVideo() {
        if (mVodPlayer != null) {//&& mPlayerState != PlayerState.STATE_PLAYING
            mVodPlayer.setVodListener(null);
            mContinuePosition = mVodPlayer.getCurrentPlaybackTime();
            mVodPlayer.stopPlay(false);
            mPlayerState = PlayerState.STATE_STOPED;
        }
    }

    @Override
    public void pauseVideo() {
        if (mVodPlayer != null) {
            mVodPlayer.pause();
            if (mPlayerState != PlayerState.STATE_STOPED) {
                mPlayerState = PlayerState.STATE_PAUSED;
            }
        }
    }

    @Override
    public void seekVideo(long position) {
        if (mVodPlayer != null) {
            mVodPlayer.seek(position / 1000.0f);
        }
    }

    @Override
    public void releaseVideo() {
        Log.i(TAG, "releaseVideo");
        if (mVodPlayer != null && mPlayerState != PlayerState.STATE_STOPED) {
            mVodPlayer.stopPlay(true);
            mPlayerState = PlayerState.STATE_STOPED;
        }
    }

    enum PlayerState {
        STATE_UNINIT,
        STATE_INITED,
        STATE_PLAYING,
        STATE_PAUSED,
        STATE_STOPED;
    }
}
