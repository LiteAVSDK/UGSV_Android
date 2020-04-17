package com.tencent.qcloud.ugckit;

import android.graphics.Bitmap;

import com.tencent.liteav.demo.beauty.IBeautyKit;
import com.tencent.qcloud.ugckit.module.record.OnFilterScrollViewListener;
import com.tencent.ugc.TXUGCRecord;

public class UGCBeautyKit implements IBeautyKit {
    public TXUGCRecord mTXUGCRecord;
    private OnFilterScrollViewListener mListener;

    public UGCBeautyKit(TXUGCRecord record) {
        mTXUGCRecord = record;
    }

    @Override
    public void setFilter(Bitmap filterImage, int index) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setFilter(filterImage);
        }
        if (mListener != null) {
            mListener.onFilerChange(filterImage, index);
        }
    }

    @Override
    public void setFilterStrength(float strength) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setFilterStrength(strength / 10.0f);
        }
    }

    @Override
    public void setGreenScreenFile(String path) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setGreenScreenFile(path);
        }
    }

    @Override
    public void setBeautyStyle(int beautyStyle) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setBeautyStyle(beautyStyle);
        }
    }

    @Override
    public void setBeautyLevel(int beautyLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setBeautyLevel(beautyLevel);
        }
    }

    @Override
    public void setWhitenessLevel(int whitenessLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setWhitenessLevel(whitenessLevel);
        }
    }

    @Override
    public void setRuddyLevel(int ruddyLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setRuddyLevel(ruddyLevel);
        }
    }

    @Override
    public void setEyeScaleLevel(int eyeScaleLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setEyeScaleLevel(eyeScaleLevel);
        }
    }

    @Override
    public void setFaceSlimLevel(int faceSlimLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setFaceSlimLevel(faceSlimLevel);
        }
    }

    @Override
    public void setFaceVLevel(int faceVLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setFaceVLevel(faceVLevel);
        }
    }

    @Override
    public void setChinLevel(int chinLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setChinLevel(chinLevel);
        }
    }

    @Override
    public void setFaceShortLevel(int faceShortLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setFaceShortLevel(faceShortLevel);
        }
    }

    @Override
    public void setNoseSlimLevel(int noseSlimLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setNoseSlimLevel(noseSlimLevel);
        }
    }

    @Override
    public void setEyeLightenLevel(int eyeLightenLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setEyeLightenLevel(eyeLightenLevel);
        }
    }

    @Override
    public void setToothWhitenLevel(int level) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setToothWhitenLevel(level);
        }
    }

    @Override
    public void setWrinkleRemoveLevel(int wrinkleRemoveLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setWrinkleRemoveLevel(wrinkleRemoveLevel);
        }
    }

    @Override
    public void setPounchRemoveLevel(int pounchRemoveLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setPounchRemoveLevel(pounchRemoveLevel);
        }
    }

    @Override
    public void setSmileLinesRemoveLevel(int smileLinesRemoveLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setSmileLinesRemoveLevel(smileLinesRemoveLevel);
        }
    }

    @Override
    public void setForeheadLevel(int foreheadLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setForeheadLevel(foreheadLevel);
        }
    }

    @Override
    public void setEyeDistanceLevel(int eyeDistanceLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setEyeDistanceLevel(eyeDistanceLevel);
        }
    }

    @Override
    public void setEyeAngleLevel(int eyeAngleLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setEyeAngleLevel(eyeAngleLevel);
        }
    }

    @Override
    public void setMouthShapeLevel(int mouthShapeLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setMouthShapeLevel(mouthShapeLevel);
        }
    }

    @Override
    public void setNoseWingLevel(int noseWingLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setNoseWingLevel(noseWingLevel);
        }
    }

    @Override
    public void setNosePositionLevel(int nosePositionLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setNosePositionLevel(nosePositionLevel);
        }
    }

    @Override
    public void setLipsThicknessLevel(int lipsThicknessLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setLipsThicknessLevel(lipsThicknessLevel);
        }
    }

    @Override
    public void setFaceBeautyLevel(int faceBeautyLevel) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setFaceBeautyLevel(faceBeautyLevel);
        }
    }

    @Override
    public void setMotionTmpl(String tmplPath) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setMotionTmpl(tmplPath);
        }
    }

    @Override
    public void setMotionMute(boolean motionMute) {
        if (mTXUGCRecord != null) {
            mTXUGCRecord.getBeautyManager().setMotionMute(motionMute);
        }
    }

    public void setOnFilterScrollViewListener(OnFilterScrollViewListener listener) {
        mListener = listener;
    }
}
