package com.tencent.qcloud.ugckit.module.effect.paster;

public class TCPasterInfo {
    private String iconPath;
    private String name;
    private String pasterPath;
    private int    pasterType;

    public int getPasterType() {
        return pasterType;
    }

    public void setPasterType(int pasterType) {
        this.pasterType = pasterType;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
