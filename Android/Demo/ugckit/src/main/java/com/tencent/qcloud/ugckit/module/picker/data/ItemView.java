package com.tencent.qcloud.ugckit.module.picker.data;

public class ItemView {
    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public interface OnAddListener {
        void onAdd(TCVideoFileInfo fileInfo);
    }
}
