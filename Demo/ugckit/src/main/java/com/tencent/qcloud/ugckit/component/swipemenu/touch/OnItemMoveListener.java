package com.tencent.qcloud.ugckit.component.swipemenu.touch;

public interface OnItemMoveListener {

    /**
     * When drag and drop the callback.
     *
     * @param fromPosition start position.
     * @param toPosition   target position.
     * @return To deal with the returns true, false otherwise.
     */
    boolean onItemMove(int fromPosition, int toPosition);

    /**
     * When items should be removed when the callback.
     *
     * @param position swipe position.
     */
    void onItemDismiss(int position);

}
