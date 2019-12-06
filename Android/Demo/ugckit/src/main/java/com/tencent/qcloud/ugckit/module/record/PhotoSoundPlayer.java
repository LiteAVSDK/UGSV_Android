package com.tencent.qcloud.ugckit.module.record;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.tencent.qcloud.ugckit.UGCKit;

public class PhotoSoundPlayer {
    /**
     * 播放拍照"咔"的声音
     */
    public static void playPhotoSound() {
        AudioManager meng = (AudioManager) UGCKit.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        MediaPlayer mediaPlayer = null;
        if (volume != 0) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(UGCKit.getAppContext(), Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            }
            mediaPlayer.start();
        }
    }
}
