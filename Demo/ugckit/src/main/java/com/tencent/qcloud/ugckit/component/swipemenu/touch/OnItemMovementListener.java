
package com.tencent.qcloud.ugckit.component.swipemenu.touch;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

public interface OnItemMovementListener {

    int INVALID = 0;
    int LEFT    = ItemTouchHelper.LEFT;
    int UP      = ItemTouchHelper.UP;
    int RIGHT   = ItemTouchHelper.RIGHT;
    int DOWN    = ItemTouchHelper.DOWN;

    /**
     * Can drag and drop the ViewHolder?
     *
     * @param recyclerView     {@link RecyclerView}.
     * @param targetViewHolder target ViewHolder.
     * @return use {@link #LEFT}, {@link #UP}, {@link #RIGHT}, {@link #DOWN}.
     */
    int onDragFlags(RecyclerView recyclerView, RecyclerView.ViewHolder targetViewHolder);

    /**
     * Can swipe and drop the ViewHolder?
     *
     * @param recyclerView     {@link RecyclerView}.
     * @param targetViewHolder target ViewHolder.
     * @return use {@link #LEFT}, {@link #UP}, {@link #RIGHT}, {@link #DOWN}.
     */
    int onSwipeFlags(RecyclerView recyclerView, RecyclerView.ViewHolder targetViewHolder);

}
