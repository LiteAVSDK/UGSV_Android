package com.tencent.qcloud.ugckit.module.effect.paster.view;

import android.content.Context;
import android.util.AttributeSet;

import com.tencent.qcloud.ugckit.component.floatlayer.FloatLayerView;

public class PasterView extends FloatLayerView {
    public static int TYPE_CHILD_VIEW_PASTER          = 1;
    public static int TYPE_CHILD_VIEW_ANIMATED_PASTER = 2;

    private int    mChildType;
    private String mPasterName;
    private String mPasterPath;  //预览界面上显示的图片
    private String mIconPath;    //缩略图

    public PasterView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public PasterView(Context context) {
        super(context, null);
    }

    public PasterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String getIconPath() {
        return mIconPath;
    }

    public void setIconPath(String path) {
        mIconPath = path;
    }

    public int getChildType() {
        return mChildType;
    }

    public void setChildType(int type) {
        mChildType = type;
    }

    public String getPasterName() {
        return mPasterName;
    }

    public void setPasterName(String name) {
        mPasterName = name;
    }

    public String getPasterPath() {
        return mPasterPath;
    }

    public void setPasterPath(String mPasterPath) {
        this.mPasterPath = mPasterPath;
    }

}
