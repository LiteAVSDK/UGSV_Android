package com.tencent.qcloud.xiaoshipin.play;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * 录制完成后的预览界面
 */
public class TCVideoPreviewActivity extends Activity implements ITXVodPlayListener {
    public static final String TAG = "TCVideoPreviewActivity";

    boolean mVideoPlay = false;
    boolean mVideoPause = false;
    boolean mAutoPause = false;
    private TXVodPlayConfig mTXPlayConfig = null;
    private String mVideoPath;
    private TXVodPlayer mTXVodPlayer = null;
    private TXCloudVideoView mTXCloudVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_video_preview);

        mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);

        mTXVodPlayer = new TXVodPlayer(this);
        mTXPlayConfig = new TXVodPlayConfig();
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mTXCloudVideoView.disableLog(true);

        startPlay();
    }

    private boolean startPlay() {
        mTXVodPlayer.setPlayerView(mTXCloudVideoView);
        mTXVodPlayer.setVodListener(this);
        mTXVodPlayer.enableHardwareDecode(false);
        mTXVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mTXVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        mTXVodPlayer.setConfig(mTXPlayConfig);

        int result = mTXVodPlayer.startPlay(mVideoPath); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
        if (result != 0) {
            return false;
        }
        mVideoPlay = true;
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTXCloudVideoView.onResume();
        if (mVideoPlay && mAutoPause) {
            mTXVodPlayer.resume();
            mAutoPause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTXCloudVideoView.onPause();
        if (mVideoPlay && !mVideoPause) {
            mTXVodPlayer.pause();
            mAutoPause = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTXCloudVideoView.onDestroy();
        stopPlay(true);
        mTXVodPlayer = null;
        mTXCloudVideoView = null;
    }

    protected void stopPlay(boolean clearLastFrame) {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.setVodListener(null);
            mTXVodPlayer.stopPlay(clearLastFrame);
            mVideoPlay = false;
        }
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.setLogText(null, param, event);
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            mTXVodPlayer.resume(); // 播放结束后，可以直接resume()，如果调用stop和start，会导致重新播放会黑一下
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle bundle) {

    }

}
