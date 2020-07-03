package com.tencent.qcloud.ugckit.module.record.interfaces;

import android.support.annotation.ColorRes;

import com.tencent.ugc.TXRecordCommon;

/**
 * 音效面板定制化UI
 */

public interface ISoundEffectsPannel {

    /**
     * 设置音效面板监听器
     *
     * @param listener
     */
    void setSoundEffectsSettingPannelListener(SoundEffectsSettingPannelListener listener);

    interface SoundEffectsSettingPannelListener {
        /**
         * 录音音量改变
         *
         * @param volume
         */
        void onMicVolumeChanged(float volume);

        /**
         * 切换变声类型 [精简版不支持]
         *
         * @param type {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_0}(关闭变声)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_1}(熊孩子)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_2}(萝莉)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_3}(大叔)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_4}(重金属)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_6}(外国人)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_7}(困兽)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_8}(死肥仔)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_9}(强电流)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_10}(重机械)</br>
         *             {@link TXRecordCommon#VIDOE_VOICECHANGER_TYPE_11}(空灵)</br>
         */
        void onClickVoiceChanger(int type);

        /**
         * 切换混响类型 [精简版不支持]
         *
         * @param type {@link TXRecordCommon#VIDOE_REVERB_TYPE_0}(关闭混响)</br>
         *             {@link TXRecordCommon#VIDOE_REVERB_TYPE_1}(KTV)</br>
         *             {@link TXRecordCommon#VIDOE_REVERB_TYPE_2}(小房间)</br>
         *             {@link TXRecordCommon#VIDOE_REVERB_TYPE_3}(大会堂)</br>
         *             {@link TXRecordCommon#VIDOE_REVERB_TYPE_4}(低沉)</br>
         *             {@link TXRecordCommon#VIDOE_REVERB_TYPE_5}(洪亮)</br>
         *             {@link TXRecordCommon#VIDOE_REVERB_TYPE_6}(金属声)</br>
         *             {@link TXRecordCommon#VIDOE_REVERB_TYPE_7}(磁性)</br>
         */
        void onClickReverb(int type);
    }

    /**
     * Seekbar颜色
     */
    void setSeekbarColor(@ColorRes int color);

    /**
     * 选中文字颜色
     */
    void setCheckedTextColor(@ColorRes int color);

    /**
     * 普通状态文字颜色
     */
    void setNormalTextColor(@ColorRes int color);

    /**
     * 设置"确定"按钮背景颜色
     */
    void setConfirmButtonBackgroundColor(@ColorRes int color);

    /**
     * 设置"确定"按钮文字颜色
     */
    void setConfirmButtonTextColor(@ColorRes int color);

    /**
     * 设置"确定"按钮文字字体大小
     */
    void setConfirmButtonTextSize(int size);
}
