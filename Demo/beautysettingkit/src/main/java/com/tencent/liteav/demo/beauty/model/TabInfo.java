package com.tencent.liteav.demo.beauty.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 美颜面板 tab 相关属性
 * 成员变量名和 assets/default_beauty_data.json 的 key 相对应，便于 json 解析
 */
public class TabInfo {

    @SerializedName("tab_id")
    private long           mTabId;                            // long, tab id，tab 唯一标识
    @SerializedName("tab_type")
    private int            mTabType;                           // int, tab类型
    @SerializedName("tab_item_name_size")
    private int            mTabItemNameSize;                 // int, item文字大小
    @SerializedName("tab_item_icon_width")
    private int            mTabItemIconWidth;                // int, item icon 宽度
    @SerializedName("tab_item_icon_height")
    private int            mTabItemIconHeight;               // int, item icon 高度
    @SerializedName("tab_item_level_hint_size")
    private int            mTabItemLevelHintSize;           // int, 进度条提示文字大小
    @SerializedName("tab_item_level_value_size")
    private int            mTabItemLevelValueSize;          // int, 进度条值文字大小
    @SerializedName("tab_name")
    private String         mTabName;                        // string, tab 名称
    @SerializedName("tab_item_name_color_normal")
    private String         mTabItemNameColorNormal;      // string, item 文件常规颜色
    @SerializedName("tab_item_name_color_select")
    private String         mTabItemNameColorSelect;      // string, item 文件选中颜色
    @SerializedName("tab_item_level_hint_color")
    private String         mTabItemLevelHintColor;       // string, 进度条提示文字颜色
    @SerializedName("tab_item_level_value_color")
    private String         mTabItemLevelValueColor;      // string, 进度条值文字颜色
    @SerializedName("tab_item_level_progress_drawable")
    private String         mTabItemLevelProgressDrawable;// string, 进度条背景颜色
    @SerializedName("tab_item_level_progress_thumb")
    private String         mTabItemLevelProgressThumb;   // string, 进度条 bar 颜色
    @SerializedName("tab_item_list_default_selected_index")
    private int            mTabItemListDefaultSelectedIndex;       // int, 默认选中的item
    @SerializedName("tab_item_list")
    private List<ItemInfo> mTabItemList;

    public long getTabId() {
        return mTabId;
    }

    public int getTabType() {
        return mTabType;
    }

    public String getTabName() {
        return mTabName;
    }

    public int getTabItemNameSize() {
        return mTabItemNameSize;
    }

    public String getTabItemNameColorNormal() {
        return mTabItemNameColorNormal;
    }

    public String getTabItemNameColorSelect() {
        return mTabItemNameColorSelect;
    }

    public int getTabItemIconWidth() {
        return mTabItemIconWidth;
    }

    public int getTabItemIconHeight() {
        return mTabItemIconHeight;
    }

    public String getTabItemLevelHintColor() {
        return mTabItemLevelHintColor;
    }

    public int getTabItemLevelHintSize() {
        return mTabItemLevelHintSize;
    }

    public String getTabItemLevelValueColor() {
        return mTabItemLevelValueColor;
    }

    public int getTabItemLevelValueSize() {
        return mTabItemLevelValueSize;
    }

    public String getTabItemLevelProgressDrawable() {
        return mTabItemLevelProgressDrawable;
    }

    public String getTabItemLevelProgressThumb() {
        return mTabItemLevelProgressThumb;
    }

    public int getTabItemListDefaultSelectedIndex() {
        return mTabItemListDefaultSelectedIndex;
    }

    public List<ItemInfo> getTabItemList() {
        return mTabItemList;
    }

    @Override
    public String toString() {
        return "TabInfo{" + "mTabId=" + mTabId + ", mTabName='" + mTabName + '\'' + ", mTabItemNameSize="
                + mTabItemNameSize + ", mTabItemNameColorNormal='" + mTabItemNameColorNormal + '\''
                + ", mTabItemNameColorSelect='" + mTabItemNameColorSelect + '\'' + ", mTabItemIconWidth="
                + mTabItemIconWidth + ", mTabItemIconHeight=" + mTabItemIconHeight + ", mTabItemLevelHintColor='"
                + mTabItemLevelHintColor + '\'' + ", mTabItemLevelHintSize=" + mTabItemLevelHintSize
                + ", mTabItemLevelValueColor='" + mTabItemLevelValueColor + '\'' + ", mTabItemLevelValueSize="
                + mTabItemLevelValueSize + ", mTabItemLevelProgressDrawable='" + mTabItemLevelProgressDrawable + '\''
                + ", mTabItemLevelProgressThumb='" + mTabItemLevelProgressThumb + '\''
                + ", mTabItemListDefaultSelectedIndex='" + mTabItemListDefaultSelectedIndex + '\'' + ", mTabItemList="
                + mTabItemList + '}';
    }
}
