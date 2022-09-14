package com.tencent.qcloud.ugckit.component.floatlayer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于统一管理{@link FloatLayerView}的layout
 */
public class FloatLayerViewGroup extends FrameLayout implements View.OnClickListener {
    private final String TAG = "FloatLayerViewGroup";

    private List<FloatLayerView> mFloatLayerViewList;
    private int                  mLastSelectedPos        = -1;
    private boolean              mEnableChildSingleClick = true;
    private boolean              mEnableChildDoubleClick = false;
    long mLastTime = 0;
    long mCurTime  = 0;

    public FloatLayerViewGroup(@NonNull Context context) {
        super(context);
        init();
    }

    public FloatLayerViewGroup(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatLayerViewGroup(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mFloatLayerViewList = new ArrayList<FloatLayerView>();
    }

    public void addOperationView(@NonNull FloatLayerView view) {
        mFloatLayerViewList.add(view);
        selectOperationView(mFloatLayerViewList.size() - 1);
        addView(view);
        view.setOnClickListener(this);
    }

    public void removeOperationView(@NonNull FloatLayerView view) {
        int viewIndex = mFloatLayerViewList.indexOf(view);
        mFloatLayerViewList.remove(view);
        mLastSelectedPos = -1;
        removeView(view);
        view.setOnClickListener(null);
    }

    public FloatLayerView getOperationView(int index) {
        return mFloatLayerViewList.get(index);
    }

    public void selectOperationView(int pos) {
        if (pos < mFloatLayerViewList.size() && pos >= 0) {
            if (mLastSelectedPos != -1)
                mFloatLayerViewList.get(mLastSelectedPos).setEditable(false);//不显示编辑的边框
            mFloatLayerViewList.get(pos).setEditable(true);//显示编辑的边框
            mLastSelectedPos = pos;
        }
    }

    private void unSelectOperationView(int pos) {
        if (pos < mFloatLayerViewList.size() && mLastSelectedPos != -1) {
            mFloatLayerViewList.get(mLastSelectedPos).setEditable(false);//不显示编辑的边框
            mLastSelectedPos = -1;
        }
    }

    @Nullable
    public FloatLayerView getSelectedLayerOperationView() {
        if (mLastSelectedPos < 0 || mLastSelectedPos >= mFloatLayerViewList.size()) return null;
        return mFloatLayerViewList.get(mLastSelectedPos);
    }

    public int getSelectedViewIndex() {
        return mLastSelectedPos;
    }

    public int getChildCount() {
        return mFloatLayerViewList.size();
    }

    public void enableChildSingleClick(boolean enable) {
        mEnableChildSingleClick = enable;
    }

    public void enableDoubleChildClick(boolean enable) {
        mEnableChildDoubleClick = enable;
    }

    @Override
    public void onClick(View v) {
        mLastTime = mCurTime;
        mCurTime = System.currentTimeMillis();
        if (mCurTime - mLastTime < 300) {//双击事件
            mCurTime = 0;
            mLastTime = 0;
            if (mEnableChildDoubleClick) {
                onItemClick(v);
            }
        } else {//单击事件
            if (mEnableChildSingleClick) {
                onItemClick(v);
            }
        }
    }

    private void onItemClick(View v) {
        FloatLayerView floatLayerView = (FloatLayerView) v;
        int pos = mFloatLayerViewList.indexOf(floatLayerView);
        int lastPos = mLastSelectedPos;
        selectOperationView(pos); //选中编辑
        if (mListener != null) {
            mListener.onLayerOperationViewItemClick(floatLayerView, lastPos, pos);
        }
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onLayerOperationViewItemClick(FloatLayerView view, int lastSelectedPos, int currentSelectedPos);
    }

}
