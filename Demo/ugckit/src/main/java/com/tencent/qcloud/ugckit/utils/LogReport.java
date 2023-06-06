package com.tencent.qcloud.ugckit.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.upload.TXUGCPublishTypeDef;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXVideoEditConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 日志上报模块
 */
public class LogReport {
    private static final String       TAG                            = "LogReport";
    // ELK统计上报HOST
    public static final  String       DEFAULT_ELK_HOST               = "https://qcloud.com";
    /******************************
     * ELK统计上报事件
     ******************************/
    // 程序启动
    public static final  String       ELK_ACTION_START_UP            = "startup";
    // 程序使用时长（秒）
    public static final  String       ELK_ACTION_STAY_TIME           = "staytime";
    // 图片编辑
    public static final  String       ELK_ACTION_PICTURE_EDIT        = "pictureedit";
    // 注册
    public static final  String       ELK_ACTION_REGISTER            = "register";
    // 安装
    public static final  String       ELK_ACTION_INSTALL             = "install";
    // 登录
    public static final  String       ELK_ACTION_LOGIN               = "login";
    // 视频编辑
    public static final  String       ELK_ACTION_VIDEO_EDIT          = "videoedit";
    // 视频合成
    public static final  String       ELK_ACTION_VIDEO_JOINER        = "videojoiner";
    // 获取视频签名（发布视频前的准备工作）
    public static final  String       ELK_ACTION_VIDEO_SIGN          = "videosign";
    // 发布视频到点播系统
    public static final  String       ELK_ACTION_VIDEO_UPLOAD_VOD    = "videouploadvod";
    // 发布视频到服务器
    public static final  String       ELK_ACTION_VIDEO_UPLOAD_SERVER = "videouploadserver";
    // 开始录制
    public static final  String       ELK_ACTION_START_RECORD        = "startrecord";
    // 停止录制
    public static final  String       ELK_ACTION_VIDEO_RECORD        = "videorecord";
    // 点播视频
    public static final  String       ELK_ACTION_VOD_PLAY            = "vodplay";
    // 合唱
    public static final  String       ELK_ACTION_VIDEO_CHORUS        = "videochorus";
    private              OkHttpClient mHttpClient;
    private              String       mUserId;

    private LogReport() {
        mHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void setUserId(String userid) {
        mUserId = userid;
    }

    /**
     * ELK上报：开始录制
     *
     * @param retCode
     */
    public void reportStartRecord(int retCode) {
        String desc = null;
        switch (retCode) {
            case TXRecordCommon.START_RECORD_OK:
                desc = UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_start_record_start_record_ok);
                break;
            case TXRecordCommon.START_RECORD_ERR_IS_IN_RECORDING:
                desc = UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_start_record_start_record_err_is_in_recording);
                break;
            case TXRecordCommon.START_RECORD_ERR_VIDEO_PATH_IS_EMPTY:
                desc = UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_start_record_start_record_err_video_path_is_empty);
                break;
            case TXRecordCommon.START_RECORD_ERR_API_IS_LOWER_THAN_18:
                desc = UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_start_record_start_record_err_api_is_lower_than_18);
                break;
            case TXRecordCommon.START_RECORD_ERR_NOT_INIT:
                desc = UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_start_record_start_record_err_not_init);
                break;
            case TXRecordCommon.START_RECORD_ERR_LICENCE_VERIFICATION_FAILED:
                desc = UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_start_record_start_record_err_licence_verification_failed);
                break;
        }
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_START_RECORD, retCode, desc);
    }

    /**
     * ELK上报：视频编辑
     *
     * @param retCode
     */
    public void reportVideoEdit(int retCode) {
        String desc = null;
        switch (retCode) {
            case TXVideoEditConstants.GENERATE_RESULT_OK:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_edit_generate_result_ok);
                break;
            case TXVideoEditConstants.GENERATE_RESULT_FAILED:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_edit_generate_result_failed);
                break;
            case TXVideoEditConstants.GENERATE_RESULT_LICENCE_VERIFICATION_FAILED:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_edit_licence_verification_failed);
                break;
        }
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_EDIT, retCode, desc);
    }

    /**
     * ELK上报：首次安装成功
     */
    public void reportInstall() {
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_INSTALL, 0,
                UGCKit.getAppContext().getString(R.string.ugckit_first_install_failed));
    }

    /**
     * ELK上报：合唱
     */
    public void reportChorus() {
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_CHORUS, 0,
                UGCKit.getAppContext().getString(R.string.ugckit_chorus_event));
    }

    /**
     * ELK上报：视频合成
     */
    public void reportVideoJoin(int retCode) {
        String desc = null;
        switch (retCode) {
            case TXVideoEditConstants.JOIN_RESULT_OK:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_join_result_ok);
                break;
            case TXVideoEditConstants.JOIN_RESULT_FAILED:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_join_result_failed);
                break;
            case TXVideoEditConstants.GENERATE_RESULT_LICENCE_VERIFICATION_FAILED:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_join_licence_verification_failed);
                break;
            default:
                desc = "";
                break;
        }
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_JOINER, retCode, desc);
    }

    /**
     * ELK上报：视频点播
     *
     * @param retCode
     */
    public void reportVodPlayFail(int retCode) {
        String desc = null;
        switch (retCode) {
            case TXLiveConstants.PLAY_ERR_GET_RTMP_ACC_URL_FAIL:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_play_failed_code_get_rtmp_acc_url);
                break;
            case TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_play_failed_code_file_not_found);
                break;
            case TXLiveConstants.PLAY_ERR_HEVC_DECODE_FAIL:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_play_failed_code_hevc_decode_failed);
                break;
            case TXLiveConstants.PLAY_ERR_HLS_KEY:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_play_failed_code_hls_key_error);
                break;
            case TXLiveConstants.PLAY_ERR_GET_PLAYINFO_FAIL:
                desc = UGCKit.getAppContext().getString(R.string.ugckit_video_play_failed_code_get_play_info);
                break;
            default:
                desc = "";
                break;
        }
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VOD_PLAY, retCode, desc);
    }

    public void reportVodPlaySucc(int retCode) {
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VOD_PLAY, retCode,
                UGCKit.getAppContext().getString(R.string.ugckit_video_play_success_des), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                    }
                });
    }

    public void reportPublishVideo(@NonNull TXUGCPublishTypeDef.TXPublishResult result) {
        String desc;
        if (result.retCode == TXUGCPublishTypeDef.PUBLISH_RESULT_OK) {
            desc = UGCKit.getAppContext().getString(R.string.ugckit_video_publish_success);
        } else {
            desc = UGCKit.getAppContext().getString(R.string.ugckit_video_publish_failed) + result.descMsg;
        }
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_UPLOAD_VOD, result.retCode, desc);
    }

    private static class LogMgrHolder {
        @NonNull
        private static LogReport instance = new LogReport();
    }

    @NonNull
    public static LogReport getInstance() {
        return LogMgrHolder.instance;
    }

    public void uploadLogs(String action, long code, String errorMsg) {
        String userName = mUserId;
        Log.i(TAG, "uploadLogs action:" + action + ",userName:" + userName + ",code:" + code + ",errorMsg:" + errorMsg);
        uploadLogs(action, code, errorMsg, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public void uploadLogs(String action, long code, String errorMsg, okhttp3.Callback callback) {
        Log.w(TAG, "uploadLogs: errorMsg " + errorMsg);
        String reqUrl = DEFAULT_ELK_HOST;
        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", action);
            jsonObject.put("action_result_code", code);
            jsonObject.put("action_result_msg", errorMsg);
            jsonObject.put("type", "xiaoshipin");
            jsonObject.put("bussiness", "ugckit");
            jsonObject.put("appid", UGCKit.getAppContext().getPackageName());
            jsonObject.put("platform", "android");
            body = jsonObject.toString();
            Log.d(TAG, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(reqUrl)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body))
                .build();
        mHttpClient.newCall(request).enqueue(callback);
    }
}
