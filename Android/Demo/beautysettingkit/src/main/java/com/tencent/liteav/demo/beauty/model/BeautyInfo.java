package com.tencent.liteav.demo.beauty.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 美颜面板相关属性
 * 成员变量名和 assets/default_beauty_data.json 的 key 相对应，便于 json 解析
 */
public class BeautyInfo {

    @SerializedName("beauty_tab_name_size")
    private int           mBeautyTabNameSize;        // int, tab文字大小
    @SerializedName("beauty_tab_name_width")
    private int           mBeautyTabNameWidth;       // int, tab 宽度
    @SerializedName("beauty_tab_name_height")
    private int           mBeautyTabNameHeight;      // int, tab 高度
    @SerializedName("beauty_bg")
    private String        mBeautyBg;                 // color/drawable, beauty布局的背景色值
    @SerializedName("beauty_tab_name_color_normal")
    private String        mBeautyTabNameColorNormal; // color, tab文字常规颜色
    @SerializedName("beauty_tab_name_color_select")
    private String        mBeautyTabNameColorSelect; // color, tab文字选中颜色
    @SerializedName("beauty_tab_list")
    private List<TabInfo> mBeautyTabList;

    public String getBeautyBg() {
        return mBeautyBg;
    }

    public void setBeautyBg(String beautyBg) {
        this.mBeautyBg = beautyBg;
    }

    public int getBeautyTabNameWidth() {
        return mBeautyTabNameWidth;
    }

    public int getBeautyTabNameHeight() {
        return mBeautyTabNameHeight;
    }

    public String getBeautyTabNameColorNormal() {
        return mBeautyTabNameColorNormal;
    }

    public String getBeautyTabNameColorSelect() {
        return mBeautyTabNameColorSelect;
    }

    public int getBeautyTabNameSize() {
        return mBeautyTabNameSize;
    }

    public List<TabInfo> getBeautyTabList() {
        return mBeautyTabList;
    }

    @Override
    public String toString() {
        return "BeautyInfo{" + ", mBeautyBg='" + mBeautyBg + '\'' + ", mBeautyTabNameWidth=" + mBeautyTabNameWidth
                + ", mBeautyTabNameHeight=" + mBeautyTabNameHeight + ", mBeautyTabNameColorNormal='"
                + mBeautyTabNameColorNormal + '\'' + ", mBeautyTabNameColorSelect='" + mBeautyTabNameColorSelect + '\''
                + ", mBeautyTabNameSize=" + mBeautyTabNameSize + ", mBeautyTabList=" + mBeautyTabList + '}';
    }
}
