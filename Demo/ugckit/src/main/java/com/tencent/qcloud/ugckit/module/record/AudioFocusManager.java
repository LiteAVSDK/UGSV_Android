package com.tencent.qcloud.ugckit.module.record;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * 用于帮助处理应用程序的音频焦点的获取与丢失。
 */
public class AudioFocusManager {


    private AudioManager mAudioManager;

    private OnAudioFocusChangeListener mListener;

    private AudioFocusRequest mAudioFocusRequest;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;

    private boolean mLossTransient;
    private boolean mLossTransientCanDuck;

    private boolean mRequested;

    /**
     * 创建一个 {@link AudioFocusManager} 对象。
     *
     * @param context  Context 对象，不能为 null
     * @param listener 事件监听器，不能为 null
     */
    public AudioFocusManager(@NonNull Context context,
                             @NonNull OnAudioFocusChangeListener listener) {

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mListener = listener;
        initAudioFocusChangeListener();
    }


    private void initAudioFocusChangeListener() {
        mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        mListener.onLossFocus();
                        mLossTransient = false;
                        mLossTransientCanDuck = false;
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        mLossTransient = true;
                        mListener.onLossFocus();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        mLossTransientCanDuck = true;
                        mListener.onLossFocus();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        mListener.onGain(mLossTransient, mLossTransientCanDuck);
                        mLossTransient = false;
                        mLossTransientCanDuck = false;
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 获取音频焦点。
     *
     * @param streamType   受焦点请求影响的主要音频流类型。该参数通常是 <code>AudioManager.STREAM_MUSIC。更多音频
     *                     流类型，请查看 https://developer.android.google.cn/reference/android/media/AudioManager
     * @param durationHint 可以是以下 4 个值之一：
     *                     AudioManager.AUDIOFOCUS_GAIN：表示获取未知时长的音频焦点；
     *                     AudioManager.AUDIOFOCUS_GAIN_TRANSIENT ：表示短暂的获取音频焦点；
     *                     AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK：
     *                     表示短暂的获取音频焦点，同时指示先前的焦点所有者可以通过降低音量（duck），并继续播放；
     *                     AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE ：
     *                     （API level 19）：表示短暂获取音频焦点，在此期间，其他任何应用程序或系统组件均不应播放任何内容。
     *                     更多内容，https://developer.android.google.cn/reference/android/media/AudioManager
     * @return AudioManager.AUDIOFOCUS_REQUEST_GRANTED（申请成功）或者 AudioManager.AUDIOFOCUS_REQUEST_FAILED（申请失败）
     */
    public int requestAudioFocus(int streamType, int durationHint) {
        if (mAudioManager == null) {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }

        mRequested = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return requestAudioFocusAPI26(streamType, durationHint);
        }

        return mAudioManager.requestAudioFocus(
                mAudioFocusChangeListener,
                streamType,
                durationHint);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private int requestAudioFocusAPI26(int streamType, int durationHint) {
        if (mAudioManager == null) {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }

        mAudioFocusRequest = new AudioFocusRequest.Builder(durationHint)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setLegacyStreamType(streamType)
                        .build())
                .setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                .setWillPauseWhenDucked(true)
                .build();

        return mAudioManager.requestAudioFocus(mAudioFocusRequest);
    }

    /**
     * （主动）放弃音频焦点。
     */
    public void abandonAudioFocus() {
        if (mAudioManager == null || !mRequested) {
            return;
        }

        mRequested = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            abandonAudioFocusAPI26();
            return;
        }

        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void abandonAudioFocusAPI26() {
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
    }

    /**
     * 可用于监听当前应用程序的音频焦点的获取与丢失。
     */
    public interface OnAudioFocusChangeListener {
        void onLossFocus();

        /**
         * 重新获取到音频焦点。
         * 那么此时应恢复正常的音量。
         *
         * @param lossTransient        指示音频焦点是否是暂时性丢失，如果是，则此时可以恢复播放。
         * @param lossTransientCanDuck 指示音频焦点是否是可降低音量的暂时性丢失，如果是，则此时只需恢复音量即可。
         */
        void onGain(boolean lossTransient, boolean lossTransientCanDuck);
    }
}
