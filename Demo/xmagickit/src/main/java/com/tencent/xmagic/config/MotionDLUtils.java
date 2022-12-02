package com.tencent.xmagic.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.xmagic.download.MotionDLModel;
import com.tencent.xmagic.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 需要下载的资源信息
 */

public class MotionDLUtils {

    private static MotionModel model = null;


    private static void readMotions(Context context) {
        if (model == null) {
            String motionsFileName = "";
            AssetManager assetManager = context.getAssets();
            try {
                String[] names = assetManager.list("json");
                if (names != null && names.length > 0) {
                    Arrays.sort(names);   //进行升序排列
                    for (String name : names) {
                        motionsFileName = name;
                    }
                }
            } catch (Exception e) {
                motionsFileName = "";
            }
            String xmagicMotionsStrJson = "{}";
            if (!TextUtils.isEmpty(motionsFileName)) {
                xmagicMotionsStrJson = FileUtil.readAssetFile(context, "json/" + motionsFileName);
            }
            model = new Gson().fromJson(xmagicMotionsStrJson.trim(), MotionModel.class);
        }
    }

    public static List<MotionDLModel> getMotionsByType(Context context, MotionType type) {
        readMotions(context);
        switch (type) {
            case MotionRes2D:
                return new ArrayList<>(addModelList("2dMotionRes", model.motion2d));
            case MotionRes3D:
                return new ArrayList<>(addModelList("3dMotionRes", model.motion3d));
            case MotionResHand:
                return new ArrayList<>(addModelList("handMotionRes", model.motionhand));
            case MotionResGan:
                return new ArrayList<>(addModelList("ganMotionRes", model.motiongan));
            case MotionResMakeup:
                return new ArrayList<>(addModelList("makeupRes", model.motionmakeup));
            case MotionResSegment:
                return new ArrayList<>(addModelList("segmentMotionRes", model.motionseg));
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 用于构造下载对象
     *
     * @param category
     * @param motionList
     * @return
     */
    private static List<MotionDLModel> addModelList(String category, List<String> motionList) {
        String motionResDownloadPrefix = model.motionsBaseUrl;
        List<MotionDLModel> list = new ArrayList<>();
        if (motionList != null) {
            for (int i = 0; i < motionList.size(); i++) {
                String url = motionResDownloadPrefix + motionList.get(i) + ".zip";
                list.add(new MotionDLModel(category, motionList.get(i), url));
            }
        }
        return list;
    }

    public static String getMotionNameByType(MotionType motionType) {
        switch (motionType) {
            case MotionRes2D:
                return "2dMotionRes";
            case MotionRes3D:
                return "3dMotionRes";
            case MotionResHand:
                return "handMotionRes";
            case MotionResGan:
                return "ganMotionRes";
            case MotionResMakeup:
                return "makeupRes";
            case MotionResSegment:
                return "segmentMotionRes";
            default:
                return "";
        }
    }


    public static String getIconUrlByName(Context context, String resourceName) {
        readMotions(context);
        String iconUrlBase = model.motionIconBaseUrl;
        if (!TextUtils.isEmpty(resourceName)) {
            switch (resourceName) {
                case "video_empty_segmentation":
                    return iconUrlBase + "add.png";
                case "video_segmentation_transparent_bg":
                case "video_segmentation_blur_45":
                case "video_segmentation_blur_75":
                    return iconUrlBase + "segment_all.png";
                case "video_bubblegum":
                    return iconUrlBase + "gan_video_bubbkegum.png";
                case "video_fenfenxia":
                    return iconUrlBase + "makeup_video_fenfenxia.png";
                case "video_shaishangzhuang":
                    return iconUrlBase + "makeup_video_shaishangzhuang.png";
                case "video_sakuragirl":
                    return iconUrlBase + "hand_video_sakuragirl.png";
                case "video_zhixingmeigui":
                    return iconUrlBase + "3d_video_zhixingmeigui.png";
                case "video_keaituya":
                    return iconUrlBase + "2d_video_keaituya.png";
                case "video_tutujiang":
                    return iconUrlBase + "2d_video_tutujiang.png";
                default:
                    return iconUrlBase + resourceName + ".png";
            }
        }
        return null;
    }


    static class MotionModel {
        public String motionsBaseUrl;
        public String motionIconBaseUrl;
        public List<String> motion2d;
        public List<String> motion3d;
        public List<String> motionhand;
        public List<String> motiongan;
        public List<String> motionmakeup;
        public List<String> motionseg;

    }
}
