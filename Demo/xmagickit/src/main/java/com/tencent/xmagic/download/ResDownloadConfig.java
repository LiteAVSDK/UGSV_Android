package com.tencent.xmagic.download;

import java.util.ArrayList;
import java.util.List;

public class ResDownloadConfig {
    private static final String MOTION_RES_DOWNLOAD_PREFIX =
            "https://mediacloud-76607.gzc.vod.tencent-cloud.com/TencentEffect/demoMotion/";

    //Motion动效的一个压缩包是一个动效，下载时逐个下载
    //压缩时把一个动效文件夹压缩成zip即可
    public static List<MotionDLModel> getMotionList() {
        List<MotionDLModel> motionList = new ArrayList<>();
        motionList.addAll(addModelList("2dMotionRes", MotionRes2D));
        motionList.addAll(addModelList("3dMotionRes", MotionRes3D));
        motionList.addAll(addModelList("handMotionRes", MotionResHand));
        motionList.addAll(addModelList("makeupRes", MotionResMakeup));
        motionList.addAll(addModelList("segmentMotionRes", MotionResSegment));
        motionList.addAll(addModelList("ganMotionRes", MotionResGan));
        return motionList;
    }

    private static List<MotionDLModel> addModelList(String category, String[] motionList) {
        List<MotionDLModel> list = new ArrayList<>();
        for (int i = 0; i < motionList.length; i++) {
            list.add(new MotionDLModel(category, motionList[i], MOTION_RES_DOWNLOAD_PREFIX + motionList[i] + ".zip"));
        }
        return list;
    }

    private static final String[] MotionResGan = new String[]{
            "video_bubblegum"
    };

    private static final String[] MotionResSegment = new String[]{
            "video_empty_segmentation",
            "video_segmentation_transparent_bg",
            "video_segmentation_blur_45",
            "video_segmentation_blur_75"
    };

    private static final String[] MotionResMakeup = new String[]{
            "video_fenfenxia",
            "video_shaishangzhuang",
    };

    private static final String[] MotionResHand = new String[]{
            "video_sakuragirl"
    };

    private static final String[] MotionRes3D = new String[]{
            "video_zhixingmeigui",
    };

    private static final String[] MotionRes2D = new String[]{
            "video_keaituya",   //
            "video_tutujiang",   //

    };


}
