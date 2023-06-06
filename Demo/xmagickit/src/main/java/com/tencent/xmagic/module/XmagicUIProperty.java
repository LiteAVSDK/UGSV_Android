package com.tencent.xmagic.module;

import com.tencent.xmagic.XMagicImpl;
import com.tencent.xmagic.XmagicProperty;
import com.tencent.xmagic.XmagicProperty.Category;
import com.tencent.xmagic.demo.R;
import com.tencent.xmagic.download.MotionDLModel;

import java.util.List;

public class XmagicUIProperty<V> {
    public XmagicProperty<V> property;
    public String displayName;
    public int thumbDrawable;
    public String thumbImagePath;
    public UICategory uiCategory;
    public String rootDisplayName;
    public List<XmagicUIProperty<V>> xmagicUIPropertyList = null;
    public MotionDLModel dlModel = null;

    /**
     * 主构造函数
     *
     * @param category
     * @param displayName
     * @param id
     * @param resPath
     * @param thumbDrawable
     * @param effKey
     * @param value
     */
    public XmagicUIProperty(UICategory category, String displayName, String id, String resPath
            , int thumbDrawable, String effKey, V value) {
        property = new XmagicProperty<>(category.getXmagicCategory(), id, resPath, effKey, value);
        this.displayName = displayName;
        this.thumbDrawable = thumbDrawable;
        this.uiCategory = category;
    }

    /**
     * 二级列表item构造函数
     *
     * @param category
     * @param displayName
     * @param id
     * @param resPath
     * @param thumbDrawable
     * @param effKey
     * @param value
     * @param rootDisplayName
     */
    public XmagicUIProperty(UICategory category, String displayName, String id, String resPath, int thumbDrawable
            , String effKey, V value, String rootDisplayName) {
        property = new XmagicProperty<>(category.getXmagicCategory(), id, resPath, effKey, value);
        this.displayName = displayName;
        this.thumbDrawable = thumbDrawable;
        this.uiCategory = category;
        this.rootDisplayName = rootDisplayName;
    }


    /**
     * 美颜构造函数
     *
     * @param category
     * @param displayName
     * @param thumbDrawable
     * @param effKey
     * @param value
     */
    public XmagicUIProperty(UICategory category, String displayName, int thumbDrawable, String effKey, V value) {
        this(category, displayName, null, null, thumbDrawable, effKey, value);
    }

    /**
     * 仅做UI数据填充的构造函数
     *
     * @param displayName
     * @param thumbDrawable
     * @param uiCategory
     */
    public XmagicUIProperty(String displayName, int thumbDrawable, UICategory uiCategory) {
        this.displayName = displayName;
        this.thumbDrawable = thumbDrawable;
        this.uiCategory = uiCategory;
    }

    public enum UICategory {
        BEAUTY(true),
        BODY_BEAUTY(true),
        LUT(true),
        MOTION(true),
        MAKEUP(true),
        SEGMENTATION(true),
        KV(false);

        private final boolean canShowOnUI;

        UICategory(boolean canShowOnUI) {
            this.canShowOnUI = canShowOnUI;
        }

        public String getDescription() {
            switch (UICategory.this) {
                case BEAUTY:
                    return XMagicImpl.applicationContext.getString(R.string.xmagic_panel_tab_beauty);
                case BODY_BEAUTY:
                    return XMagicImpl.applicationContext.getString(R.string.xmagic_panel_tab_body_beauty);
                case LUT:
                    return XMagicImpl.applicationContext.getString(R.string.xmagic_panel_tab_lut);
                case SEGMENTATION:
                    return XMagicImpl.applicationContext.getString(R.string.xmagic_panel_tab_segmentation);
                case MOTION:
                    return XMagicImpl.applicationContext.getString(R.string.xmagic_panel_tab_motion);
                case MAKEUP:
                    return XMagicImpl.applicationContext.getString(R.string.xmagic_panel_tab_makeup);
                default:
                    return XMagicImpl.applicationContext.getString(R.string.xmagic_panel_tab_kv);
            }
        }

        public boolean isCanShowOnUI() {
            return canShowOnUI;
        }

        public Category getXmagicCategory() {
            switch (UICategory.this) {
                case BEAUTY:
                    return Category.BEAUTY;
                case BODY_BEAUTY:
                    return Category.BODY_BEAUTY;
                case LUT:
                    return Category.LUT;
                case SEGMENTATION:
                    return Category.SEGMENTATION;
                case MOTION:
                    return Category.MOTION;
                case MAKEUP:
                    return Category.MAKEUP;
                default:
                    return Category.KV;
            }
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
