package com.tencent.qcloud.ugckit.module.record.beauty;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * 美颜参数
 */
public class BeautyParams {
    public float mExposure = 0;
    public int mBeautyLevel = 4;
    public int mWhiteLevel = 1;
    public int mRuddyLevel = 0;
    public int mBeautyStyle = 0;
    public int mFilterMixLevel = 0;
    public int mBigEyeLevel;
    public int mFaceSlimLevel;
    public int mNoseScaleLevel;
    public int mChinSlimLevel;
    public int mFaceVLevel;
    public int mFaceShortLevel;
    public int mEyeLightenLevel = 0;                 // 亮眼
    public int mToothWhitenLevel = 0;                // 白牙
    public int mWrinkleRemoveLevel = 0;              // 祛皱
    public int mPounchRemoveLevel = 0;               // 祛眼袋
    public int mSmileLinesRemoveLevel = 0;             // 去法令纹
    public int mForeheadLevel = 0;                   // 发际线
    public int mEyeDistanceLevel = 0;                // 眼距
    public int mEyeAngleLevel = 0;                   // 眼角
    public int mMouthShapeLevel = 0;                 // 嘴型
    public int mNoseWingLevel = 0;                   // 鼻翼
    public int mNosePositionLevel = 0;               // 鼻子位置
    public int mLipsThicknessLevel = 0;              // 嘴唇厚度
    public int mFaceBeautyLevel = 0;                 // 脸型

    public int mLongLegLevel = 0;                    // 长腿
    public int mThinWaistLevel = 0;                  // 瘦腰
    public int mThinBodyLevel = 0;                   // 瘦体
    public int mThinShoulderLevel = 0;               // 瘦肩

    @Nullable
    public Bitmap mFilterBmp;
    public String mMotionTmplPath;
    public String mGreenFile;
    public int filterIndex;
}
