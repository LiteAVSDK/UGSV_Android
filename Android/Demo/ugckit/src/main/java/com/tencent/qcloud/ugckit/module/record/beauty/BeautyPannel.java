package com.tencent.qcloud.ugckit.module.record.beauty;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;

import com.tencent.qcloud.ugckit.module.record.VideoMaterialDownloadProgress;
import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.pickerview.HorizontalPickerView;
import com.tencent.qcloud.ugckit.module.record.CustomProgressDialog;
import com.tencent.qcloud.ugckit.module.record.interfaces.IBeautyPannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 录制美颜Pannel
 */
public class BeautyPannel extends FrameLayout implements IBeautyPannel, SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private final String TAG = "BeautyPannel";
    // 美容
    public static final int ITEM_TYPE_BEAUTY = 0;
    // 滤镜
    public static final int ITEM_TYPE_FILTTER = 1;
    // 动效贴纸
    public static final int ITEM_TYPE_MOTION = 2;
    // 抠背
    public static final int ITEM_TYPE_KOUBEI = 3;
    // 绿幕
    public static final int ITEM_TYPE_GREEN = 4;
    // 美妆
    public static final int ITEM_TYPE_BEAUTY_FACE = 5;
    // 手势
    public static final int ITEM_TYPE_GESUTRE = 6;
    // 美体
    public static final int ITEM_TYPE_BEAUTY_BODY = 7;

    private int mSencodGradleType = ITEM_TYPE_BEAUTY;
    private int mThirdGradleIndex = 0;
    @Nullable
    private int[][] mSzSeekBarValue = null;
    @NonNull
    private int[] mSzSecondGradleIndex = new int[16];
    private TextView mSeekBarValue;
    private SeekBar mSeekbar;
    private CustomProgressDialog mCustomProgressDialog;
    private final int mFilterBasicLevel = 5;

    private final int mBeautyBasicLevel = 4;
    private final int mWhiteBasicLevel = 1;
    private final int mRuddyBasicLevel = 0;

    private Context mContext;
    private IOnBeautyParamsChangeListener mBeautyChangeListener;

    public static final int BEAUTYPARAM_BEAUTY = 1;
    public static final int BEAUTYPARAM_WHITE = 2;
    public static final int BEAUTYPARAM_FACESLIM = 3;
    public static final int BEAUTYPARAM_BIG_EYE = 4;
    public static final int BEAUTYPARAM_FILTER = 5;
    public static final int BEAUTYPARAM_FILTER_MIX_LEVEL = 6;
    public static final int BEAUTYPARAM_MOTION_TMPL = 7;
    public static final int BEAUTYPARAM_GREEN = 8;
    public static final int BEAUTYPARAM_RUDDY = 10;
    public static final int BEAUTYPARAM_NOSESCALE = 11;
    public static final int BEAUTYPARAM_CHINSLIME = 12;
    public static final int BEAUTYPARAM_FACEV = 13;
    public static final int BEAUTYPARAM_FACESHORT = 14;
    public static final int BEAUTYPARAM_SHARPEN = 15;
    public static final int BEAUTYPARAM_CAPTURE_MODE = 16;
    public static final int BEAUTYPARAM_SKINBEAUTY = 17;
    public static final int BEAUTYPARAM_EYELIGHTEN = 18;
    public static final int BEAUTYPARAM_TOOTHWHITEN = 19;
    public static final int BEAUTYPARAM_WRINKLEREMOVE = 20;
    public static final int BEAUTYPARAM_POUNCHREMOVE = 21;
    public static final int BEAUTYPARAM_SMILELINESREMOVE = 22;
    public static final int BEAUTYPARAM_FOREHEAD = 23;
    public static final int BEAUTYPARAM_EYEDISTANCE = 24;
    public static final int BEAUTYPARAM_EYEANGLE = 25;
    public static final int BEAUTYPARAM_MOUTHSHAPE = 26;
    public static final int BEAUTYPARAM_NOSEWING = 27;
    public static final int BEAUTYPARAM_NOSEPOSITION = 28;
    public static final int BEAUTYPARAM_LIPSTHICKNESS = 29;
    public static final int BEAUTYPARAM_FACEBEAUTY = 30;
    public static final int BEAUTYPARAM_LONGLEG = 31;
    public static final int BEAUTYPARAM_THINWAIST = 32;
    public static final int BEAUTYPARAM_THINBODY = 33;
    public static final int BEAUTYPARAM_THINSHOULDER = 34;
    public static final int BEAUTYPARAM_BEAUTY_STYLE_SMOOTH = 0; // 光滑
    public static final int BEAUTYPARAM_BEAUTY_STYLE_NATURAL = 1; // 自然
    public static final int BEAUTYPARAM_BEAUTY_STYLE_HAZY = 2; // 天天P图(朦胧)
    private ArrayAdapter<String> mFirstGradleAdapter;
    private ArrayList<BeautyData> mFilterBeautyDataList;
    private ArrayList<BeautyData> mBeautyDataList;
    private ArrayList<BeautyData> mGreenScreenDataList;
    private ArrayList<BeautyData> mKoubeiDataList;
    private ArrayList<BeautyData> mFaceBeautyDataList;
    private ArrayList<BeautyData> mGestureDataLit;
    private IconTextAdapter mItemAdapter;

    @NonNull
    private String[] mBeautyStyleString = {
            getResources().getString(R.string.beauty_setting_pannel_beauty),
            getResources().getString(R.string.beauty_setting_pannel_filter),
            getResources().getString(R.string.beauty_setting_pannel_dynamic_effect),
            getResources().getString(R.string.beauty_setting_pannel_key),
            getResources().getString(R.string.beauty_setting_pannel_green_screen),
            getResources().getString(R.string.beauty_setting_pannel_facebeauty),
            getResources().getString(R.string.beauty_setting_pannel_gesture),
    };

    @NonNull
    private String[] mFilterTypeString = {
            getResources().getString(R.string.beauty_setting_pannel_filter_none),
            getResources().getString(R.string.beauty_setting_pannel_filter_standard),
            getResources().getString(R.string.beauty_setting_pannel_filter_cheery),
            getResources().getString(R.string.beauty_setting_pannel_filter_cloud),
            getResources().getString(R.string.beauty_setting_pannel_filter_pure),
            getResources().getString(R.string.beauty_setting_pannel_filter_orchid),
            getResources().getString(R.string.beauty_setting_pannel_filter_vitality),
            getResources().getString(R.string.beauty_setting_pannel_filter_super),
            getResources().getString(R.string.beauty_setting_pannel_filter_fragrance),
            getResources().getString(R.string.beauty_setting_pannel_filter_romantic),
            getResources().getString(R.string.beauty_setting_pannel_filter_fresh),
            getResources().getString(R.string.beauty_setting_pannel_filter_beautiful),
            getResources().getString(R.string.beauty_setting_pannel_filter_pink),
            getResources().getString(R.string.beauty_setting_pannel_filter_reminiscence),
            getResources().getString(R.string.beauty_setting_pannel_filter_blues),
            getResources().getString(R.string.beauty_setting_pannel_filter_cool),
            getResources().getString(R.string.beauty_setting_pannel_filter_Japanese),
    };
    private ArrayList<String> mFirstGradleArrayString = new ArrayList<String>();
    @NonNull
    private List<MotionData> motionDataList = new ArrayList<>();
    @NonNull
    private List<MotionData> motionDataKoubeiList = new ArrayList<>();
    private List<MotionData> motionBeautyFaceList = new ArrayList<>();
    private List<MotionData> motionGestureList = new ArrayList<>();

    private HorizontalPickerView mFirstGradlePicker;
    private HorizontalPickerView mSecondGradlePicker;
    private SharedPreferences mPrefs;
    private ArrayList<BeautyData> mMotionDataList;
    private MotionData motionData;
    private ArrayList<BeautyData> beautyDataList;
    private int mTextColorPrimary;

    public BeautyPannel(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        LayoutInflater.from(context).inflate(R.layout.ugckit_beauty_pannel, this);
        initView();
    }

    @Override
    public void setBeautyParamsChangeListener(IOnBeautyParamsChangeListener listener) {
        mBeautyChangeListener = listener;
    }

    private void initView() {
        mTextColorPrimary = UIAttributeUtil.getColorRes(mContext, R.attr.ugckitColorPrimary, R.color.colorRed2);
        mSeekbar = (SeekBar) findViewById(R.id.seekbarThird);
        mSeekbar.setOnSeekBarChangeListener(this);

        mFirstGradlePicker = (HorizontalPickerView) findViewById(R.id.horizontalPickerViewFirst);
        mSecondGradlePicker = (HorizontalPickerView) findViewById(R.id.horizontalPickerViewSecond);
        mSeekBarValue = (TextView) findViewById(R.id.tvSeekbarValue);
        mItemAdapter = new IconTextAdapter(mContext);
        initBeautyData();
        initFilterData();
        initMotionData();
        initMotionLink();
        initGreenScreenData();
        initKoubeiData();
        initFaceBeautyData();
        initGestureData();
        setFirstPickerType();
    }

    private void initMotionLink() {
        motionDataList.add(new MotionData("none", "无动效", "", ""));
        motionDataKoubeiList.add(new MotionData("none", "无", "", ""));
        // 美妆
        motionBeautyFaceList.add(new MotionData("none", "无", "", ""));
        // 手势
        motionGestureList.add(new MotionData("none", "无", "", ""));
    }

    private void initMotionData() {
        mMotionDataList = new ArrayList<BeautyData>();
        mMotionDataList.add(new BeautyData(R.drawable.ic_effect_non, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_none)));
        mMotionDataList.add(new BeautyData(R.drawable.video_boom, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_boom)));
        mMotionDataList.add(new BeautyData(R.drawable.video_nihongshu, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_neon_mouse)));
        mMotionDataList.add(new BeautyData(R.drawable.video_starear, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_star_ear)));
        mMotionDataList.add(new BeautyData(R.drawable.video_fengkuangdacall, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_crazy_cheer_up)));
        mMotionDataList.add(new BeautyData(R.drawable.video_qxingzuo, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_Q_cancelonstellation)));
        mMotionDataList.add(new BeautyData(R.drawable.video_caidai, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_colored_ribbon)));
        mMotionDataList.add(new BeautyData(R.drawable.video_liuhaifadai, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_bands_hairband)));
        mMotionDataList.add(new BeautyData(R.drawable.video_lianpu, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_change_face)));
        mMotionDataList.add(new BeautyData(R.drawable.video_purplecat, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_purple_kitten)));
        mMotionDataList.add(new BeautyData(R.drawable.video_huaxianzi, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_flower_faerie)));
        mMotionDataList.add(new BeautyData(R.drawable.video_baby_agetest, getResources().getString(R.string.beauty_setting_pannel_dynamic_effect_little_Princess)));
    }

    /**
     * 初始化美容数据
     */
    private void initBeautyData() {
        int beauty_smooth = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelSmoothIcon, R.drawable.ic_beauty_smooth);
        int beauty_natural = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelNaturalIcon, R.drawable.ic_beauty_natural);
        int beauty_pitu = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelPituIcon, R.drawable.ic_beauty_pitu);
        int beauty_white = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelWhiteIcon, R.drawable.ic_beauty_white);
        int beauty_ruddy = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelRuddyIcon, R.drawable.ic_beauty_ruddy);
        int beauty_bigeye = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelBigeyeIcon, R.drawable.ic_beauty_bigeye);
        int beauty_faceslim = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelFaceslimIcon, R.drawable.ic_beauty_faceslim);
        int beauty_facev = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelFacevIcon, R.drawable.ic_beauty_facev);
        int beauty_chin = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelChinIcon, R.drawable.ic_beauty_chin);
        int beauty_faceshort = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelFaceshortIcon, R.drawable.ic_beauty_faceshort);
        int beauty_noseslim = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelNoseslimIcon, R.drawable.ic_beauty_noseslim);
        int beauty_eyebright = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelEyeLightenIcon, R.drawable.ic_eye_bright);
        int beauty_toothwhite = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelToothWhiteIcon, R.drawable.ic_tooth_white);
        int beauty_pounchremove = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelPounchRemoveIcon, R.drawable.ic_pounch_remove);
        int beauty_wrinkles = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelWrinkleIcon, R.drawable.ic_wrinkles);
        int beauty_forehead = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelForeheadIcon, R.drawable.ic_forehead);
        int beauty_eyedistance = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelEyeDistanceIcon, R.drawable.ic_eye_distance);
        int beauty_eyeangle = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelEyeAngleIcon, R.drawable.ic_eye_angle);
        int beauty_mouthshape = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelMouthShapeIcon, R.drawable.ic_mouseshape);
        int beauty_nosewing = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelNoseWingIcon, R.drawable.ic_nose_wing);
        int beauty_noseposition = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelNosePositionIcon, R.drawable.ic_nose_position);
        int beauty_mousewidth = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelMouseWidthIcon, R.drawable.ic_mouse_width);
        int beauty_faceshape = UIAttributeUtil.getResResources(getContext(), R.attr.beautyPannelFaceShapeIcon, R.drawable.ic_faceshape);

        mBeautyDataList = new ArrayList<BeautyData>();
        mBeautyDataList.add(new BeautyData(beauty_smooth, getResources().getString(R.string.beauty_pannel_style_smooth)));
        mBeautyDataList.add(new BeautyData(beauty_natural, getResources().getString(R.string.beauty_pannel_style_natural)));
        mBeautyDataList.add(new BeautyData(beauty_pitu, getResources().getString(R.string.beauty_pannel_style_pitu)));
        mBeautyDataList.add(new BeautyData(beauty_white, getResources().getString(R.string.beauty_pannel_white)));
        mBeautyDataList.add(new BeautyData(beauty_ruddy, getResources().getString(R.string.beauty_pannel_ruddy)));
        mBeautyDataList.add(new BeautyData(beauty_bigeye, getResources().getString(R.string.beauty_pannel_bigeye)));
        mBeautyDataList.add(new BeautyData(beauty_faceslim, getResources().getString(R.string.beauty_pannel_faceslim)));
        mBeautyDataList.add(new BeautyData(beauty_facev, getResources().getString(R.string.beauty_pannel_facev)));
        mBeautyDataList.add(new BeautyData(beauty_chin, getResources().getString(R.string.beauty_pannel_chin)));
        mBeautyDataList.add(new BeautyData(beauty_faceshort, getResources().getString(R.string.beauty_pannel_faceshort)));
        mBeautyDataList.add(new BeautyData(beauty_noseslim, getResources().getString(R.string.beauty_pannel_noseslim)));
        mBeautyDataList.add(new BeautyData(beauty_eyebright, getResources().getString(R.string.beauty_pannel_eyelighten)));
        mBeautyDataList.add(new BeautyData(beauty_toothwhite, getResources().getString(R.string.beauty_pannel_toothwhite)));
        mBeautyDataList.add(new BeautyData(beauty_pounchremove, getResources().getString(R.string.beauty_pannel_pounchremove)));
        mBeautyDataList.add(new BeautyData(beauty_wrinkles, getResources().getString(R.string.beauty_pannel_wrinkleremove)));
        mBeautyDataList.add(new BeautyData(beauty_forehead, getResources().getString(R.string.beauty_pannel_forehead)));
        mBeautyDataList.add(new BeautyData(beauty_eyedistance, getResources().getString(R.string.beauty_pannel_eyedistance)));
        mBeautyDataList.add(new BeautyData(beauty_eyeangle, getResources().getString(R.string.beauty_pannel_eyeangle)));
        mBeautyDataList.add(new BeautyData(beauty_mouthshape, getResources().getString(R.string.beauty_pannel_mouthshape)));
        mBeautyDataList.add(new BeautyData(beauty_nosewing, getResources().getString(R.string.beauty_pannel_nosewing)));
        mBeautyDataList.add(new BeautyData(beauty_noseposition, getResources().getString(R.string.beauty_pannel_noseposition)));
        mBeautyDataList.add(new BeautyData(beauty_mousewidth, getResources().getString(R.string.beauty_pannel_mousewidth)));
        mBeautyDataList.add(new BeautyData(beauty_faceshape, getResources().getString(R.string.beauty_pannel_faceshape)));
    }

    /**
     * 初始化滤镜数据
     */
    private void initFilterData() {
        mFilterBeautyDataList = new ArrayList<BeautyData>();
        mFilterBeautyDataList.add(new BeautyData(R.drawable.ic_effect_non, getResources().getString(R.string.beauty_setting_pannel_filter_none)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.biaozhun, getResources().getString(R.string.beauty_setting_pannel_filter_standard)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.yinghong, getResources().getString(R.string.beauty_setting_pannel_filter_cheery)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.yunshang, getResources().getString(R.string.beauty_setting_pannel_filter_cloud)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.chunzhen, getResources().getString(R.string.beauty_setting_pannel_filter_pure)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.bailan, getResources().getString(R.string.beauty_setting_pannel_filter_orchid)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.yuanqi, getResources().getString(R.string.beauty_setting_pannel_filter_vitality)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.chaotuo, getResources().getString(R.string.beauty_setting_pannel_filter_super)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.xiangfen, getResources().getString(R.string.beauty_setting_pannel_filter_fragrance)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.langman, getResources().getString(R.string.beauty_setting_pannel_filter_romantic)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.qingxin, getResources().getString(R.string.beauty_setting_pannel_filter_fresh)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.weimei, getResources().getString(R.string.beauty_setting_pannel_filter_beautiful)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.fennen, getResources().getString(R.string.beauty_setting_pannel_filter_pink)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.huaijiu, getResources().getString(R.string.beauty_setting_pannel_filter_reminiscence)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.landiao, getResources().getString(R.string.beauty_setting_pannel_filter_blues)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.qingliang, getResources().getString(R.string.beauty_setting_pannel_filter_cool)));
        mFilterBeautyDataList.add(new BeautyData(R.drawable.rixi, getResources().getString(R.string.beauty_setting_pannel_filter_Japanese)));
    }

    /**
     * 初始化绿幕数据
     */
    private void initGreenScreenData() {
        mGreenScreenDataList = new ArrayList<BeautyData>();
        mGreenScreenDataList.add(new BeautyData(R.drawable.ic_effect_non, getResources().getString(R.string.beauty_setting_pannel_green_screen_none)));
        mGreenScreenDataList.add(new BeautyData(R.drawable.ic_beauty_goodluck, getResources().getString(R.string.beauty_setting_pannel_green_screen_good_luck)));
    }

    private void initKoubeiData() {
        mKoubeiDataList = new ArrayList<BeautyData>();
        mKoubeiDataList.add(new BeautyData(R.drawable.ic_effect_non, getResources().getString(R.string.beauty_setting_pannel_key_none)));
        mKoubeiDataList.add(new BeautyData(R.drawable.ic_beauty_koubei, getResources().getString(R.string.beauty_setting_pannel_key_AI_key)));
    }

    private void initGestureData() {
        mGestureDataLit = new ArrayList<BeautyData>();
        mGestureDataLit.add(new BeautyData(R.drawable.ic_effect_non, getResources().getString(R.string.beauty_setting_pannel_key_none)));
        mGestureDataLit.add(new BeautyData(R.drawable.video_pikachu, getResources().getString(R.string.beauty_setting_pannel_pikaqiu)));
        mGestureDataLit.add(new BeautyData(R.drawable.video_liuxingyu, getResources().getString(R.string.beauty_setting_pannel_liuxingyu)));
        mGestureDataLit.add(new BeautyData(R.drawable.video_kongxue2, getResources().getString(R.string.beauty_setting_pannel_kongxue)));
        mGestureDataLit.add(new BeautyData(R.drawable.video_dianshizhixing, getResources().getString(R.string.beauty_setting_pannel_dianshizhixing)));
        mGestureDataLit.add(new BeautyData(R.drawable.video_bottle1, getResources().getString(R.string.beauty_setting_pannel_bottle)));
    }

    private void initFaceBeautyData() {
        mFaceBeautyDataList = new ArrayList<BeautyData>();
        mFaceBeautyDataList.add(new BeautyData(R.drawable.ic_effect_non, getResources().getString(R.string.beauty_setting_pannel_key_none)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_yuansufugu, getResources().getString(R.string.beauty_setting_pannel_yuansufugu)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_cherries, getResources().getString(R.string.beauty_setting_pannel_cherries)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_haiyang2, getResources().getString(R.string.beauty_setting_pannel_haiyang)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_fenfenxia_square, getResources().getString(R.string.beauty_setting_pannel_fenfenxia)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_guajiezhuang, getResources().getString(R.string.beauty_setting_pannel_guajiezhuang)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_qixichun, getResources().getString(R.string.beauty_setting_pannel_qixichun)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_gufengzhuang, getResources().getString(R.string.beauty_setting_pannel_gufengzhuang)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_dxxiaochounv, getResources().getString(R.string.beauty_setting_pannel_xiaochounv)));
        mFaceBeautyDataList.add(new BeautyData(R.drawable.video_remix1, getResources().getString(R.string.beauty_setting_pannel_hunhezhuang)));
    }

    private void setFirstPickerType() {
        mFirstGradleArrayString.clear();
        mFirstGradleArrayString.addAll(Arrays.asList(mBeautyStyleString));
        mFirstGradleAdapter = new ArrayAdapter<String>(mContext, 0, mFirstGradleArrayString) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                String value = getItem(position);
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
                }
                TextView view = (TextView) convertView.findViewById(android.R.id.text1);
                view.setTag(position);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                view.setText(value);
                view.setPadding(15, 5, 30, 5);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        int index = (int) view.getTag();
                        ViewGroup group = (ViewGroup) mFirstGradlePicker.getChildAt(0);
                        for (int i = 0; i < mFirstGradleAdapter.getCount(); i++) {
                            View v = group.getChildAt(i);
                            if (v instanceof TextView) {
                                if (i == index) {
                                    ((TextView) v).setTextColor(mTextColorPrimary);
                                } else {
                                    ((TextView) v).setTextColor(Color.WHITE);
                                }
                            }
                        }
                        setSecondPickerType(index);
                    }
                });
                return convertView;

            }
        };
        mFirstGradlePicker.setAdapter(mFirstGradleAdapter);
        mFirstGradlePicker.setClicked(ITEM_TYPE_BEAUTY);
    }

    private void setSecondPickerType(int type) {
        mSencodGradleType = type;

        beautyDataList = null;
        switch (type) {
            case ITEM_TYPE_BEAUTY:
                beautyDataList = mBeautyDataList;
                break;
            case ITEM_TYPE_FILTTER:
                beautyDataList = mFilterBeautyDataList;
                break;
            case ITEM_TYPE_MOTION:
                beautyDataList = mMotionDataList;
                break;
            case ITEM_TYPE_KOUBEI:
                beautyDataList = mKoubeiDataList;
                break;
            case ITEM_TYPE_GREEN:
                beautyDataList = mGreenScreenDataList;
                break;
            case ITEM_TYPE_BEAUTY_FACE:
                beautyDataList = mFaceBeautyDataList;
                break;
            case ITEM_TYPE_GESUTRE:
                beautyDataList = mGestureDataLit;
                break;
            default:
                break;
        }
        mItemAdapter.addAll(beautyDataList);
        mItemAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BeautyData beautyData, int pos) {
                switch (mSencodGradleType) {
                    case ITEM_TYPE_BEAUTY:
                    case ITEM_TYPE_FILTTER:
                    case ITEM_TYPE_GREEN:
                        setPickerEffect(mSencodGradleType, pos);
                        break;
                    case ITEM_TYPE_KOUBEI:
                        if (pos > motionDataKoubeiList.size() - 1) {
                            return;
                        }
                        motionData = motionDataKoubeiList.get(pos);
                        if (motionData.motionId.equals("none") || !TextUtils.isEmpty(motionData.motionPath)) {
                            setPickerEffect(mSencodGradleType, pos);
                        } else if ((TextUtils.isEmpty(motionData.motionPath))) {
                            downloadVideoMaterial(beautyData, motionData, pos);
                        }
                        break;
                    case ITEM_TYPE_MOTION:
                        if (pos > motionDataList.size() - 1) {
                            return;
                        }
                        motionData = motionDataList.get(pos);
                        if (motionData.motionId.equals("none") || !TextUtils.isEmpty(motionData.motionPath)) {
                            setPickerEffect(mSencodGradleType, pos);
                        } else if ((TextUtils.isEmpty(motionData.motionPath))) {
                            downloadVideoMaterial(beautyData, motionData, pos);
                        }
                        break;
                    case ITEM_TYPE_BEAUTY_FACE:
                        if (pos > motionBeautyFaceList.size() - 1) {
                            return;
                        }
                        motionData = motionBeautyFaceList.get(pos);
                        if (motionData.motionId.equals("none") || !TextUtils.isEmpty(motionData.motionPath)) {
                            setPickerEffect(mSencodGradleType, pos);
                        } else if ((TextUtils.isEmpty(motionData.motionPath))) {
                            downloadVideoMaterial(beautyData, motionData, pos);
                        }
                        break;
                    case ITEM_TYPE_GESUTRE:
                        if (pos > motionGestureList.size() - 1) {
                            return;
                        }
                        motionData = motionGestureList.get(pos);
                        if (motionData.motionId.equals("none") || !TextUtils.isEmpty(motionData.motionPath)) {
                            setPickerEffect(mSencodGradleType, pos);
                        } else if ((TextUtils.isEmpty(motionData.motionPath))) {
                            downloadVideoMaterial(beautyData, motionData, pos);
                        }
                        break;
                }
            }
        });
        mSecondGradlePicker.setAdapter(mItemAdapter);
        mSecondGradlePicker.setClicked(mSzSecondGradleIndex[mSencodGradleType]);
    }

    private void downloadVideoMaterial(BeautyData beautyData, final MotionData motionData, final int pos) {
        VideoMaterialDownloadProgress videoMaterialDownloadProgress = new VideoMaterialDownloadProgress(beautyData.text, motionData.motionUrl);
        videoMaterialDownloadProgress.start(new VideoMaterialDownloadProgress.Downloadlistener() {
            @Override
            public void onDownloadFail(final String errorMsg) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCustomProgressDialog != null) {
                            mCustomProgressDialog.dismiss();
                        }
                        ToastUtil.toastShortMessage(errorMsg);
                    }
                });
            }

            @Override
            public void onDownloadProgress(final int progress) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TXCLog.i(TAG, "onDownloadProgress, progress = " + progress);
                        if (mCustomProgressDialog == null) {
                            mCustomProgressDialog = new CustomProgressDialog();
                            mCustomProgressDialog.createLoadingDialog(mContext);
                            mCustomProgressDialog.setCancelable(false); // 设置是否可以通过点击Back键取消
                            mCustomProgressDialog.setCanceledOnTouchOutside(false); // 设置在点击Dialog外是否取消Dialog进度条
                            mCustomProgressDialog.show();
                        }
                        mCustomProgressDialog.setMsg(progress + "%");
                    }
                });
            }

            @Override
            public void onDownloadSuccess(String filePath) {
                motionData.motionPath = filePath;
                mPrefs.edit().putString(motionData.motionId, filePath).apply();

                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCustomProgressDialog != null) {
                            mCustomProgressDialog.dismiss();
                            mCustomProgressDialog = null;
                        }
                        setPickerEffect(mSencodGradleType, pos);
                    }
                });
            }
        });

    }

    public interface OnItemClickListener {
        void onItemClick(BeautyData beautyData, int pos);
    }

    private void setPickerEffect(int type, int index) {
        initSeekBarValue();
        mSzSecondGradleIndex[type] = index;
        mThirdGradleIndex = index;

        switch (type) {
            case ITEM_TYPE_BEAUTY:
                mSeekbar.setVisibility(View.VISIBLE);
                mSeekBarValue.setVisibility(View.VISIBLE);
                mSeekbar.setProgress(mSzSeekBarValue[type][index]);
                setBeautyStyle(index, mSzSeekBarValue[type][index]);
                break;
            case ITEM_TYPE_FILTTER:
                setFilter(index);
                mSeekbar.setVisibility(View.VISIBLE);
                mSeekBarValue.setVisibility(View.VISIBLE);
                mSeekbar.setProgress(mSzSeekBarValue[type][index]);
                break;
            case ITEM_TYPE_MOTION:
            case ITEM_TYPE_BEAUTY_FACE:
            case ITEM_TYPE_GESUTRE:
                mSeekbar.setVisibility(View.GONE);
                mSeekBarValue.setVisibility(View.GONE);
                setDynamicEffect(type, index);
                break;
            case ITEM_TYPE_KOUBEI:
                mSeekbar.setVisibility(View.GONE);
                mSeekBarValue.setVisibility(View.GONE);
                setDynamicEffect(type, index);
                break;
            case ITEM_TYPE_GREEN:
                mSeekbar.setVisibility(View.GONE);
                mSeekBarValue.setVisibility(View.GONE);
                setGreenScreen(index);
                break;
            default:
                break;
        }

    }

    private Bitmap decodeResource(@NonNull Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }

    //设置滤镜
    private void setFilter(int index) {
        Bitmap bmp = getFilterBitmapByIndex(index);
        if (mBeautyChangeListener != null) {
            BeautyParams params = new BeautyParams();
            params.mFilterBmp = bmp;
            params.filterIndex = index;
            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FILTER);
        }
    }

    @Nullable
    public Bitmap getFilterBitmapByIndex(int index) {
        Bitmap bmp = null;
        switch (index) {
            case 1:
                bmp = decodeResource(getResources(), R.drawable.filter_biaozhun);
                break;
            case 2:
                bmp = decodeResource(getResources(), R.drawable.filter_yinghong);
                break;
            case 3:
                bmp = decodeResource(getResources(), R.drawable.filter_yunshang);
                break;
            case 4:
                bmp = decodeResource(getResources(), R.drawable.filter_chunzhen);
                break;
            case 5:
                bmp = decodeResource(getResources(), R.drawable.filter_bailan);
                break;
            case 6:
                bmp = decodeResource(getResources(), R.drawable.filter_yuanqi);
                break;
            case 7:
                bmp = decodeResource(getResources(), R.drawable.filter_chaotuo);
                break;
            case 8:
                bmp = decodeResource(getResources(), R.drawable.filter_xiangfen);
                break;
            case 9:
                bmp = decodeResource(getResources(), R.drawable.filter_langman);
                break;
            case 10:
                bmp = decodeResource(getResources(), R.drawable.filter_qingxin);
                break;
            case 11:
                bmp = decodeResource(getResources(), R.drawable.filter_weimei);
                break;
            case 12:
                bmp = decodeResource(getResources(), R.drawable.filter_fennen);
                break;
            case 13:
                bmp = decodeResource(getResources(), R.drawable.filter_huaijiu);
                break;
            case 14:
                bmp = decodeResource(getResources(), R.drawable.filter_landiao);
                break;
            case 15:
                bmp = decodeResource(getResources(), R.drawable.filter_qingliang);
                break;
            case 16:
                bmp = decodeResource(getResources(), R.drawable.filter_rixi);
                break;
            default:
                bmp = null;
                break;
        }
        return bmp;
    }

    //设置绿幕
    private void setGreenScreen(int index) {
        String file = "";
        switch (index) {
            case 1:
                file = "green_1.mp4";
                break;
            default:
                break;
        }
        if (mBeautyChangeListener != null) {
            BeautyParams params = new BeautyParams();
            params.mGreenFile = file;
            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_GREEN);
        }
    }

    //设置动效
    private void setDynamicEffect(int type, int index) {
        MotionData motionData = null;
        if (type == ITEM_TYPE_MOTION) {
            motionData = motionDataList.get(index);
        } else if (type == ITEM_TYPE_KOUBEI) {
            motionData = motionDataKoubeiList.get(index);
        } else if (type == ITEM_TYPE_BEAUTY_FACE) {
            motionData = motionBeautyFaceList.get(index);
        } else if (type == ITEM_TYPE_GESUTRE) {
            motionData = motionGestureList.get(index);
            if (motionData.motionId.equals("video_pikachu")) {
                Toast.makeText(mContext, "伸出手掌", Toast.LENGTH_SHORT).show();
            }
        }
        String path = motionData.motionPath;
        if (mBeautyChangeListener != null) {
            BeautyParams params = new BeautyParams();
            params.mMotionTmplPath = path;
            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_MOTION_TMPL);
        }
    }

    // 设置美颜类型
    private void setBeautyStyle(int index, int beautyLevel) {
        int style = index;
        if (index >= 3) {
            return;
        }
        if (mBeautyChangeListener != null) {
            BeautyParams params = new BeautyParams();
            params.mBeautyStyle = style;
            params.mBeautyLevel = beautyLevel;
            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BEAUTY);
        }
    }

    @Override
    public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
        initSeekBarValue();
        mSzSeekBarValue[mSencodGradleType][mThirdGradleIndex] = progress;   // 记录设置的值
        mSeekBarValue.setText(String.valueOf(progress));

        if (seekBar.getId() == R.id.seekbarThird) {
            if (mSencodGradleType == ITEM_TYPE_BEAUTY) {
                BeautyData beautyData = beautyDataList.get(mThirdGradleIndex);
                String beautyType = beautyData.text;
                if (beautyType.equals(getResources().getString(R.string.beauty_pannel_style_smooth))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mBeautyLevel = progress;
                        params.mBeautyStyle = BEAUTYPARAM_BEAUTY_STYLE_SMOOTH;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BEAUTY);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_style_natural))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mBeautyLevel = progress;
                        params.mBeautyStyle = BEAUTYPARAM_BEAUTY_STYLE_NATURAL;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BEAUTY);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_style_pitu))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mBeautyLevel = progress;
                        params.mBeautyStyle = BEAUTYPARAM_BEAUTY_STYLE_HAZY;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BEAUTY);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_white))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mWhiteLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_WHITE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_ruddy))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mRuddyLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_RUDDY);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_bigeye))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mBigEyeLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BIG_EYE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_faceslim))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mFaceSlimLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FACESLIM);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_facev))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mFaceVLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FACEV);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_chin))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mChinSlimLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_CHINSLIME);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_faceshort))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mFaceShortLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FACESHORT);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_noseslim))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mNoseScaleLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_NOSESCALE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_eyelighten))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mEyeLightenLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_EYELIGHTEN);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_toothwhite))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mToothWhitenLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_TOOTHWHITEN);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_pounchremove))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mPounchRemoveLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_POUNCHREMOVE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_wrinkleremove))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mWrinkleRemoveLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_WRINKLEREMOVE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_smilelinesremove))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mSmileLinesRemoveLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_SMILELINESREMOVE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_forehead))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mForeheadLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FOREHEAD);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_eyedistance))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mEyeDistanceLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_EYEDISTANCE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_eyeangle))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mEyeAngleLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_EYEANGLE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_mouthshape))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mMouthShapeLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_MOUTHSHAPE);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_nosewing))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mNoseWingLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_NOSEWING);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_noseposition))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mNosePositionLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_NOSEPOSITION);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_mousewidth))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mLipsThicknessLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_LIPSTHICKNESS);
                    }
                } else if (beautyType.equals(getResources().getString(R.string.beauty_pannel_faceshape))) {
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mFaceBeautyLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FACEBEAUTY);
                    }
                }
            } else if (mSencodGradleType == ITEM_TYPE_FILTTER) {
                if (mBeautyChangeListener != null) {
                    BeautyParams params = new BeautyParams();
                    params.mFilterMixLevel = progress;
                    mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FILTER_MIX_LEVEL);
                }
            }

        }

    }

    private void initSeekBarValue() {
        if (null == mSzSeekBarValue) {
            mSzSeekBarValue = new int[16][24];
            for (int i = 1; i < mSzSeekBarValue[ITEM_TYPE_FILTTER].length; i++) {
                mSzSeekBarValue[ITEM_TYPE_FILTTER][i] = mFilterBasicLevel;
            }
            // 前八个滤镜的推荐值 （其他默认为5）
            mSzSeekBarValue[ITEM_TYPE_FILTTER][1] = 4;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][2] = 8;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][3] = 8;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][4] = 8;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][5] = 10;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][6] = 8;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][7] = 10;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][8] = 5;
            // 设置美颜默认值
            mSzSeekBarValue[ITEM_TYPE_BEAUTY][0] = mBeautyBasicLevel;
            mSzSeekBarValue[ITEM_TYPE_BEAUTY][1] = mBeautyBasicLevel;
            mSzSeekBarValue[ITEM_TYPE_BEAUTY][2] = mBeautyBasicLevel;
            mSzSeekBarValue[ITEM_TYPE_BEAUTY][3] = mWhiteBasicLevel;
            mSzSeekBarValue[ITEM_TYPE_BEAUTY][4] = mRuddyBasicLevel;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public int getFilterProgress(int index) {
        return mSzSeekBarValue[ITEM_TYPE_FILTTER][index];
    }

    @NonNull
    public String[] getBeautyFilterArr() {
        return mFilterTypeString;
    }

    @Override
    public void onClick(View v) {

    }

    public void setCurrentFilterIndex(int index) {
        mSzSecondGradleIndex[ITEM_TYPE_FILTTER] = index;
        if (mSencodGradleType == ITEM_TYPE_FILTTER) {
            ViewGroup group = (ViewGroup) mSecondGradlePicker.getChildAt(0);
            int size = mItemAdapter.getCount();
            for (int i = 0; i < size; i++) {
                View v = group.getChildAt(i);
                if (v instanceof TextView) {
                    if (i == index) {
                        ((TextView) v).setTextColor(mTextColorPrimary);
                    } else {
                        ((TextView) v).setTextColor(Color.WHITE);
                    }
                }
            }

            mThirdGradleIndex = index;
            mSeekbar.setVisibility(View.VISIBLE);
            mSeekBarValue.setVisibility(View.VISIBLE);
            mSeekbar.setProgress(mSzSeekBarValue[ITEM_TYPE_FILTTER][index]);
        }
    }
}
