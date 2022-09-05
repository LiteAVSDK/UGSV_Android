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
        BEAUTY(XMagicImpl.applicationContext.getString(R.string.xmagic_pannel_tab1), true),
        BODY_BEAUTY(XMagicImpl.applicationContext.getString(R.string.xmagic_pannel_tab2), true),
        LUT(XMagicImpl.applicationContext.getString(R.string.xmagic_pannel_tab3), true),
        MOTION(XMagicImpl.applicationContext.getString(R.string.xmagic_pannel_tab4), true),
        MAKEUP(XMagicImpl.applicationContext.getString(R.string.xmagic_pannel_tab5), true),
        SEGMENTATION(XMagicImpl.applicationContext.getString(R.string.xmagic_pannel_tab6), true),
        KV(XMagicImpl.applicationContext.getString(R.string.xmagic_pannel_tab7), false);

        private final String description;
        private final boolean canShowOnUI;

        UICategory(String description, boolean canShowOnUI) {
            this.description = description;
            this.canShowOnUI = canShowOnUI;
        }

        public String getDescription() {
            return description;
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
            return name() + "(" + description + ")";

        }
    }
}
