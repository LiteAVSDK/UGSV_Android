package com.tencent.qcloud.xiaoshipin.videopublish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.UGCKitVideoPublish;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.mainui.TCMainActivity;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCVideoInfo;
import com.tencent.qcloud.xiaoshipin.play.TCVodPlayerActivity;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * 小视频发布页面
 */
public class TCVideoPublisherActivity extends Activity {
    private String mVideoPath = null;
    private String mCoverPath = null;
    private boolean mDisableCache;
    private UGCKitVideoPublish mUGCKitVideoPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowParam();
        setContentView(R.layout.activity_video_publisher);
        mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
        mCoverPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_COVERPATH);
        mDisableCache = getIntent().getBooleanExtra(UGCKitConstants.VIDEO_RECORD_NO_CACHE, false);
        mUGCKitVideoPublish = (UGCKitVideoPublish) findViewById(R.id.video_publish_layout);
        /**
         * 设置发布视频的路径和封面
         */
        mUGCKitVideoPublish.setPublishPath(mVideoPath, mCoverPath);
        /**
         * 设置是否开启本地缓存，若关闭本地缓存，则发布完成后删除"已发布"的视频和封面
         */
        mUGCKitVideoPublish.setCacheEnable(mDisableCache);
        /**
         * 设置发布视频的监听器
         */
        mUGCKitVideoPublish.setOnPublishListener(new UGCKitVideoPublish.OnPublishListener() {
            @Override
            public void onPublishComplete(String videoId, String videoURL, String coverURL) {
                /**
                 * 发布完成，返回主界面
                 */
                backToMainActivity();
                // 播放
                TCVideoInfo videoInfo = new TCVideoInfo();
                videoInfo.nickname = TCUserMgr.getInstance().getNickname();
                videoInfo.headpic = TCUserMgr.getInstance().getHeadPic();
                videoInfo.playurl = videoURL;
                videoInfo.fileid = videoId;
                videoInfo.frontcover = coverURL;
                videoInfo.review_status = TCVideoInfo.REVIEW_STATUS_NORMAL;
                startVodPlay(videoInfo);
            }

            @Override
            public void onPublishCancel() {
                /**
                 * 发布取消，退出发布页面
                 */
                finish();
            }
        });
    }

    private void startVodPlay(TCVideoInfo item) {
        ArrayList<TCVideoInfo> videoList = new ArrayList<TCVideoInfo>();
        videoList.add(item);

        Intent intent = new Intent(this, TCVodPlayerActivity.class);
        intent.putExtra(UGCKitConstants.PLAY_URL, item.playurl);
        intent.putExtra(
                UGCKitConstants.PUSHER_NAME, item.nickname == null ? item.userid : item.nickname);
        intent.putExtra(UGCKitConstants.PUSHER_AVATAR, item.headpic);
        intent.putExtra(UGCKitConstants.COVER_PIC, item.frontcover);
        intent.putExtra(UGCKitConstants.FILE_ID, item.fileid != null ? item.fileid : "");
        intent.putExtra(UGCKitConstants.TCLIVE_INFO_LIST, (Serializable) videoList);
        intent.putExtra(UGCKitConstants.TCLIVE_INFO_POSITION, 0);
        startActivity(intent);
    }

    private void initWindowParam() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 返回主界面
     */
    private void backToMainActivity() {
        Intent intent = new Intent(TCVideoPublisherActivity.this, TCMainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoPublish.release();
    }
}
