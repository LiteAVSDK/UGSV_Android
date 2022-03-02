package com.tencent.qcloud.ugckit.module.picker.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.picker.data.ItemView;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.component.swipemenu.MenuAdapter;
import com.tencent.qcloud.ugckit.component.swipemenu.SwipeMenuRecyclerView;
import com.tencent.qcloud.ugckit.component.swipemenu.touch.OnItemMoveListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 选择器下方"已被选中"列表
 */
public class PickedLayout extends RelativeLayout implements ItemView.OnDeleteListener, OnItemMoveListener, View.OnClickListener, IPickedLayout {
    private Activity              mActivity;
    private TextView              mTextDrag;
    private Button                mButtonNext;
    private SwipeMenuRecyclerView mSwipeMenuRecyclerView;

    private MenuAdapter                mMenuAdapter;
    private ArrayList<TCVideoFileInfo> mTCVideoFileInfoList;
    private OnNextStepListener         mOnNextStepListener;
    private String                     mDragText;
    private int                        mMinSelectedItemCount = 1;

    public PickedLayout(Context context) {
        super(context);
        initViews();
    }

    public PickedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public PickedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.ugckit_picture_pick_layout, this);

        mTCVideoFileInfoList = new ArrayList<>();

        mTextDrag = (TextView) findViewById(R.id.tv_drag);
        mButtonNext = (Button) findViewById(R.id.btn_next);
        mButtonNext.setEnabled(false);
        mButtonNext.setOnClickListener(this);
        mSwipeMenuRecyclerView = (SwipeMenuRecyclerView) findViewById(R.id.swipe_menu_recycler_view);
        mSwipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        mMenuAdapter = new MenuAdapter(mActivity, mTCVideoFileInfoList);
        mMenuAdapter.setOnItemDeleteListener(this);
        mSwipeMenuRecyclerView.setAdapter(mMenuAdapter);
        mSwipeMenuRecyclerView.setLongPressDragEnabled(true);
        mSwipeMenuRecyclerView.setOnItemMoveListener(this);

        if (!TextUtils.isEmpty(mDragText)) {
            mTextDrag.setText(mDragText);
        }
    }

    @Override
    public void addItem(TCVideoFileInfo fileInfo) {
        if (!mMenuAdapter.contains(fileInfo)) {
            mMenuAdapter.addItem(fileInfo);
        }
        if (mMenuAdapter.getItemCount() >= mMinSelectedItemCount) {
            mButtonNext.setEnabled(true);
        }
    }

    @Override
    public void onDelete(int position) {
        if (position < 0) {  // 异常情况处理，getAdapterPosition可能返回为 -1
            return;
        }
        mMenuAdapter.removeIndex(position);
        if (mMenuAdapter.getItemCount() < mMinSelectedItemCount) {
            mButtonNext.setEnabled(false);
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mTCVideoFileInfoList, fromPosition, toPosition);
        mMenuAdapter.notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();
        if (id == R.id.btn_next) {
            mOnNextStepListener.onNextStep();
        }
    }

    @Nullable
    @Override
    public ArrayList<TCVideoFileInfo> getSelectItems(int type) {
        int count = mMenuAdapter.getItemCount();
        ArrayList<TCVideoFileInfo> picturePathList = new ArrayList<TCVideoFileInfo>();
        for (int i = 0; i < count; i++) {
            TCVideoFileInfo fileInfo = mMenuAdapter.getItem(i);
            File file = new File(fileInfo.getFilePath());
            if (!file.exists()) {
                showErrorDialog(getResources().getString(R.string.ugckit_picture_choose_activity_the_selected_file_does_not_exist));
                return null;
            }
            picturePathList.add(fileInfo);
        }
        if (type == TYPE_PICTURE) {
            if (count < 2) {
                ToastUtil.toastShortMessage(getResources().getString(R.string.ugckit_picture_choose_activity_please_select_multiple_images));
                return picturePathList;
            }
        }
        return picturePathList;
    }

    private void showErrorDialog(String msg) {
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity, R.style.UGCKitConfirmDialogStyle);
        normalDialog.setMessage(msg);
        normalDialog.setCancelable(false);
        normalDialog.setPositiveButton(getResources().getString(R.string.ugckit_picture_choose_activity_got_it), null);
        normalDialog.show();
    }

    @Override
    public void setDragTipText(String dragTipText) {
        mTextDrag.setText(dragTipText);
    }

    @Override
    public void setNextTextSize(int textSize) {
        mTextDrag.setTextSize(textSize);
    }

    @Override
    public void setTextColor(int textColor) {
        mTextDrag.setTextColor(getResources().getColor(textColor));
    }

    @Override
    public void setButtonBackgroundResource(int resid) {
        mButtonNext.setBackgroundResource(resid);
    }

    @Override
    public void setRemoveIconResource(int resId) {
        mMenuAdapter.setRemoveIconId(resId);
    }

    @Override
    public void setBitmapWidth(int bitmapWidth) {
        mMenuAdapter.setBitmapWidth(bitmapWidth);
    }

    @Override
    public void setBitmapHeight(int bitmapHeight) {
        mMenuAdapter.setBitmapHeight(bitmapHeight);
    }

    @Override
    public void setMinSelectedItemCount(int minSelectedItemCount) {
        mMinSelectedItemCount = minSelectedItemCount;
    }


    @Override
    public void setOnNextStepListener(OnNextStepListener listener) {
        mOnNextStepListener = listener;
    }

    public interface OnNextStepListener {
        void onNextStep();
    }
}
