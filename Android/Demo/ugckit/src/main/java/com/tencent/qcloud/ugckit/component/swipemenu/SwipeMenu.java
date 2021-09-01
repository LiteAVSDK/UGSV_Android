package com.tencent.qcloud.ugckit.component.swipemenu;

import android.content.Context;
import androidx.annotation.IntDef;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class SwipeMenu {

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL   = LinearLayout.VERTICAL;

    private int                 mViewType;
    private int                 orientation = HORIZONTAL;
    private SwipeMenuLayout     mSwipeMenuLayout;
    private List<SwipeMenuItem> mSwipeMenuItems;

    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public SwipeMenu(SwipeMenuLayout swipeMenuLayout, int viewType) {
        this.mSwipeMenuLayout = swipeMenuLayout;
        this.mViewType = viewType;
        this.mSwipeMenuItems = new ArrayList<>(2);
    }

    /**
     * Set a percentage.
     *
     * @param openPercent such as 0.5F.
     */
    public void setOpenPercent(float openPercent) {
        if (openPercent != mSwipeMenuLayout.getOpenPercent()) {
            openPercent = openPercent > 1 ? 1 : (openPercent < 0 ? 0 : openPercent);
            mSwipeMenuLayout.setOpenPercent(openPercent);
        }
    }

    /**
     * The duration of the set.
     *
     * @param scrollerDuration such 500.
     */
    public void setScrollerDuration(int scrollerDuration) {
        this.mSwipeMenuLayout.setScrollerDuration(scrollerDuration);
    }

    /**
     * Set the menu orientation.
     *
     * @param orientation use {@link SwipeMenu#HORIZONTAL} or {@link SwipeMenu#VERTICAL}.
     * @see SwipeMenu#HORIZONTAL
     * @see SwipeMenu#VERTICAL
     */
    public void setOrientation(@OrientationMode int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL)
            throw new IllegalArgumentException("Use SwipeMenu#HORIZONTAL or SwipeMenu#VERTICAL.");
        this.orientation = orientation;
    }

    /**
     * Get the menu orientation.
     *
     * @return {@link SwipeMenu#HORIZONTAL} or {@link SwipeMenu#VERTICAL}.
     */
    @OrientationMode
    public int getOrientation() {
        return orientation;
    }

    public void addMenuItem(SwipeMenuItem item) {
        mSwipeMenuItems.add(item);
    }

    public void removeMenuItem(SwipeMenuItem item) {
        mSwipeMenuItems.remove(item);
    }

    public List<SwipeMenuItem> getMenuItems() {
        return mSwipeMenuItems;
    }

    public SwipeMenuItem getMenuItem(int index) {
        return mSwipeMenuItems.get(index);
    }

    public Context getContext() {
        return mSwipeMenuLayout.getContext();
    }

    public int getViewType() {
        return mViewType;
    }
}
