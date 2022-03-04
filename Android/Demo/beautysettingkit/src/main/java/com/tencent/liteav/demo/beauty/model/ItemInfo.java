package com.tencent.liteav.demo.beauty.model;

import com.google.gson.annotations.SerializedName;

/**
 * 美颜面板 item 相关属性
 * 成员变量名和 assets/default_beauty_data.json 的 key 相对应，便于 json 解析
 */
public class ItemInfo {

    @SerializedName("item_id")
    private long mItemId;               // long, item id，item 唯一标识

    @SerializedName("item_type")
    private int mItemType;              // int, item 类型，item 的功能
    @SerializedName("item_level")
    private int mItemLevel;             // int, 特效级别，-1代表无特效级别，即不显示SeekBar

    @SerializedName("item_name")
    private String mItemName;           // string, item 名称
    @SerializedName("item_material_url")
    private String mItemMaterialUrl;   // string, 素材 url
    @SerializedName("item_material_path")
    private String mItemMaterialPath;  // string, 素材本地路径
    @SerializedName("item_icon_normal")
    private String mItemIconNormal;    // drawable, item 常规 icon
    @SerializedName("item_icon_select")
    private String mItemIconSelect;    // drawable, item 选中 icon

    public long getItemId() {
        return mItemId;
    }

    public int getItemType() {
        return mItemType;
    }

    public String getItemName() {
        return mItemName;
    }

    public String getItemIconNormal() {
        return mItemIconNormal;
    }

    public String getItemIconSelect() {
        return mItemIconSelect;
    }

    public void setItemLevel(int itemLevel) {
        this.mItemLevel = itemLevel;
    }

    public int getItemLevel() {
        return mItemLevel;
    }

    public String getItemMaterialUrl() {
        return mItemMaterialUrl;
    }

    public String getItemMaterialPath() {
        return mItemMaterialPath;
    }

    public void setItemMaterialPath(String itemMaterialPath) {
        this.mItemMaterialPath = itemMaterialPath;
    }

    @Override
    public String toString() {
        return "ItemInfo{" + "mItemId=" + mItemId + ", mItemType=" + mItemType + ", mItemName='" + mItemName + '\''
                + ", mItemMaterialUrl='" + mItemMaterialUrl + '\'' + ", mItemLevel=" + mItemLevel
                + ", mItemIconNormal='" + mItemIconNormal + '\'' + ", mItemIconSelect='" + mItemIconSelect + '\''
                + ", mItemMaterialPath='" + mItemMaterialPath + '\'' + '}';
    }
}
