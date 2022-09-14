package com.tencent.qcloud.ugckit.component.swipemenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.tencent.qcloud.ugckit.R;

import java.util.List;


public abstract class SwipeMenuAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private SwipeMenuCreator             mSwipeMenuCreator;             // Swipe menu creator。
    private OnSwipeMenuItemClickListener mSwipeMenuItemClickListener;   // Swipe menu click listener。

    /**
     * Set to create menu listener.
     *
     * @param swipeMenuCreator listener.
     */
    void setSwipeMenuCreator(SwipeMenuCreator swipeMenuCreator) {
        this.mSwipeMenuCreator = swipeMenuCreator;
    }

    /**
     * Set to click menu listener.
     *
     * @param swipeMenuItemClickListener listener.
     */
    void setSwipeMenuItemClickListener(OnSwipeMenuItemClickListener swipeMenuItemClickListener) {
        this.mSwipeMenuItemClickListener = swipeMenuItemClickListener;
    }

    @NonNull
    @Override
    public final VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View contentView = onCreateContentView(parent, viewType);
        if (mSwipeMenuCreator != null) {
            SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .ugckit_swipe_item, parent, false);

            SwipeMenu swipeLeftMenu = new SwipeMenu(swipeMenuLayout, viewType);
            SwipeMenu swipeRightMenu = new SwipeMenu(swipeMenuLayout, viewType);

            mSwipeMenuCreator.onCreateMenu(swipeLeftMenu, swipeRightMenu, viewType);

            int leftMenuCount = swipeLeftMenu.getMenuItems().size();
            if (leftMenuCount > 0) {
                SwipeMenuView swipeLeftMenuView = (SwipeMenuView) swipeMenuLayout.findViewById(R.id.swipe_left);
                // noinspection WrongConstant
                swipeLeftMenuView.setOrientation(swipeLeftMenu.getOrientation());
                swipeLeftMenuView.bindMenu(swipeLeftMenu, SwipeMenuRecyclerView.LEFT_DIRECTION);
                swipeLeftMenuView.bindMenuItemClickListener(mSwipeMenuItemClickListener, swipeMenuLayout);
            }

            int rightMenuCount = swipeRightMenu.getMenuItems().size();
            if (rightMenuCount > 0) {
                SwipeMenuView swipeRightMenuView = (SwipeMenuView) swipeMenuLayout.findViewById(R.id.swipe_right);
                // noinspection WrongConstant
                swipeRightMenuView.setOrientation(swipeRightMenu.getOrientation());
                swipeRightMenuView.bindMenu(swipeRightMenu, SwipeMenuRecyclerView.RIGHT_DIRECTION);
                swipeRightMenuView.bindMenuItemClickListener(mSwipeMenuItemClickListener, swipeMenuLayout);
            }

            if (leftMenuCount > 0 || rightMenuCount > 0) {
                ViewGroup viewGroup = (ViewGroup) swipeMenuLayout.findViewById(R.id.swipe_content);
                viewGroup.addView(contentView);
                contentView = swipeMenuLayout;
            }
        }
        return onCompatCreateViewHolder(contentView, viewType);
    }

    /**
     * Create view for item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    public abstract View onCreateContentView(ViewGroup parent, int viewType);

    /**
     * Instead {@link #onCreateViewHolder(ViewGroup, int)}.
     *
     * @param realContentView Is this Item real view, {@link SwipeMenuLayout} or {@link #onCreateContentView(ViewGroup, int)}.
     * @param viewType        The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #onCompatBindViewHolder(RecyclerView.ViewHolder, int, List)
     */
    @NonNull
    public abstract VH onCompatCreateViewHolder(View realContentView, int viewType);

    @Override
    public final void onBindViewHolder(@NonNull VH holder, int position, List<Object> payloads) {
        View itemView = holder.itemView;
        if (itemView instanceof SwipeMenuLayout) {
            SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) itemView;
            int childCount = swipeMenuLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = swipeMenuLayout.getChildAt(i);
                if (childView instanceof SwipeMenuView) {
                    ((SwipeMenuView) childView).bindAdapterViewHolder(holder);
                }
            }
        }
        onCompatBindViewHolder(holder, position, payloads);
    }

    /**
     * Instead {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)}.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in
     *                 the data set.
     * @param position The position of the item within the adapter's data set.
     * @param payloads A non-null list of merged payloads. Can be empty list if requires full update.
     * @see #onCompatBindViewHolder(RecyclerView.ViewHolder, int, List)
     */
    public void onCompatBindViewHolder(VH holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, position);
    }
}