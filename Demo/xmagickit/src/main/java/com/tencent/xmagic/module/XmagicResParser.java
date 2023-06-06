package com.tencent.xmagic.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.xmagic.XmagicApi;
import com.tencent.xmagic.config.MotionDLUtils;
import com.tencent.xmagic.config.MotionType;
import com.tencent.xmagic.demo.R;
import com.tencent.xmagic.download.MotionDLModel;
import com.tencent.xmagic.module.XmagicUIProperty.UICategory;
import com.tencent.xmagic.panel.XmagicPanelDataManager;
import com.tencent.xmagic.XmagicConstant.BeautyConstant;
import com.tencent.xmagic.XmagicProperty;
import com.tencent.xmagic.XmagicProperty.XmagicPropertyValues;
import com.tencent.xmagic.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 美颜数据封装示例代码类。
 * 注意事项：
 * 本类数据构造默认会通过工程内所持有的原子能力资源来构造全量美颜能力与分类。使用者无需根据自己使用的套餐再做代码调整。
 * 不同套餐对应的原子能力明细如下：
 * <p>
 * A1-00
 * 基础美颜：美白、磨皮、红润
 * 滤镜特效
 * ***********************************************************************************
 * A1-01
 * 基础美颜：美白、磨皮、红润
 * 画面调整：对比度、饱和度、清晰度
 * 高级美颜(人像属性识别能力)：大眼、瘦脸（自然、女神、英俊）
 * 滤镜特效
 * ***********************************************************************************
 * A1-02
 * 涵盖A1-01所有功能
 * 2D特效能力、2D特效素材资源包
 * ***********************************************************************************
 * A1-03
 * 涵盖A1-01所有功能
 * 2D特效能力、2D特效素材资源包
 * 高级美颜(人像属性识别能力)：增加窄脸/下巴/发际线/瘦鼻
 * ***********************************************************************************
 * A1-04
 * 涵盖A1-01所有功能
 * 2D特效能力、2D特效素材资源包
 * 手势识别能力、手势特效素材资源包
 * ***********************************************************************************
 * A1-05
 * 涵盖A1-01所有功能
 * 2D特效能力、2D特效素材资源包
 * 人像分割所有能力、人像分割特效
 * ***********************************************************************************
 * A1-06
 * 涵盖A1-01所有功能
 * 2D特效能力、2D特效素材资源包
 * 美妆能力、美妆特效素材资源包
 * **********************************************************************************
 * S1-00
 * 基础美颜美颜：美白、磨皮、红润
 * 画面调整：对比度、饱和度、锐化
 * 高级美颜(人像属性识别能力)：大眼、窄脸、瘦脸（自然、女神、英俊）、V 脸、下巴、短脸、脸型、发际线、亮眼、
 * 眼距、眼角、瘦鼻、鼻翼、鼻子位置、白牙、祛皱、祛法令纹、祛眼袋、嘴型、嘴唇厚度、口红、腮红、立体
 * 滤镜特效
 * ***********************************************************************************
 * S1-01
 * 基础美颜美颜：美颜：美白、磨皮、红润
 * 画面调整：对比度、饱和度、锐化
 * 高级美颜(人像属性识别能力)：大眼、窄脸、瘦脸（自然、女神、英俊）、V 脸、下巴、短脸、脸型、发际线、亮眼、
 * 眼距、眼角、瘦鼻、鼻翼、鼻子位置、白牙、祛皱、祛法令纹、祛眼袋、嘴型、嘴唇厚度、口红、腮红、立体
 * 滤镜特效
 * 2D特效能力、2D特效素材资源包
 * 3D特效能力、3D特效素材资源包
 * 美妆能力、美妆特效素材资源包
 * ***********************************************************************************
 * S1-02
 * 涵盖S1-01所有功能
 * 手势识别能力、手势特效素材资源包
 * ***********************************************************************************
 * S1-03
 * 涵盖S1-01所有功能
 * 人像分割所有能力、人像分割特效素材资源包
 * ***********************************************************************************
 * S1-04
 * 涵盖S1-01所有功能
 * 手势识别能力、手势特效素材资源包
 * 人像分割所有能力、人像分割特效
 * <p>
 * <p>
 * Sample code class for beauty data encapsulation.
 * Precautions:
 * By default, this type of data structure will use the atomic power resources held in
 * the project to construct the full amount of beauty capabilities and classifications.
 * Users do not need to make code adjustments according to the packages they use.
 * The atomic capabilities corresponding to different packages are detailed as follows:
 * <p>
 * A1-00
 * Basic beauty: whitening, microdermabrasion, ruddy
 * Filter effects
 * ************************************************ ************************************
 * A1-01
 * Basic beauty: whitening, microdermabrasion, ruddy
 * Picture adjustment: contrast, saturation, sharpness
 * Advanced beauty (portrait attribute recognition ability): big eyes, thin face (natural, goddess, handsome)
 * Filter effects
 * ************************************************ ************************************
 * A1-02
 * Covers all functions of A1-01
 * 2D special effect capability, 2D special effect material resource package
 * ************************************************ ************************************
 * A1-03
 * Covers all functions of A1-01
 * 2D special effect capability, 2D special effect material resource package
 * Advanced beauty (portrait attribute recognition ability): increase narrow face/chin/hairline/slim nose
 * ************************************************ ************************************
 * A1-04
 * Covers all functions of A1-01
 * 2D special effect capability, 2D special effect material resource package
 * Gesture recognition ability, gesture special effect resource package
 * ************************************************ ************************************
 * A1-05
 * Covers all functions of A1-01
 * 2D special effect capability, 2D special effect material resource package
 * Portrait segmentation all abilities, portrait segmentation effects
 * ************************************************ ************************************
 * A1-06
 * Covers all functions of A1-01
 * 2D special effect capability, 2D special effect material resource package
 * Resource packs for beauty abilities and beauty effects
 * ************************************************ ********************************
 * S1-00
 * Basic beauty beauty: whitening, microdermabrasion, ruddy
 * Picture adjustment: contrast, saturation, sharpening
 * Advanced beauty (portrait attribute recognition ability): big eyes, narrow face,
 * thin face (natural, goddess, handsome), V face, chin, short face, face shape, hairline,
 * bright eyes, eye distance, eye corners, thin nose , nose wing, nose position, white teeth,
 * wrinkle removal, nasolabial wrinkle removal, eye bag removal, mouth shape, lip thickness,
 * lipstick, blush, three-dimensional
 * Filter effects
 * ************************************************ ************************************
 * S1-01
 * Basic beauty beauty: beauty: whitening, microdermabrasion, rosy
 * Picture adjustment: contrast, saturation, sharpening
 * Advanced beauty (portrait attribute recognition ability): big eyes, narrow face, thin face
 * (natural, goddess, handsome), V face, chin, short face, face shape, hairline, bright eyes, eye distance,
 * eye corners, thin nose , nose wing, nose position, white teeth, wrinkle removal, nasolabial wrinkle removal,
 * eye bag removal, mouth shape, lip thickness, lipstick, blush, three-dimensional
 * Filter effects
 * 2D special effect capability, 2D special effect material resource package
 * 3D special effect capability, 3D special effect material resource package
 * Resource packs for beauty abilities and beauty effects
 * ************************************************ ************************************
 * S1-02
 * Covers all functions of S1-01
 * Gesture recognition ability, gesture special effect resource package
 * ************************************************ ************************************
 * S1-03
 * Covers all functions of S1-01
 * All abilities of portrait segmentation, portrait segmentation special effects resource package
 * ************************************************ ************************************
 * S1-04
 * Covers all functions of S1-01
 * Gesture recognition ability, gesture special effect resource package
 * Portrait segmentation all abilities, portrait segmentation effects
 */
public class XmagicResParser {

    private static final String TAG = XmagicResParser.class.getSimpleName();

    /**
     * 约定以 "/" 结尾, 方便拼接
     * xmagic resource local path
     */
    private static String sResPath;


    /**
     * 直接使用此类的方法，需要注意使用顺序
     * 1. 调用setResPath（）设置存放资源的路径
     * 2. copyRes(Context context) 将asset中的资源文件复制到 第一步设置的路径下
     * 3. 调用parseRes()方法对资源进行分类处理
     * 4. 之后就可以使用XmagicPanelView和XmagicPanelDataManager.getInstance()类的方法
     * <p>
     * Direct use of such methods, need to pay attention to the order of use
     * 1. Call setResPath() to set the path for storing resources
     * 2. copyRes(Context context) Copy the resource file in the asset to the path set in the first step
     * 3. Call the parseRes() method to classify the resources
     * 4. Then you can use the methods of XmagicPanelView and XmagicPanelDataManager.getInstance() classes
     */
    private XmagicResParser() {/*nothing*/}

    /**
     * 设置asset 资源存放的位置
     * set the asset path
     *
     * @param path
     */
    public static void setResPath(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        sResPath = path;
    }

    public static String getResPath() {
        ensureResPathAlreadySet();
        return sResPath;
    }

    /**
     * 从 apk 的 assets 解压资源文件到指定路径, 需要先设置路径: {@link #setResPath(String)} <br>
     * 首次安装 App, 或 App 升级后调用一次即可.
     * copy xmagic resource from assets to local path
     */
    public static void copyRes(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("xmagic_settings", Context.MODE_PRIVATE);
        String savedVersion = sharedPreferences.getString("appVersionKey", "");
        String appVersionName = XmagicResParser.getAppVersionName(context);
        if (appVersionName.equals(savedVersion)) {
            return;
        }
        copyResource(context);
        sharedPreferences.edit().putString("appVersionKey", appVersionName).commit();
    }


    /**
     *
     * @param context
     * @return
     */
    private static boolean copyResource(Context context) {
        ensureResPathAlreadySet();
        int addResult = XmagicApi.addAiModeFilesFromAssets(context, sResPath);
        Log.e(TAG, "add ai model files result = " + addResult);
        for (String path : new String[]{"lut"}) {
            boolean result = FileUtil.copyAssets(context, path, sResPath + "light_material" + File.separator + path);
            if (!result) {
                Log.d(TAG, "copyRes: fail,path=" + path + ",new path=" + sResPath + "light_material"
                        + File.separator + path);
                return false;
            }
        }
        for (String path : new String[]{"MotionRes"}) {
            boolean result = FileUtil.copyAssets(context, path, sResPath + path);
            if (!result) {
                Log.d(TAG, "copyRes: fail,path=" + path + ",new path=" + sResPath + path);
                return false;
            }
        }
        return true;
    }

    /**
     * 对已经复制到本地的资源进行分类处理
     * Classify resources that have been copied to the local
     */
    public static void parseRes(Context context) {
        File file = new File(sResPath);
        String[] list = file.list();
        if (!file.exists() || list == null || list.length == 0) {
            throw new IllegalStateException("resource dir not found or empty, call XmagicResParser.copyRes first.");
        }

        List<XmagicUIProperty<?>> beautyList = new ArrayList<>();
        XmagicPanelDataManager.getInstance().addAllDataItem(UICategory.BEAUTY, beautyList);
        pareBeauty(context, beautyList);

//        List<XmagicUIProperty<?>> bodyBeautyList = new ArrayList<>();
//        XmagicPanelDataManager.getInstance().addAllDataItem(UICategory.BODY_BEAUTY, bodyBeautyList);
//        parseBodyBeauty(context, bodyBeautyList);

        List<XmagicUIProperty<?>> lutList = new ArrayList<>();
        XmagicPanelDataManager.getInstance().addAllDataItem(UICategory.LUT, lutList);
        parseLutProperty(context, lutList);

        List<XmagicUIProperty<?>> motionList = new ArrayList<>();
        XmagicPanelDataManager.getInstance().addAllDataItem(UICategory.MOTION, motionList);
        parseMotionData(context, motionList);

        List<XmagicUIProperty<?>> makeUpList = new ArrayList<>();
        XmagicPanelDataManager.getInstance().addAllDataItem(UICategory.MAKEUP, makeUpList);
        parseMakeUpData(context, makeUpList);

        List<XmagicUIProperty<?>> segList = new ArrayList<>();
        XmagicPanelDataManager.getInstance().addAllDataItem(UICategory.SEGMENTATION, segList);
        parseSegData(context, segList);

    }


    /**
     * 美颜数据构造
     * create beauty data
     */
    private static void pareBeauty(Context context, List<XmagicUIProperty<?>> beautyList) {
        String effDirs = "/images/beauty/";
        Map<String, String> effs = new HashMap<>();
        for (String effPath : new File(sResPath + "light_assets" + effDirs).list()) {
            effs.put(new File(effPath).getName(), effPath);
        }

        if (beautyList == null) {
            return;
        }
        XmagicUIProperty<?> beautyProperty = new XmagicUIProperty<>(UICategory.KV, null, 0, null, null);
        beautyList.add(beautyProperty);
        XmagicUIProperty<?> xmagicUIProperty = new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_whiten_label), R.mipmap.beauty_whiten,
                BeautyConstant.BEAUTY_WHITEN,
                new XmagicPropertyValues(0, 100, 30, 0, 1));
        XmagicPanelDataManager.getInstance().getSelectedItems()
                .put(xmagicUIProperty.uiCategory.getDescription(), xmagicUIProperty);
        beautyList.add(xmagicUIProperty);
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_smooth_label), R.mipmap.beauty_smooth, BeautyConstant.BEAUTY_SMOOTH,
                new XmagicPropertyValues(0, 100, 50, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_ruddy_label), R.mipmap.beauty_ruddy, BeautyConstant.BEAUTY_ROSY,
                new XmagicPropertyValues(0, 100, 20, 0, 2)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.image_contrast_label),
                R.mipmap.image_contrast, BeautyConstant.BEAUTY_CONTRAST,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.image_saturation_label),
                R.mipmap.image_saturation, BeautyConstant.BEAUTY_SATURATION,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.image_sharpen_label), R.mipmap.image_sharpen, BeautyConstant.BEAUTY_CLEAR,
                new XmagicPropertyValues(0, 100, 0, 0, 2)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_enlarge_eye_label), R.mipmap.beauty_enlarge_eye,
                BeautyConstant.BEAUTY_ENLARGE_EYE, new XmagicPropertyValues(0, 100, 20, 0, 1)));

        String thinFaceLabel = context.getString(R.string.beauty_thin_face_label);
        XmagicUIProperty thinFace = new XmagicUIProperty(
                thinFaceLabel, R.mipmap.beauty_thin_face, UICategory.BEAUTY);
        beautyList.add(thinFace);
        List<XmagicUIProperty<?>> slList = new ArrayList<>();
        slList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_thin_face1_label),
                "nature", null, R.mipmap.beauty_thin_face1, BeautyConstant.BEAUTY_FACE_NATURE,
                new XmagicPropertyValues(0, 100, 30, 0, 1), thinFaceLabel));
        slList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_thin_face2_label),
                "femaleGod", null, R.mipmap.beauty_thin_face2, BeautyConstant.BEAUTY_FACE_GODNESS,
                new XmagicPropertyValues(0, 100, 0, 0, 1), thinFaceLabel));
        slList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_thin_face3_label),
                "maleGod", null, R.mipmap.beauty_thin_face3, BeautyConstant.BEAUTY_FACE_MALE_GOD,
                new XmagicPropertyValues(0, 100, 0, 0, 1), thinFaceLabel));
        thinFace.xmagicUIPropertyList = slList;

        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_v_face_label),
                R.mipmap.beauty_v_face, BeautyConstant.BEAUTY_FACE_V,
                new XmagicPropertyValues(0, 100, 30, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_narrow_face_label),
                R.mipmap.beauty_narrow_face, BeautyConstant.BEAUTY_FACE_THIN,
                new XmagicPropertyValues(0, 100, 0, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_short_face_label),
                R.mipmap.beauty_short_face, BeautyConstant.BEAUTY_FACE_SHORT,
                new XmagicPropertyValues(0, 100, 0, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_basic_face_label),
                R.mipmap.beauty_basic_face, BeautyConstant.BEAUTY_FACE_BASIC,
                new XmagicPropertyValues(0, 100, 0, 0, 1)));

        Map<String, String> lipsResPathNames = new LinkedHashMap<>();
        lipsResPathNames.put("lips_fuguhong.png", context.getString(R.string.beauty_lips1_label));
        lipsResPathNames.put("lips_mitaose.png", context.getString(R.string.beauty_lips2_label));
        lipsResPathNames.put("lips_shanhuju.png", context.getString(R.string.beauty_lips3_label));
        lipsResPathNames.put("lips_wenroufen.png", context.getString(R.string.beauty_lips4_label));
        lipsResPathNames.put("lips_huolicheng.png", context.getString(R.string.beauty_lips5_label));
        List<XmagicUIProperty<?>> itemLipsPropertys = new ArrayList<>();
        String lipId = "beauty.lips.lipsMask";
        String lipLabel = context.getString(R.string.beauty_lips_label);
        for (String ids : lipsResPathNames.keySet()) {
            itemLipsPropertys.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                    lipsResPathNames.get(ids), lipId, effDirs + effs.get(ids),
                    R.mipmap.beauty_lips, BeautyConstant.BEAUTY_MOUTH_LIPSTICK,
                    new XmagicPropertyValues(0, 100, 50, 0, 1),
                    lipLabel));
        }
        XmagicUIProperty itemLips = new XmagicUIProperty<>(
                lipLabel,
                R.mipmap.beauty_lips, UICategory.BEAUTY);
        itemLips.xmagicUIPropertyList = itemLipsPropertys;
        beautyList.add(itemLips);

        Map<String, String> redcheeksResPathNames = new LinkedHashMap<>();
        redcheeksResPathNames.put("saihong_jianyue.png", context.getString(R.string.beauty_redcheeks1_label));
        redcheeksResPathNames.put("saihong_shengxia.png", context.getString(R.string.beauty_redcheeks2_label));
        redcheeksResPathNames.put("saihong_haixiu.png", context.getString(R.string.beauty_redcheeks3_label));
        redcheeksResPathNames.put("saihong_chengshu.png", context.getString(R.string.beauty_redcheeks4_label));
        redcheeksResPathNames.put("saihong_queban.png", context.getString(R.string.beauty_redcheeks5_label));

        List<XmagicUIProperty<?>> itemRedcheekPropertys = new ArrayList<>();
        String redcheekId = "beauty.makeupMultiply.multiplyMask";
        String redcheeksLabel = context.getString(R.string.beauty_redcheeks_label);
        for (String ids : redcheeksResPathNames.keySet()) {
            itemRedcheekPropertys.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                    redcheeksResPathNames.get(ids), redcheekId,
                    effDirs + effs.get(ids), R.mipmap.beauty_redcheeks,
                    BeautyConstant.BEAUTY_FACE_RED_CHEEK,
                    new XmagicPropertyValues(0, 100, 50, 0, 1),
                    redcheeksLabel));
        }
        XmagicUIProperty itemRedcheeks = new XmagicUIProperty<>(
                redcheeksLabel, R.mipmap.beauty_redcheeks, UICategory.BEAUTY);
        itemRedcheeks.xmagicUIPropertyList = itemRedcheekPropertys;
        beautyList.add(itemRedcheeks);

        Map<String, String> litisResPathNames = new LinkedHashMap<>();
        litisResPathNames.put("liti_ziran.png", context.getString(R.string.beauty_liti1_label));
        litisResPathNames.put("liti_junlang.png", context.getString(R.string.beauty_liti2_label));
        litisResPathNames.put("liti_guangmang.png", context.getString(R.string.beauty_liti3_label));
        litisResPathNames.put("liti_qingxin.png", context.getString(R.string.beauty_liti4_label));

        List<XmagicUIProperty<?>> liTiItems = new ArrayList<>();
        String liTiLabel = context.getString(R.string.beauty_liti_label);
        for (String ids : litisResPathNames.keySet()) {
            liTiItems.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                    litisResPathNames.get(ids), "beauty.softLight.softLightMask",
                    effDirs + effs.get(ids), R.mipmap.beauty_liti, BeautyConstant.BEAUTY_FACE_SOFTLIGHT,
                    new XmagicPropertyValues(0, 100, 50, 0, 1), liTiLabel));
        }
        XmagicUIProperty itemLiTi = new XmagicUIProperty(liTiLabel, R.mipmap.beauty_liti, UICategory.BEAUTY);
        itemLiTi.xmagicUIPropertyList = liTiItems;
        beautyList.add(itemLiTi);

        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_thin_cheek_label),
                R.mipmap.beauty_thin_cheek, BeautyConstant.BEAUTY_FACE_THIN_CHEEKBONE,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_chin_label),
                R.mipmap.beauty_chin, BeautyConstant.BEAUTY_FACE_THIN_CHIN,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_forehead_label),
                R.mipmap.beauty_forehead, BeautyConstant.BEAUTY_FACE_FOREHEAD,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_eye_lighten_label),
                R.mipmap.beauty_eye_lighten, BeautyConstant.BEAUTY_EYE_LIGHTEN,
                new XmagicPropertyValues(0, 100, 30, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_eye_distance_label),
                R.mipmap.beauty_eye_distance, BeautyConstant.BEAUTY_EYE_DISTANCE,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_eye_angle_label),
                R.mipmap.beauty_eye_angle, BeautyConstant.BEAUTY_EYE_ANGLE,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_thin_nose_label),
                R.mipmap.beauty_thin_nose, BeautyConstant.BEAUTY_NOSE_THIN,
                new XmagicPropertyValues(0, 100, 0, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_nose_wing_label),
                R.mipmap.beauty_nose_wing, BeautyConstant.BEAUTY_NOSE_WING,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_nose_position_label),
                R.mipmap.beauty_nose_position, BeautyConstant.BEAUTY_NOSE_HEIGHT,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_tooth_beauty_label),
                R.mipmap.beauty_tooth_beauty, BeautyConstant.BEAUTY_TOOTH_WHITEN,
                new XmagicPropertyValues(0, 100, 0, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_remove_pounch_label),
                R.mipmap.beauty_remove_pounch, BeautyConstant.BEAUTY_FACE_REMOVE_WRINKLE,
                new XmagicPropertyValues(0, 100, 0, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_wrinkle_smooth_label),
                R.mipmap.beauty_wrinkle_smooth, BeautyConstant.BEAUTY_FACE_REMOVE_LAW_LINE,
                new XmagicPropertyValues(0, 100, 0, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_remove_eye_pouch_label),
                R.mipmap.beauty_remove_eye_pouch, BeautyConstant.BEAUTY_FACE_REMOVE_EYE_BAGS,
                new XmagicPropertyValues(0, 100, 0, 0, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_mouth_size_label),
                R.mipmap.beauty_mouth_size, BeautyConstant.BEAUTY_MOUTH_SIZE,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
        beautyList.add(new XmagicUIProperty<>(UICategory.BEAUTY,
                context.getString(R.string.beauty_mouth_height_label),
                R.mipmap.beauty_mouth_height, BeautyConstant.BEAUTY_MOUTH_HEIGHT,
                new XmagicPropertyValues(-100, 100, 0, -1, 1)));
    }

    /**
     * 滤镜数据构造
     * create lut data
     *
     * @param lutList
     */
    private static void parseLutProperty(Context context, List<XmagicUIProperty<?>> lutList) {
        //收集全部滤镜
        String lutDir = sResPath + "light_material/lut/";
        String[] fileNames = new File(lutDir).list();
        if (fileNames == null) {
            return;
        }
        if (lutList == null) {
            return;
        }
        lutList.add(new XmagicUIProperty<XmagicPropertyValues>(UICategory.LUT,
                context.getString(R.string.item_none_label), XmagicProperty.ID_NONE, "", R.mipmap.naught, null, null));

        LutData[] lutDatas = {
                new LutData(context.getString(R.string.lut_item1_label), "baixi_lf.png", R.mipmap.filter_baizhi),
                new LutData(context.getString(R.string.lut_item2_label), "ziran_lf.png", R.mipmap.filter_ziran),
                new LutData(context.getString(R.string.lut_item3_label), "moren_lf.png", R.mipmap.filter_chulian),
                new LutData(context.getString(R.string.lut_item4_label), "xindong_lf.png", R.mipmap.filter_xindong),
                new LutData(context.getString(R.string.lut_item5_label), "dongjing_lf.png", R.mipmap.filter_dongjing)
        };

        for (LutData lutData : lutDatas) {
            if (exists(lutDir + lutData.id)) {
                lutList.add(new XmagicUIProperty<XmagicPropertyValues>(UICategory.LUT,
                        lutData.name, lutData.id, lutDir + lutData.id, lutData.resourceId, null,
                        new XmagicPropertyValues(0, 100, 60, 0, 1)));
            }
        }

    }

    private static class LutData {
        String name;
        String id;
        int resourceId = 0;

        public LutData(String name, String id, int resourceId) {
            this.name = name;
            this.id = id;
            this.resourceId = resourceId;
        }
    }

    /**
     * 生成动效资源
     * create motion data
     */
    private static void parseMotionData(Context context, List<XmagicUIProperty<?>> motionsData) {
        if (motionsData == null) {
            return;
        }
        List<XmagicUIProperty<?>> dynamicData = new ArrayList<>();
        LinkedHashMap<String, String> map = getMaterialDataByStr(context.getString(R.string.motion_resource_str));

        //2D素材
        String motion2dResPath = sResPath + "/MotionRes/2dMotionRes/";
        XmagicUIProperty item2dMotions = new XmagicUIProperty<>(context.getString(R.string.motion_2d_label),
                R.mipmap.motion_2d, UICategory.MOTION);
        item2dMotions.xmagicUIPropertyList = new ArrayList<XmagicUIProperty<XmagicPropertyValues>>();
        parseMotion(context, motion2dResPath, map,
                context.getString(R.string.motion_3d_label),
                MotionType.MotionRes2D, item2dMotions.xmagicUIPropertyList);
        if (item2dMotions.xmagicUIPropertyList.size() > 0) {
            dynamicData.add(item2dMotions);
        }
        //3D素材
        String motion3dResPath = sResPath + "/MotionRes/3dMotionRes/";
        XmagicUIProperty item3dMotions = new XmagicUIProperty<>(context.getString(R.string.motion_3d_label),
                R.mipmap.motion_3d, UICategory.MOTION);
        item3dMotions.xmagicUIPropertyList = new ArrayList<XmagicUIProperty<XmagicPropertyValues>>();
        parseMotion(context, motion3dResPath, map, context.getString(R.string.motion_3d_label),
                MotionType.MotionRes3D, item3dMotions.xmagicUIPropertyList);
        if (item3dMotions.xmagicUIPropertyList.size() > 0) {
            dynamicData.add(item3dMotions);
        }
        //构造手势素材数据
        String handMotionResPath = sResPath + "/MotionRes/handMotionRes/";
        XmagicUIProperty itemHandMotions = new XmagicUIProperty<>(context.getString(R.string.motion_hand_label),
                R.mipmap.motion_hand, UICategory.MOTION);
        itemHandMotions.xmagicUIPropertyList = new ArrayList<XmagicUIProperty<XmagicPropertyValues>>();
        parseMotion(context, handMotionResPath, map, context.getString(R.string.motion_hand_label),
                MotionType.MotionResHand, itemHandMotions.xmagicUIPropertyList);
        if (itemHandMotions.xmagicUIPropertyList.size() > 0) {
            dynamicData.add(itemHandMotions);
        }
        //趣味素材数据
        String ganMotionResPath = sResPath + "/MotionRes/ganMotionRes/";
        XmagicUIProperty itemGanMotions = new XmagicUIProperty<>(context.getString(R.string.motion_gan_label),
                R.mipmap.motion_gan, UICategory.MOTION);
        itemGanMotions.xmagicUIPropertyList = new ArrayList<XmagicUIProperty<XmagicPropertyValues>>();
        parseMotion(context, ganMotionResPath, map, context.getString(R.string.motion_gan_label),
                MotionType.MotionResGan, itemGanMotions.xmagicUIPropertyList);
        if (itemGanMotions.xmagicUIPropertyList.size() > 0) {
            dynamicData.add(itemGanMotions);
        }
        if (dynamicData.size() > 0) {   //如果有数据才添加无的选项，没有数据则不添加此选项
            motionsData.add(new XmagicUIProperty(UICategory.MOTION,
                    context.getString(R.string.item_none_label), XmagicProperty.ID_NONE,
                    sResPath + "light_assets/template.json",
                    R.mipmap.naught, null, null));
            motionsData.addAll(dynamicData);
        }
    }


    private static void parseMotion(Context context, String filePath,
                                    LinkedHashMap<String, String> converter, String name,
                                    MotionType motionType,
                                    List<XmagicUIProperty<XmagicPropertyValues>> list) {
        List<MotionDLModel> motionDLModels = MotionDLUtils.getMotionsByType(context, motionType);
        File[] ganMotionFiles = getFilesByPath(filePath);
        if (ganMotionFiles != null) {
            for (File file : ganMotionFiles) {
                if (file.isDirectory()) {
                    String id = file.getName();
                    removeHasExitItem(motionDLModels, id);
                    XmagicUIProperty uiProperty = createMotionUiProperty(id, converter, filePath, name);
                    uiProperty.thumbImagePath = getTemplateImgPath(
                            MotionDLUtils.getMotionNameByType(motionType) + File.separator + id
                    );
                    list.add(uiProperty);
                }
            }
        }
        //开始处理需要下载的item
        for (MotionDLModel motionDLModel : motionDLModels) {
            XmagicUIProperty uiProperty = createMotionUiProperty(motionDLModel.getName(), converter, filePath, name);
            uiProperty.thumbImagePath = MotionDLUtils.getIconUrlByName(context, motionDLModel.getName());
            uiProperty.dlModel = motionDLModel;
            list.add(uiProperty);
        }
    }


    private static XmagicUIProperty createMotionUiProperty(String id,
                                                           LinkedHashMap<String, String> nameConverter,
                                                           String resourcePath, String rootDisplayName) {
        String name = nameConverter.get(id);
        String materialPath = resourcePath + id;
        name = (name != null ? name : id);
        return new XmagicUIProperty<>(UICategory.MOTION, name, id, materialPath, 0, null, null, rootDisplayName);
    }


    /**
     * 构造美妆数据
     * create makeup data
     */
    private static void parseMakeUpData(Context context, List<XmagicUIProperty<?>> makeupData) {
        if (makeupData == null) {
            return;
        }
        String makeupResPath = sResPath + "/MotionRes/makeupRes/";
        File[] makeupFiles = getFilesByPath(makeupResPath);
        List<MotionDLModel> motionDLModelList = MotionDLUtils.getMotionsByType(context, MotionType.MotionResMakeup);
        if ((makeupFiles == null || makeupFiles.length == 0)
                && (motionDLModelList == null || motionDLModelList.size() == 0)) {
            return;
        }
        makeupData.add(new XmagicUIProperty(UICategory.MAKEUP,
                context.getString(R.string.item_none_label), XmagicProperty.ID_NONE,
                sResPath + "light_assets/template.json", R.mipmap.naught, null, null));
        LinkedHashMap<String, String> map = getMaterialDataByStr(context.getString(R.string.makeup_resource_str));
        //构造整妆素材数据
        if (makeupFiles != null) {
            for (File file : makeupFiles) {
                if (file.isDirectory()) {
                    String id = file.getName();
                    removeHasExitItem(motionDLModelList, id);
                    String materialPath = makeupResPath + id;
                    String name = map.get(id);
                    name = (name != null ? name : id);
                    XmagicPropertyValues xmagicPropertyValues = new XmagicPropertyValues(0, 100, 60, 0, 1);
                    XmagicUIProperty xmagicUIProperty = new XmagicUIProperty<>(UICategory.MAKEUP,
                            name, id, materialPath, 0,
                            "makeup.strength", xmagicPropertyValues);
                    xmagicUIProperty.thumbImagePath = getTemplateImgPath("makeupRes/" + id);
                    makeupData.add(xmagicUIProperty);
                }
            }
        }
        //开始处理需要下载的item
        for (MotionDLModel motionDLModel : motionDLModelList) {
            String id = motionDLModel.getName();
            String name = map.get(id);
            name = (name != null ? name : id);
            XmagicPropertyValues xmagicPropertyValues = new XmagicPropertyValues(0, 100, 60, 0, 1);
            XmagicUIProperty<XmagicPropertyValues> uiProperty = new XmagicUIProperty<>(UICategory.MAKEUP,
                    name, id, makeupResPath + id, 0, "makeup.strength", xmagicPropertyValues);
            uiProperty.thumbImagePath = MotionDLUtils.getIconUrlByName(context, motionDLModel.getName());
            uiProperty.dlModel = motionDLModel;
            makeupData.add(uiProperty);
        }

    }

    /**
     * 移除贝蒂已有的资源名称
     *
     * @param motionDLModelList
     * @param id
     */
    private static void removeHasExitItem(List<MotionDLModel> motionDLModelList, String id) {
        Iterator<MotionDLModel> motionDLModelIterator = motionDLModelList.iterator();
        while (motionDLModelIterator.hasNext()) {
            MotionDLModel item = motionDLModelIterator.next();
            if (id.equals(item.getName())) {
                motionDLModelIterator.remove();
            }
        }
    }

    /**
     * 构造分割 数据
     * create segmentation data
     */
    private static void parseSegData(Context context, List<XmagicUIProperty<?>> segListData) {
        if (segListData == null) {
            return;
        }
        List<MotionDLModel> motionDLModelList = MotionDLUtils.getMotionsByType(context, MotionType.MotionResSegment);
        String segmentMotionResPath = sResPath + "/MotionRes/segmentMotionRes/";
        File[] segmentMotionFiles = getFilesByPath(segmentMotionResPath);
        if ((segmentMotionFiles == null || segmentMotionFiles.length == 0)
                && (motionDLModelList == null || motionDLModelList.size() == 0)) {
            return;
        }
        LinkedHashMap<String, String> map = getMaterialDataByStr(
                context.getString(R.string.segmentation_resource_str));
        segListData.add(new XmagicUIProperty(UICategory.SEGMENTATION,
                context.getString(R.string.item_none_label), XmagicProperty.ID_NONE,
                sResPath + "light_assets/template.json", R.mipmap.naught, null, null));

        //构造分割素材数据
        if (segmentMotionFiles != null) {
            for (File file : segmentMotionFiles) {
                if (file.isDirectory()) {
                    String id = file.getName();
                    removeHasExitItem(motionDLModelList, id);
                    String materialPath = segmentMotionResPath + id;
                    String name = map.get(id);
                    name = (name != null ? name : id);
                    if (id.equals("video_empty_segmentation")) {
                        segListData.add(new XmagicUIProperty<>(UICategory.SEGMENTATION,
                                context.getString(R.string.segmentation_custom_label),
                                id, materialPath, R.mipmap.segmentation_formulate, "", null));
                    } else {
                        XmagicUIProperty<XmagicPropertyValues> uiProperty = new XmagicUIProperty<>(
                                UICategory.SEGMENTATION, name,
                                id, materialPath, 0, null, null);
                        uiProperty.thumbImagePath = getTemplateImgPath("segmentMotionRes/" + id);
                        segListData.add(uiProperty);
                    }
                }
            }
        }
        //开始处理需要下载的item
        for (MotionDLModel motionDLModel : motionDLModelList) {
            String id = motionDLModel.getName();
            String name = map.get(id);
            name = (name != null ? name : id);
            if (id.equals("video_empty_segmentation")) {
                name = context.getString(R.string.segmentation_custom_label);
            }
            XmagicUIProperty<XmagicPropertyValues> uiProperty = new XmagicUIProperty<>(
                    UICategory.SEGMENTATION, name, id,
                    segmentMotionResPath + id, 0, null, null);
            uiProperty.thumbImagePath = MotionDLUtils.getIconUrlByName(context, motionDLModel.getName());
            uiProperty.dlModel = motionDLModel;
            segListData.add(uiProperty);
        }
    }

    private static File[] getFilesByPath(String path) {
        File[] fileList = new File(path).listFiles();
        if (fileList != null && fileList.length > 0) {
            List<File> fileNames = Arrays.asList(fileList);
            Collections.sort(fileNames, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile()) {
                        return -1;
                    }
                    if (o1.isFile() && o2.isDirectory()) {
                        return 1;
                    }
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return fileList;
        }
        return null;
    }

    private static LinkedHashMap<String, String> getMaterialDataByStr(String str) {
        LinkedHashMap<String, String> propertyNameConverter = new LinkedHashMap<>();
        String[] pairs = str.split(",");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split(":");
            propertyNameConverter.put(keyValue[0].trim(), keyValue[1].trim());
        }
        return propertyNameConverter;
    }


    private static String getTemplateImgPath(String motionName) {
        return sResPath + "MotionRes" + File.separator + motionName + File.separator + "template.png";
    }

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return true为文件存在，false为文件不存在
     */
    private static boolean exists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        // assets中的文件，默认一定存在；非assets中的文件，需要正常判断是否存在
        return path.indexOf("assets") >= 0 || new File(path).exists();
    }


    /**
     * 复制asset文件到指定目录
     *
     * @param oldPath asset下的路径
     * @param newPath SD卡下保存路径
     */
    private static boolean copyAssets(Context context, String oldPath, String newPath) {
        try {
            // 获取assets目录下的所有文件及目录名
            String[] fileNames = context.getAssets().list(oldPath);

            if (fileNames.length > 0) {    // 如果是目录
                Log.d(TAG, "copyAssets path: " + Arrays.toString(fileNames));
                File file = new File(newPath);
                file.mkdirs();    // 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {   // 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024 * 1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {    // 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);    // 将读取的输入流写入到输出流
                }
                fos.flush();    // 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void ensureResPathAlreadySet() {
        if (TextUtils.isEmpty(sResPath)) {
            throw new IllegalStateException("resource path not set, call XmagicResParser.setResPath() first.");
        }
    }



    private static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
