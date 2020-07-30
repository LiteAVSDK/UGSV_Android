package com.tencent.qcloud.ugckit.module.effect.bgm;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.utils.DialogUtil;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.effect.bgm.view.IEditMusicPannel;
import com.tencent.qcloud.ugckit.module.record.MusicInfo;
import com.tencent.qcloud.ugckit.module.effect.bgm.view.TCEditMusicPannel;
import com.tencent.qcloud.ugckit.module.effect.utils.DraftEditer;

import com.tencent.ugc.TXVideoEditer;

import java.io.IOException;

/**
 * 音乐设置的fragment
 */
public class TCMusicSettingFragment extends Fragment {
    private static final String TAG = "TCMusicSettingFragment";

    private DraftEditer        mEditerDraft;
    private TCEditMusicPannel  mTCEditMusicPannel;
    private MusicInfo          mMusicInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditerDraft = DraftEditer.getInstance();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMusicInfo = DraftEditer.getInstance().loadMusicInfo();
        if (TextUtils.isEmpty(mMusicInfo.path)) {
            chooseBGM();
            return;
        }
        mTCEditMusicPannel.setMusicInfo(mMusicInfo);
    }

    private void chooseBGM() {
        Intent bgmIntent = new Intent(getActivity(), TCMusicActivity.class);
        bgmIntent.putExtra(UGCKitConstants.MUSIC_POSITION, mMusicInfo.position);
        startActivityForResult(bgmIntent, UGCKitConstants.ACTIVITY_MUSIC_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            getActivity().finish();
            return;
        }
        mMusicInfo = new MusicInfo();
        mMusicInfo.path = data.getStringExtra(UGCKitConstants.MUSIC_PATH);
        mMusicInfo.name = data.getStringExtra(UGCKitConstants.MUSIC_NAME);
        mMusicInfo.position = data.getIntExtra(UGCKitConstants.MUSIC_POSITION, -1);

        if (TextUtils.isEmpty(mMusicInfo.path)) {
            getActivity().finish();
            return;
        }
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        int result = editer.setBGM(mMusicInfo.path);
        if (result != 0) {
            DialogUtil.showDialog(getContext(), getResources().getString(R.string.ugckit_bgm_setting_fragment_video_edit_failed), getResources().getString(R.string.ugckit_bgm_setting_fragment_background_sound_only_supports_mp3_or_m4a_format), null);
        }
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(mMusicInfo.path);
            mediaPlayer.prepare();
            mMusicInfo.duration = mediaPlayer.getDuration();
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        editer.setBGMStartTime(0, mMusicInfo.duration);
        editer.setBGMVolume(0.5f);
        editer.setVideoVolume(0.5f);

        DraftEditer.getInstance().saveRecordMusicInfo(mMusicInfo);

        mMusicInfo.videoVolume = 0.5f;
        mMusicInfo.bgmVolume = 0.5f;
        mMusicInfo.startTime = 0;
        mMusicInfo.endTime = mMusicInfo.duration;

        mTCEditMusicPannel.setMusicInfo(mMusicInfo);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ugckit_fragment_bgm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMusicPanel(view);
    }

    /**
     * ==============================================音乐列表相关==============================================
     */
    private void initMusicPanel(@NonNull View view) {
        mTCEditMusicPannel = (TCEditMusicPannel) view.findViewById(R.id.tc_record_bgm_pannel);
        mTCEditMusicPannel.setOnMusicChangeListener(new IEditMusicPannel.MusicChangeListener() {
            @Override
            public void onMicVolumeChanged(float volume) {
                mEditerDraft.setVideoVolume(volume);

                TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
                editer.setVideoVolume(volume);
            }

            @Override
            public void onMusicVolumChanged(float volume) {
                mEditerDraft.setBgmVolume(volume);

                TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
                editer.setBGMVolume(volume);
            }

            @Override
            public void onMusicTimeChanged(long startTime, long endTime) {
                mEditerDraft.setBgmStartTime(startTime);
                mEditerDraft.setBgmEndTime(endTime);

                // bgm 播放时间区间设置
                VideoEditerSDK.getInstance().getEditer().setBGMStartTime(startTime, endTime);
            }

            @Override
            public void onMusicReplace() {
                chooseBGM();
            }

            @Override
            public void onMusicDelete() {
                mEditerDraft.setBgmPath(null);

                TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
                editer.setBGM(null);
            }
        });
    }

}
