package com.tencent.qcloud.xiaoshipin.mainui.list;

import org.json.JSONObject;

import java.io.Serializable;

public class TCVideoInfo implements Serializable {
    public int review_status;
    public String userid;
    public String groupid;
    public int viewerCount;
    public int likeCount;
    public String title;
    public String playurl;
    public String fileid;
    public String nickname;
    public String headpic;
    public String frontcover;
    public String location;
    public String avatar;
    public String createTime;
    public String startTime;
    public String hlsPlayUrl;

    public final static int REVIEW_STATUS_NOT_REVIEW = 0;
    public final static int REVIEW_STATUS_NORMAL = 1;
    public final static int REVIEW_STATUS_PORN = 2;

    public TCVideoInfo() {
    }

    public TCVideoInfo(JSONObject data) {
        try {
            if (data.has("review_status")) {
                // 如果后台接入了鉴黄功能，需要根据状态来判断要不要播放
                this.review_status = data.optInt("review_status");
            } else {
                // 如果后台没有接入鉴黄功能，视频可以正常播放
                this.review_status = REVIEW_STATUS_NORMAL;
            }
            this.userid = data.optString("userid");
            this.nickname = data.optString("nickname");
            this.avatar = data.optString("avatar");
            this.fileid = data.optString("file_id");
            this.title = data.optString("title");
            this.frontcover = data.optString("frontcover");
            this.location = data.optString("location");
            this.playurl = data.optString("play_url");
            this.hlsPlayUrl = data.optString("hls_play_url");
            this.createTime = data.optString("create_time");
            this.likeCount = data.optInt("like_count");
            this.viewerCount = data.optInt("viewer_count");
            this.startTime = data.optString("start_time");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
