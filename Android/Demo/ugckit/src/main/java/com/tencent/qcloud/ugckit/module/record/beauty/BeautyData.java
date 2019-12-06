package com.tencent.qcloud.ugckit.module.record.beauty;

public class BeautyData {
    public static final int ITEM_TYPE_BEAUTY = 0;
    public static final int ITEM_TYPE_FILTTER = 1;
    public static final int ITEM_TYPE_MOTION = 2;
    public static final int ITEM_TYPE_KOUBEI = 3;
    public static final int ITEM_TYPE_GREEN = 4;

    public int icon;
    public String text;

    public BeautyData(int icon, String text) {
        this.icon = icon;
        this.text = text;
    }
}
