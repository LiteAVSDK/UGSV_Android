package com.tencent.qcloud.ugckit.component.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.R;

import java.util.ArrayList;
import java.util.List;

public class ActionSheetDialog {

    private Context         mContext;
    private Dialog          mDialog;
    private TextView        mTextTitle;
    private TextView        mTextCancel;
    private LinearLayout    mLayoutContent;
    private ScrollView      mScrollContent;
    private List<SheetItem> mSheetItemList;
    private Display         mDisplay;
    private boolean         mShowTitle = false;

    public ActionSheetDialog(@NonNull Context context) {
        this.mContext = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
    }

    @NonNull
    public ActionSheetDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.ugckit_view_actionsheet, null);

        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(mDisplay.getWidth());

        // 获取自定义Dialog布局中的控件
        mScrollContent = (ScrollView) view.findViewById(R.id.sLayout_content);
        mLayoutContent = (LinearLayout) view
                .findViewById(R.id.lLayout_content);
        mTextTitle = (TextView) view.findViewById(R.id.txt_title);
        mTextCancel = (TextView) view.findViewById(R.id.txt_cancel);
        mTextCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        // 定义Dialog布局和参数
        mDialog = new Dialog(mContext, R.style.UGCKitActionSheetDialogStyle);
        mDialog.setContentView(view);
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);

        return this;
    }

    @NonNull
    public ActionSheetDialog setTitle(String title) {
        mShowTitle = true;
        mTextTitle.setVisibility(View.VISIBLE);
        mTextTitle.setText(title);
        return this;
    }

    @NonNull
    public ActionSheetDialog setCancelable(boolean cancel) {
        mDialog.setCancelable(cancel);
        return this;
    }

    @NonNull
    public ActionSheetDialog setCanceledOnTouchOutside(boolean cancel) {
        mDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    /**
     * @param strItem  条目名称
     * @param color    条目字体颜色，设置null则默认蓝色
     * @param listener
     * @return
     */
    @NonNull
    public ActionSheetDialog addSheetItem(String strItem, SheetItemColor color,
                                          OnSheetItemClickListener listener) {
        if (mSheetItemList == null) {
            mSheetItemList = new ArrayList<SheetItem>();
        }
        mSheetItemList.add(new SheetItem(strItem, color, listener));
        return this;
    }

    /**
     * 设置条目布局
     */
    private void setSheetItems() {
        if (mSheetItemList == null || mSheetItemList.size() <= 0) {
            return;
        }

        int size = mSheetItemList.size();

        // TODO 高度控制，非最佳解决办法
        // 添加条目过多的时候控制高度
        if (size >= 7) {
            LayoutParams params = (LayoutParams) mScrollContent
                    .getLayoutParams();
            params.height = mDisplay.getHeight() / 2;
            mScrollContent.setLayoutParams(params);
        }

        // 循环添加条目
        for (int i = 1; i <= size; i++) {
            final int index = i;
            SheetItem sheetItem = mSheetItemList.get(i - 1);
            String strItem = sheetItem.name;
            SheetItemColor color = sheetItem.color;
            final OnSheetItemClickListener listener = (OnSheetItemClickListener) sheetItem.itemClickListener;

            TextView textView = new TextView(mContext);
            textView.setText(strItem);
            textView.setTextSize(18);
            textView.setGravity(Gravity.CENTER);

            // 背景图片
            if (size == 1) {
                if (mShowTitle) {
                    textView.setBackgroundResource(R.drawable.ugckit_actionsheet_bottom_selector);
                } else {
                    textView.setBackgroundResource(R.drawable.ugckit_actionsheet_single_selector);
                }
            } else {
                if (mShowTitle) {
                    if (i >= 1 && i < size) {
                        textView.setBackgroundResource(R.drawable.ugckit_actionsheet_middle_selector);
                    } else {
                        textView.setBackgroundResource(R.drawable.ugckit_actionsheet_bottom_selector);
                    }
                } else {
                    if (i == 1) {
                        textView.setBackgroundResource(R.drawable.ugckit_actionsheet_top_selector);
                    } else if (i < size) {
                        textView.setBackgroundResource(R.drawable.ugckit_actionsheet_middle_selector);
                    } else {
                        textView.setBackgroundResource(R.drawable.ugckit_actionsheet_bottom_selector);
                    }
                }
            }

            // 字体颜色
            if (color == null) {
                textView.setTextColor(Color.parseColor(SheetItemColor.Blue
                        .getName()));
            } else {
                textView.setTextColor(Color.parseColor(color.getName()));
            }

            // 高度
            float scale = mContext.getResources().getDisplayMetrics().density;
            int height = (int) (45 * scale + 0.5f);
            textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height));

            // 点击事件
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(index);
                    mDialog.dismiss();
                }
            });

            mLayoutContent.addView(textView);
        }
    }

    public void show() {
        setSheetItems();
        mDialog.show();
    }

    public interface OnSheetItemClickListener {
        void onClick(int which);
    }

    public class SheetItem {
        String                   name;
        OnSheetItemClickListener itemClickListener;
        SheetItemColor           color;

        public SheetItem(String name, SheetItemColor color,
                         OnSheetItemClickListener itemClickListener) {
            this.name = name;
            this.color = color;
            this.itemClickListener = itemClickListener;
        }
    }

    public enum SheetItemColor {
        Blue("#037BFF"), Red("#FD4A2E");

        private String name;

        SheetItemColor(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
