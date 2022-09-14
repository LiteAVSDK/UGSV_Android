package com.tencent.xmagic.panel;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.tencent.xmagic.module.XmagicResParser;
import com.tencent.xmagic.module.XmagicUIProperty;
import com.tencent.xmagic.XmagicConstant;
import com.tencent.xmagic.XmagicConstant.BeautyConstant;
import com.tencent.xmagic.XmagicProperty;
import com.tencent.xmagic.XmagicProperty.Category;
import com.tencent.xmagic.XmagicProperty.XmagicPropertyValues;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对XmagicPanelView中的数据进行管理
 * Manage data in XmagicPanelView
 */
public class XmagicPanelDataManager {


    private final Map<XmagicUIProperty.UICategory, List<XmagicUIProperty<?>>> allData = new ArrayMap<>();
    /**
     * 用于存放用户选中的item
     * save user checked item，
     */
    private final Map<String, XmagicUIProperty<?>> selectedItems = new ArrayMap<>();
    /**
     * 用户点击过的 所有美颜的item  和美体item,用于在恢复美颜效果时使用，
     * 因为美颜SDK在页面不可见的时候就需要销毁，可见后又需要重新创建，所以在重新创建后则需要将上次的状态重新设置给新创建的美颜对象
     * All beauty items and body beauty items clicked by the user are used to restore the beauty effect,
     * Because the beauty SDK needs to be destroyed when the page is invisible;
     * and needs to be recreated after it is visible, so after re-creation,
     * the last state needs to be reset to the newly created beauty object
     */
    private final Set<XmagicUIProperty<?>> beautyList = new LinkedHashSet<>();
    /**
     * 用于保存最后一次生效的 动效、美装、分割的item
     * to save the last motion, makeup, or segmentation item
     */
    private XmagicUIProperty<?> lastItem = null;

    private boolean isPanelBeautyOpen = true;

    private XmagicPanelDataManager() {
    }

    private static class ClassHolder {
        static final XmagicPanelDataManager XMAGIC_PANEL_DATA_MANAGER = new XmagicPanelDataManager();
    }

    public static XmagicPanelDataManager getInstance() {
        return ClassHolder.XMAGIC_PANEL_DATA_MANAGER;
    }


    /**
     * 获取去除子列表的 动效 数据集  motion数据集合
     * get motion items
     *
     * @return
     */
    public List<XmagicProperty<?>> getMotionXmagicProperties() {
        return getXmagicPropertyByUICategory(XmagicUIProperty.UICategory.MOTION);
    }


    /**
     * 获取去除子列表的美颜数据集   beauty的数据
     * get beauty items
     *
     * @return
     */
    public List<XmagicProperty<?>> getBeautyXmagicProperties() {
        return getXmagicPropertyByUICategory(XmagicUIProperty.UICategory.BEAUTY);
    }

    /**
     * 获取去除子列表的美体数据集   美体的数据
     * get body items
     *
     * @return
     */
    public List<XmagicProperty<?>> getBodyXmagicProperties() {
        return getXmagicPropertyByUICategory(XmagicUIProperty.UICategory.BODY_BEAUTY);
    }

    /**
     * 根据不同的类型XmagicUIProperty.UICategory 获取对应的数据集合
     * Get the data collection by different types XmagicUIProperty.UICategory
     *
     * @param uiCategory UICategory.BEAUTY UICategory.LUT ....
     * @return 对应的去除了子列表的item集合
     * @return items
     */
    private List<XmagicProperty<?>> getXmagicPropertyByUICategory(XmagicUIProperty.UICategory uiCategory) {
        List<XmagicUIProperty<?>> propertyDataList = allData.get(uiCategory);
        if (propertyDataList != null) {
            List<XmagicProperty<?>> beautyProperties = new ArrayList<>();
            for (XmagicUIProperty<?> data : propertyDataList) {
                if (data.xmagicUIPropertyList != null && data.xmagicUIPropertyList.size() > 0) {
                    for (XmagicUIProperty<?> property : data.xmagicUIPropertyList) {
                        if (property.property != null) {
                            beautyProperties.add(property.property);
                        }
                    }
                } else if (data.property != null) {
                    beautyProperties.add(data.property);
                }
            }
            return beautyProperties;
        }
        return null;
    }


    /**
     * 获取美颜的默认设置项
     * get default beauty settings data
     *
     * @return
     */
    public List<XmagicProperty<?>> getDefaultBeautyData() {
        List<XmagicProperty<?>> defProperty = new ArrayList<>();
        XmagicPropertyValues values1 = new XmagicProperty.XmagicPropertyValues(0, 100, 30, 0, 1);
        defProperty.add(new XmagicProperty<>(Category.BEAUTY, null, null, BeautyConstant.BEAUTY_WHITEN, values1));
        XmagicPropertyValues values2 = new XmagicPropertyValues(0, 100, 50, 0, 1);
        defProperty.add(new XmagicProperty<>(Category.BEAUTY, null, null, BeautyConstant.BEAUTY_SMOOTH, values2));
        XmagicPropertyValues values3 = new XmagicPropertyValues(0, 100, 20, 0, 1);
        defProperty.add(new XmagicProperty<>(Category.BEAUTY, null, null, BeautyConstant.BEAUTY_ENLARGE_EYE, values3));
        XmagicPropertyValues values4 = new XmagicProperty.XmagicPropertyValues(0, 100, 30, 0, 1);
        defProperty.add(new XmagicProperty<>(Category.BEAUTY, "nature", null,
                BeautyConstant.BEAUTY_FACE_NATURE, values4));
        addDefaultBeautyToBeautyList();
        return defProperty;
    }

    /**
     * 将默认美颜的UI数据添加进选中列表中
     * chang UI panel data by default data
     */
    private void addDefaultBeautyToBeautyList() {
        for (XmagicUIProperty<?> xmagicUIProperty : allData.get(XmagicUIProperty.UICategory.BEAUTY)) {
            if (xmagicUIProperty == null) {
                continue;
            }
            if (xmagicUIProperty.property != null
                    && (TextUtils.equals(BeautyConstant.BEAUTY_WHITEN, xmagicUIProperty.property.effKey))) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 30);
                beautyList.add(xmagicUIProperty);
            } else if (xmagicUIProperty.property != null
                    && (TextUtils.equals(BeautyConstant.BEAUTY_SMOOTH, xmagicUIProperty.property.effKey))) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 50);
                beautyList.add(xmagicUIProperty);
            } else if (xmagicUIProperty.property != null
                    && (TextUtils.equals(BeautyConstant.BEAUTY_ENLARGE_EYE, xmagicUIProperty.property.effKey))) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 20);
                beautyList.add(xmagicUIProperty);
            } else if (xmagicUIProperty.xmagicUIPropertyList != null) {
                for (XmagicUIProperty<?> uiProperty : xmagicUIProperty.xmagicUIPropertyList) {
                    if (uiProperty == null) {
                        continue;
                    }
                    if (uiProperty.property != null
                            && (TextUtils.equals(BeautyConstant.BEAUTY_FACE_NATURE, uiProperty.property.effKey))) {
                        changeXmagicPropertyDisPlayValue(uiProperty, 30);
                        beautyList.add(uiProperty);
                    }
                }
            }
        }
    }


    /**
     * 用于修改xmagicUIProperty 中的property 的value
     * Used to modify the value of the property in xmagicUIProperty
     *
     * @param xmagicUIProperty
     * @param disPlayValue
     */
    private void changeXmagicPropertyDisPlayValue(XmagicUIProperty<?> xmagicUIProperty, int disPlayValue) {
        changePropertyDisPlayValue(xmagicUIProperty.property, disPlayValue);
    }

    private void changePropertyDisPlayValue(XmagicProperty<?> xmagicProperty, int disPlayValue) {
        if (xmagicProperty != null && xmagicProperty.effValue != null) {
            XmagicPropertyValues propertyValues = (XmagicProperty.XmagicPropertyValues) xmagicProperty.effValue;
            propertyValues.setCurrentDisplayValue(disPlayValue);
        }
    }


    /**
     * 获取使用过的美颜选项
     * Get the settings of the beauty before the page is invisible
     * 由于SDK在页面处于后台的时候需要进行销毁，当页面切回前台的时候会创建新的xmagicApi对象
     * ，所以在重新创建新的对象的时候，通过此方法拿到上次的设置，进行美颜还原
     * Since the SDK needs to be destroyed when the page is in the background,
     * a new xmagicApi object will be created when the page is switched back to the foreground,
     * so when a new object is recreated, the last setting is obtained through this method to restore the beauty.
     */
    public List<XmagicProperty<?>> getUsedProperty() {
        List<XmagicProperty<?>> xmagicPropertyList = new ArrayList<>();
        for (XmagicUIProperty<?> xmagicUIProperty : beautyList) {
            if (xmagicUIProperty != null && xmagicUIProperty.property != null) {
                xmagicPropertyList.add(xmagicUIProperty.property);
            }
        }
        XmagicProperty<?> lut = new XmagicProperty<>(Category.LUT, XmagicProperty.ID_NONE, "", null, null);
        String resPath = XmagicResParser.getResPath() + "light_assets/template.json";
        XmagicProperty<?> motion = new XmagicProperty<>(Category.MOTION, XmagicProperty.ID_NONE, resPath, null, null);
        XmagicProperty<?> makeup = new XmagicProperty<>(Category.MAKEUP, XmagicProperty.ID_NONE, resPath, null, null);
        xmagicPropertyList.add(lut);
        xmagicPropertyList.add(makeup);
        xmagicPropertyList.add(motion);

        /**
         * 由于动效、美妆、分割不能同时生效，所以在恢复的时候应该设置最后一次添加的（动效||美妆||分割）
         * Since motion , makeup, and segmentation cannot take effect at the same time,
         * the last added (motion ||makeup||segment) should be set when restoring.so use the selectedItems
         */
        for (XmagicUIProperty<?> xmagicUIProperty : selectedItems.values()) {
            if (xmagicUIProperty != null && xmagicUIProperty.property != null) {
                //添加滤镜的数据
                if (xmagicUIProperty.uiCategory == XmagicUIProperty.UICategory.LUT) {
                    xmagicPropertyList.add(xmagicUIProperty.property);
                }
            }
        }
        if (lastItem != null && lastItem.property != null) {
            xmagicPropertyList.add(lastItem.property);
        }
        return xmagicPropertyList;
    }

    /**
     * 获取用于还原Xmagic效果的 数据集合，在xmagicApi中使用此数据还原美颜设置
     * Get a collection of data used to restore Xmagic effects
     *
     * @return
     */
    public synchronized List<XmagicProperty<?>> getRevertXmagicData() {
        List<XmagicProperty<?>> cleanUpData = new ArrayList<>();
        List<XmagicUIProperty<?>> tempBeautyList = new ArrayList<>();  //用于存放，还原美颜时，还依然生效的美白、磨皮、大眼、自然
        for (XmagicUIProperty<?> xmagicUIProperty : beautyList) {
            if (xmagicUIProperty == null) {
                continue;
            }
            if (xmagicUIProperty.property != null
                    && (TextUtils.equals(BeautyConstant.BEAUTY_WHITEN, xmagicUIProperty.property.effKey))) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 30);
                tempBeautyList.add(xmagicUIProperty);
            } else if (xmagicUIProperty.property != null
                    && (TextUtils.equals(BeautyConstant.BEAUTY_SMOOTH, xmagicUIProperty.property.effKey))) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 50);
                tempBeautyList.add(xmagicUIProperty);
            } else if (xmagicUIProperty.property != null
                    && (TextUtils.equals(BeautyConstant.BEAUTY_ENLARGE_EYE, xmagicUIProperty.property.effKey))) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 20);
                tempBeautyList.add(xmagicUIProperty);
            } else if (xmagicUIProperty.property != null
                    && (TextUtils.equals(BeautyConstant.BEAUTY_FACE_NATURE, xmagicUIProperty.property.effKey))) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 30);
                tempBeautyList.add(xmagicUIProperty);
            } else {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 0);
            }
            if (xmagicUIProperty.property != null && xmagicUIProperty.property.effValue != null) {
                cleanUpData.add(xmagicUIProperty.property);
            }
        }
        selectedItems.clear();
        beautyList.clear();
        beautyList.addAll(tempBeautyList);
        lastItem = null;
        List<XmagicUIProperty<?>> lutList = allData.get(XmagicUIProperty.UICategory.LUT);
        if (lutList != null) {
            for (XmagicUIProperty xmagicUIProperty : lutList) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 60);
            }
        }
        List<XmagicUIProperty<?>> makeupList = allData.get(XmagicUIProperty.UICategory.MAKEUP);
        if (makeupList != null) {
            for (XmagicUIProperty xmagicUIProperty : makeupList) {
                changeXmagicPropertyDisPlayValue(xmagicUIProperty, 60);
            }
        }
        List<XmagicUIProperty<?>> list = allData.get(XmagicUIProperty.UICategory.BEAUTY);
        if (list != null && list.size() > 1) {
            XmagicUIProperty<?> xmagicUIProperty = list.get(1);
            selectedItems.put(xmagicUIProperty.uiCategory.getDescription(), xmagicUIProperty);
        }
        XmagicProperty<?> lut = new XmagicProperty<>(Category.LUT, XmagicProperty.ID_NONE, "", null, null);
        String resPath = XmagicResParser.getResPath() + "light_assets/template.json";
        XmagicProperty<?> motion = new XmagicProperty<>(Category.MOTION, XmagicProperty.ID_NONE, resPath, null, null);
        XmagicProperty<?> makeup = new XmagicProperty<>(Category.MAKEUP, XmagicProperty.ID_NONE, resPath, null, null);
        cleanUpData.add(lut);
        cleanUpData.add(makeup);
        cleanUpData.add(motion);
        return cleanUpData;
    }

    /**
     * 获取用于关闭美颜效果的items
     * Get the items used to turn off the beauty effect
     *
     * @return
     */
    public List<XmagicProperty<?>> getCloseBeautyItems() {
        List<XmagicProperty<?>> xmagicPropertyList = new ArrayList<>();
        for (XmagicUIProperty<?> xmagicUIProperty : beautyList) {
            if (xmagicUIProperty != null
                    && xmagicUIProperty.uiCategory == XmagicUIProperty.UICategory.BEAUTY
                    && xmagicUIProperty.property != null) {
                try {
                    XmagicProperty<?> xmagicProperty = xmagicUIProperty.property.clone();
                    changePropertyDisPlayValue(xmagicProperty, 0);
                    xmagicPropertyList.add(xmagicProperty);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return xmagicPropertyList;
    }

    /**
     * 获取重新打开美颜效果的items
     * Get the items that reopen the beauty effect
     *
     * @return
     */
    public List<XmagicProperty<?>> getOpenBeautyItems() {
        List<XmagicProperty<?>> xmagicPropertyList = new ArrayList<>();
        for (XmagicUIProperty<?> xmagicUIProperty : beautyList) {
            if (xmagicUIProperty != null
                    && xmagicUIProperty.uiCategory == XmagicUIProperty.UICategory.BEAUTY
                    && xmagicUIProperty.property != null) {
                xmagicPropertyList.add(xmagicUIProperty.property);
            }
        }
        return xmagicPropertyList;
    }


    public void addAllDataItem(XmagicUIProperty.UICategory uiCategory, List<XmagicUIProperty<?>> list) {
        allData.put(uiCategory, list);
    }

    /**
     * 根据UICategory 获取对应的列表数据
     *
     * @param uiCategory
     * @return
     */
    public List<XmagicUIProperty<?>> getXmagicUIProperty(XmagicUIProperty.UICategory uiCategory) {
        return allData.get(uiCategory);
    }

    public Set<XmagicUIProperty<?>> getBeautyList() {
        return beautyList;
    }

    public Map<String, XmagicUIProperty<?>> getSelectedItems() {
        return selectedItems;
    }

    public void setLastItem(XmagicUIProperty<?> lastItem) {
        this.lastItem = lastItem;
    }

    public XmagicUIProperty<?> getLastItem() {
        return lastItem;
    }

    /**
     * @param uiCategory 菜单的类型，例如美颜、美体、滤镜、动效、美妆、分割
     * @return 返回对应的数据，可能包括二级菜单
     */
    public List<XmagicUIProperty<?>> getProperties(XmagicUIProperty.UICategory uiCategory) {
        return allData.get(uiCategory);
    }

    public boolean isPanelBeautyOpen() {
        return isPanelBeautyOpen;
    }

    public void setPanelBeautyOpen(boolean panelBeautyOpen) {
        isPanelBeautyOpen = panelBeautyOpen;
    }

    public void clearData() {
        isPanelBeautyOpen = true;
        beautyList.clear();
        selectedItems.clear();
        lastItem = null;
    }
}
