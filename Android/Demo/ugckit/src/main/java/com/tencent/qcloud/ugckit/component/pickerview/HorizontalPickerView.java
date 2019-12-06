package com.tencent.qcloud.ugckit.component.pickerview;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;


public class HorizontalPickerView extends android.widget.HorizontalScrollView {

    public HorizontalPickerView(Context context) {
        super(context);
        initialize();
    }

    public HorizontalPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public HorizontalPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private DataSetObserver observer;
    public void setAdapter(@NonNull Adapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(observer);
        }
        this.adapter = adapter;
        adapter.registerDataSetObserver(observer);
        updateAdapter();
    }

    private void updateAdapter() {
        ViewGroup group = (ViewGroup)getChildAt(0);
        group.removeAllViews();

        for (int i = 0; i<adapter.getCount(); i++) {
            View view = adapter.getView(i,null,group);
            group.addView(view);
        }
    }

    private Adapter adapter;
    void initialize() {
        observer = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateAdapter();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                ((ViewGroup)getChildAt(0)).removeAllViews();
            }
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int side = getWidth() / 2;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            getChildAt(0).setPaddingRelative(side, 0, side, 0);
//        } else {
//            getChildAt(0).setPadding(side,0,side,0);
//        }

    }

    public void setClicked(int position) {
        ((ViewGroup)getChildAt(0)).getChildAt(position).performClick();
    }
}