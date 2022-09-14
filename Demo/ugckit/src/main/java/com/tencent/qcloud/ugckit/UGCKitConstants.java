package com.tencent.qcloud.ugckit;

public class UGCKitConstants {

    public static final String APP_SVR_URL                     = "http://demo.vod2.myqcloud.com/lite"; // 业务Server的地址 如果您的服务器没有部署https证书，这里需要用http
    public static final String SVR_BGM_GET_URL                 = "https://liteav.sdk.qcloud.com/app/res/bgm/bgm_list.json";   // BGM列表地址
    public static final String BUGLY_APPID                     = "9018b79ae2";    // 小视频Bugly
    public static final String USER_ID                         = "userid";
    public static final String USER_PWD                        = "userpwd";
    public static final String COVER_PIC                       = "cover_pic";
    public static final String PLAY_URL                        = "play_url";
    public static final String PUSHER_AVATAR                   = "pusher_avatar";
    public static final String PUSHER_ID                       = "pusher_id";
    public static final String PUSHER_NAME                     = "pusher_name";
    public static final String FILE_ID                         = "file_id";
    public static final String TIMESTAMP                       = "timestamp";
    public static final String ACTIVITY_RESULT                 = "activity_result";
    public static final String TCLIVE_INFO_LIST                = "txlive_info_list";
    public static final String TCLIVE_INFO_POSITION            = "txlive_info_position";
    public static final String VIDEO_PATH                      = "key_video_editer_path";
    public static final String VIDEO_COVERPATH                 = "coverpath";
    public static final String VIDEO_URI                       = "key_video_editer_uri_path";
    public static final String VIDEO_RECORD_NO_CACHE           = "nocache";
    public static final String VIDEO_RECORD_DURATION           = "duration";
    public static final String VIDEO_RECORD_RESOLUTION         = "resolution";
    public static final String OUTPUT_DIR_NAME                 = "TXUGC";
    public static final String INTENT_KEY_MULTI_PIC_LIST       = "pic_list"; // 图片列表
    public static final String INTENT_KEY_MULTI_CHOOSE         = "multi_video";
    public static final String DEFAULT_MEDIA_PACK_FOLDER       = "txrtmp";      // UGC编辑器输出目录
    public static final int    ACTIVITY_MUSIC_REQUEST_CODE     = 1;    // bgm activity request code and intent extra
    public static final String MUSIC_POSITION                  = "bgm_position";
    public static final String MUSIC_PATH                      = "bgm_path";
    public static final String MUSIC_NAME                      = "bgm_name";
    public static final int    ACTIVITY_OTHER_REQUEST_CODE     = 2;
    public static final String KEY_FRAGMENT                    = "fragment_type";
    public static final int    TYPE_EDITER_BGM                 = 1;
    public static final int    TYPE_EDITER_MOTION              = 2;
    public static final int    TYPE_EDITER_SPEED               = 3;
    public static final int    TYPE_EDITER_FILTER              = 4;
    public static final int    TYPE_EDITER_PASTER              = 5;
    public static final int    TYPE_EDITER_SUBTITLE            = 6;
    public static final int    TYPE_EDITER_TRANSITION          = 7;
    public static final String CHORUS_URL                      = "http://1400100725.vod2.myqcloud.com/8b7d5993vodgzp1400100725/d864a3545285890780576877210/ss2W2I8oIn4A.mp4";   // 合唱演示视频地址
    /**
     * 暂时关闭暂留画面，等IOS上线该功能后再一起打开 kongdywang 2021/08/31/
     */
    public static final int    DEFAULT_PAUSE_SNAP_OPACITY      = 0;
    // EventBus Msg
    public static final int    EVENT_MSG_PUBLISH_DONE          = 1; // 上传视频成功
    // SP record draft录制草稿
    public static final String SP_NAME_RECORD                  = "record";
    public static final String SP_KEY_RECORD_LAST_DRAFT        = "record_last_draft";
    public static final String RECORD_CONFIG_MAX_DURATION      = "record_config_max_duration";
    public static final String RECORD_CONFIG_MIN_DURATION      = "record_config_min_duration";
    public static final String RECORD_CONFIG_ASPECT_RATIO      = "record_config_aspect_ratio";
    public static final String RECORD_CONFIG_RECOMMEND_QUALITY = "record_config_recommend_quality";
    public static final String RECORD_CONFIG_HOME_ORIENTATION  = "record_config_home_orientation";
    public static final String RECORD_CONFIG_RESOLUTION        = "record_config_resolution";
    public static final String RECORD_CONFIG_BITE_RATE         = "record_config_bite_rate";
    public static final String RECORD_CONFIG_FPS               = "record_config_fps";
    public static final String RECORD_CONFIG_GOP               = "record_config_gop";
    public static final String RECORD_CONFIG_NEED_EDITER       = "record_config_go_editer";
    public static final String RECORD_CONFIG_TOUCH_FOCUS       = "record_config_touch_focus";
    public static final String ERROR_MSG_NET_DISCONNECTED      = "网络异常，请检查网络";
    public static final String PLAYER_DEFAULT_VIDEO            = "play_default_video";
}
