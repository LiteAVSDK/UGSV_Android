package com.tencent.qcloud.ugckit.module.picker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.TitleBarLayout;


public abstract class AbsPickerUI extends RelativeLayout implements IPickerLayout {

    private   TitleBarLayout   mTitleBar;
    protected PickerListLayout mSelectorListLayout;
    private   PickedLayout     mSelectedLayout;

    public AbsPickerUI(Context context) {
        super(context);
        initViews();
    }

    public AbsPickerUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public AbsPickerUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_picker_layout, this);

        mTitleBar = (TitleBarLayout) findViewById(R.id.titleBar_layout);
        mSelectorListLayout = (PickerListLayout) findViewById(R.id.choose_list_layout);
        mSelectedLayout = (PickedLayout) findViewById(R.id.choose_pick_layout);
    }

    @Override
    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    @Override
    public PickerListLayout getPickerListLayout() {
        return mSelectorListLayout;
    }

    @Override
    public PickedLayout getPickedLayout() {
        return mSelectedLayout;
    }

}
