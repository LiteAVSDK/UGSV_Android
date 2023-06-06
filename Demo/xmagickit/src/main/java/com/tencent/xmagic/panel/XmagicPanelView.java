package com.tencent.xmagic.panel;

import static android.app.Activity.RESULT_OK;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.xmagic.demo.R;
import com.tencent.xmagic.download.MotionDLModel;
import com.tencent.xmagic.download.ResDownloadUtil;
import com.tencent.xmagic.module.XmagicUIProperty;
import com.tencent.xmagic.module.XmagicUIProperty.UICategory;
import com.tencent.xmagic.panel.adapter.XmagicPanelAdapter;
import com.tencent.xmagic.utils.OnDownloadListener;
import com.tencent.xmagic.utils.UriUtils;
import com.tencent.xmagic.widget.XmagicSeekBarLayout;
import com.tencent.xmagic.widget.diaolog.ProgressDialog;
import com.tencent.xmagic.widget.diaolog.TipDialog;
import com.tencent.xmagic.XmagicProperty;
import com.tencent.xmagic.XmagicProperty.XmagicPropertyValues;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.List;

/**
 * 美颜的UI面板
 */
public class XmagicPanelView extends RelativeLayout {
    private static final int RC_CHOOSE_PHOTO = 2002;

    private TextView errorTxt = null;    //用于展示错误提示的View
    private XmagicSeekBarLayout seekBarkLayout = null;


    private RelativeLayout firstMenuLayout = null;
    private RadioGroup radioGroup = null;       //分类菜单
    private RecyclerView firstRecyclerView = null;       //展示一级菜单内容
    private XmagicPanelAdapter firstAdapter = null;


    private RelativeLayout secondMenuLayout = null;   //二级菜单
    private ImageView backImg = null;
    private TextView titleTxt = null;   //展示二级菜单标题
    private RecyclerView secondRecycleView = null;    //展示二级菜单内容
    private XmagicPanelAdapter secondAdapter = null;
    private PanelViewCallBack panelViewCallBack = null;
    private XmagicUIProperty<?> segXmagicProperty = null;   //自定义分割的item

    private ViewGroup layout;
    private ImageView closeBtn;
    private LinearLayout expandLayout;
    private ImageView revertBtn;
    private ImageView beautyCompareBtn;   //美颜开关
    private float layoutHeight = 0;

    private static final int animateTime = 300;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ProgressDialog mProgressDialog;

    public void setOnUserUpdatePropertyListener(PanelViewCallBack panelViewCallBack) {
        this.panelViewCallBack = panelViewCallBack;
    }

    public XmagicPanelView(Context context) {
        this(context, null);
    }

    public XmagicPanelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XmagicPanelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initViews();
        this.initData();
        this.initUISettings();
    }

    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.xmagic_panel_layout, this, true);
        layout = findViewById(R.id.layout);
        closeBtn = findViewById(R.id.close_btn);
        expandLayout = findViewById(R.id.expand_btn_layout);
        revertBtn = findViewById(R.id.revert_btn);
        beautyCompareBtn = findViewById(R.id.beauty_compare_btn);

        errorTxt = layout.findViewById(R.id.tv_error_msg);
        seekBarkLayout = layout.findViewById(R.id.seekBarRootView);

        radioGroup = layout.findViewById(R.id.radioGroup);
        firstMenuLayout = layout.findViewById(R.id.rl_root_panel);
        firstRecyclerView = layout.findViewById(R.id.scrollViewContainer);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        firstRecyclerView.setLayoutManager(manager);
        firstAdapter = new XmagicPanelAdapter(new XmagicPanelAdapter.XmagicPanelItemClickListener() {
            @Override
            public void onItemClick(XmagicUIProperty<?> xmagicUIProperty) {
                onRecycleViewItemClick(xmagicUIProperty, PanelMenu.FIRST_MENU);
            }

            @Override
            public void onChecked(XmagicUIProperty<?> xmagicUIProperty) {
                onRecycleViewItemChecked(xmagicUIProperty, PanelMenu.FIRST_MENU);
            }

            @Override
            public void onBeautySwitchCheckedChange(boolean isChecked) {
                seekBarkLayout.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                if (isChecked) {
                    firstAdapter.getCheckedPosition();
                }
                if (panelViewCallBack != null) {
                    panelViewCallBack.onBeautyCloseOrOpen(isChecked);
                }
            }
        });
        firstRecyclerView.setAdapter(firstAdapter);

        secondMenuLayout = layout.findViewById(R.id.rl_item_panel);
        backImg = layout.findViewById(R.id.btn_back);
        titleTxt = layout.findViewById(R.id.tv_item_name);
        secondRecycleView = layout.findViewById(R.id.rv_item_listview);
        LinearLayoutManager manager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        secondRecycleView.setLayoutManager(manager1);
        secondAdapter = new XmagicPanelAdapter(new XmagicPanelAdapter.XmagicPanelItemClickListener() {
            @Override
            public void onItemClick(XmagicUIProperty<?> xmagicUIProperty) {
                onRecycleViewItemClick(xmagicUIProperty, PanelMenu.SECOND_MENU);
            }

            @Override
            public void onChecked(XmagicUIProperty<?> xmagicUIProperty) {
                onRecycleViewItemChecked(xmagicUIProperty, PanelMenu.SECOND_MENU);
            }

            @Override
            public void onBeautySwitchCheckedChange(boolean isChecked) {

            }
        });
        secondRecycleView.setAdapter(secondAdapter);
    }


    private void initUISettings() {
        layout.post((new Runnable() {
            @Override
            public void run() {
                layoutHeight = layout.getMeasuredHeight();
            }
        }));
        expandLayout.setVisibility(GONE);
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.animate().setListener(new ValueAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        layout.setVisibility(View.INVISIBLE);
                        expandLayout.setVisibility(View.VISIBLE);
                    }
                }).setDuration(animateTime).translationY(layoutHeight * 0.45f).scaleX(0.1f).scaleY(0.1f).start();
            }
        });
        expandLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expandLayout.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
                layout.animate().setListener(new ValueAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                }).setDuration(animateTime).translationY(0).scaleX(1f).scaleY(1f).start();
            }
        });
        revertBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = v.getResources().getString(R.string.revert_tip_title);
                String msg = v.getResources().getString(R.string.revert_tip_msg);
                String leftStr = v.getResources().getString(R.string.revert_tip_dialog_left_btn);
                String rightStr = v.getResources().getString(R.string.revert_tip_dialog_right_btn);
                TipDialog tipDialog = new TipDialog(v.getContext()).setData(title, msg, leftStr, rightStr);
                tipDialog.setClickListener(new TipDialog.TipDialogClickListener() {
                    @Override
                    public void onLeftBtnClick(Button btn) {

                    }

                    @Override
                    public void onRightBtnCLick(Button btn) {
                        if (panelViewCallBack != null) {
                            XmagicPanelDataManager.getInstance().setPanelBeautyOpen(true);
                            panelViewCallBack.onRevertBtnClick();
                        }
                    }
                }).show();
            }
        });
        beautyCompareBtn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isDown = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDown = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isDown = false;
                        break;
                    default:  // 其他情况，不回调接口
                        return true;
                }
                if (panelViewCallBack != null) {
                    panelViewCallBack.onBeautySwitchCheckedChanged(!isDown);
                }
                return true;
            }
        });
    }


    //初始化数据
    private void initData() {
        backImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                secondMenuLayout.setVisibility(GONE);
                firstMenuLayout.setVisibility(VISIBLE);
                seekBarkLayout.setVisibility(View.INVISIBLE);
            }
        });
        seekBarkLayout.setOnSeekBarChangeListener(new XmagicSeekBarLayout.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (seekBarkLayout.getTag() instanceof XmagicUIProperty) {
                        XmagicUIProperty<?> focusProperty = (XmagicUIProperty<?>) seekBarkLayout.getTag();
                        if (focusProperty != null && focusProperty.property.effValue != null) {
                            ((XmagicPropertyValues) focusProperty.property.effValue).setCurrentDisplayValue(progress);
                            //调节属性, 回调
                            callUpdateProperty(focusProperty);
                        }
                    }
                }
            }
        });
        initRootRadioGroup();

    }


    /**
     * 初始化美颜分类选项
     */
    private void initRootRadioGroup() {
        XmagicUIProperty.UICategory[] uiCategoryList = new XmagicUIProperty.UICategory[]{
                XmagicUIProperty.UICategory.BEAUTY,
                XmagicUIProperty.UICategory.BODY_BEAUTY,
                XmagicUIProperty.UICategory.LUT,
                XmagicUIProperty.UICategory.MOTION,
                XmagicUIProperty.UICategory.MAKEUP,
                XmagicUIProperty.UICategory.SEGMENTATION};
        int uiCategoryListSize = uiCategoryList.length;
        int btnWidths = 0;
        int firstRadioBtnId = 0;
        for (int i = 0; i < uiCategoryListSize; i++) {
            XmagicUIProperty.UICategory category = uiCategoryList[i];
            List<XmagicUIProperty<?>> datas = XmagicPanelDataManager.getInstance().getXmagicUIProperty(category);
            if (datas == null || datas.size() == 0) {
                continue;
            }
            if (category.isCanShowOnUI()) {
                RadioButton btn = new RadioButton(getContext());
                btn.setTag(category);
                int uiID = View.generateViewId();
                btn.setId(uiID);
                if (i == 0) {
                    firstRadioBtnId = uiID;
                    btn.setChecked(true);
                }
                btn.setButtonDrawable(null);
                btn.setTextSize(16);
                btn.setLines(1);
                btn.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.color_radiobutton));
                btn.setText(category.getDescription());
                btnWidths += (int) (btn.getPaint().measureText(category.getDescription())
                        + btn.getPaddingLeft() + btn.getPaddingRight());
                radioGroup.addView(btn);
            }
        }

        int count = radioGroup.getChildCount();
        int leftAndRightMargin =
                (getContext().getResources().getDisplayMetrics().widthPixels - btnWidths) / (count + 1);
        leftAndRightMargin = Math.max(leftAndRightMargin, dip2px(getContext(), 30));
        for (int index = 0; index < count; index++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(index);
            RadioGroup.LayoutParams layoutParams = (RadioGroup.LayoutParams) radioButton.getLayoutParams();
            layoutParams.leftMargin = leftAndRightMargin;
            if (index == count - 1) {
                layoutParams.rightMargin = leftAndRightMargin;
            }
            radioButton.setLayoutParams(layoutParams);
        }
        CheckedListener listener = new CheckedListener();
        radioGroup.setOnCheckedChangeListener(listener);
        listener.onCheckedChanged(radioGroup, firstRadioBtnId);
    }




    class CheckedListener implements RadioGroup.OnCheckedChangeListener {
        private boolean isFirstClick = true;

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radioButton = group.findViewById(checkedId);
            UICategory uiCategory = (UICategory) radioButton.getTag();
            if (uiCategory != null) {
                List<XmagicUIProperty<?>> datas = XmagicPanelDataManager.getInstance().getXmagicUIProperty(uiCategory);
                if (datas == null || datas.size() == 0) {
                    String noAuthTip = getResources().getString(R.string.check_noauth_tip);
                    Toast.makeText(getContext(), noAuthTip, Toast.LENGTH_LONG).show();
                    return;
                }
                firstAdapter.setProperties(datas);
                if (isFirstClick) {
                    if (datas != null && datas.size() > 1) {
                        onRecycleViewItemChecked(datas.get(1), PanelMenu.FIRST_MENU);
                    }
                } else {
                    //此种情况指的是美颜开关关闭，并且点击了美颜radiobutton的情况，这种情况应该隐藏seekBarkLayout
                    if (uiCategory == UICategory.BEAUTY && !XmagicPanelDataManager.getInstance().isPanelBeautyOpen()) {
                        seekBarkLayout.setVisibility(INVISIBLE);
                    } else {
                        firstRecyclerView.scrollToPosition(firstAdapter.getCheckedPosition());
                    }
                }
            }
            isFirstClick = false;
        }
    }


    /**
     * 当菜单的按钮被点击的时候
     *
     * @param xmagicUIProperty
     * @param menu
     */
    private void onRecycleViewItemClick(XmagicUIProperty xmagicUIProperty, PanelMenu menu) {
        if (menu == PanelMenu.FIRST_MENU) {
            if (xmagicUIProperty.xmagicUIPropertyList != null
                    && xmagicUIProperty.xmagicUIPropertyList.size() > 0) {  //表示有二级菜单
                setCurrentXmagicUIProperty(xmagicUIProperty, menu);
                firstAdapter.notifyDataSetChanged();
                firstAdapter.getCheckedPosition();
                firstMenuLayout.setVisibility(GONE);
                secondMenuLayout.setVisibility(VISIBLE);
                titleTxt.setText(xmagicUIProperty.displayName);
                secondAdapter.setProperties(xmagicUIProperty.xmagicUIPropertyList);
                secondRecycleView.scrollToPosition(secondAdapter.getCheckedPosition());

            } else { //表示没有二级菜单
                if (checkIsNeedDownload(xmagicUIProperty, menu)) {
                    return;
                }
                if (isCustomSegItem(xmagicUIProperty)) {
                    if (panelViewCallBack != null) {
                        segXmagicProperty = xmagicUIProperty;
                        panelViewCallBack.onClickCustomSegItem();
                    }
                } else {
                    if (!checkIsSupportAndAuth(xmagicUIProperty)) {
                        return;
                    }
                    setCurrentXmagicUIProperty(xmagicUIProperty, menu);
                    firstAdapter.notifyDataSetChanged();
                    firstAdapter.getCheckedPosition();
                }
            }
        } else {   //二级菜单
            if (checkIsNeedDownload(xmagicUIProperty, menu)) {
                return;
            }
            if (!checkIsSupportAndAuth(xmagicUIProperty)) {
                return;
            }
            setCurrentXmagicUIProperty(xmagicUIProperty, menu);
            secondAdapter.notifyDataSetChanged();
            secondAdapter.getCheckedPosition();
        }
    }

    /**
     * 设置一二级菜单的选中项
     *
     * @param uiProperty
     * @param menu             1表示一级菜单  2表示二级菜单
     */
    private void setCurrentXmagicUIProperty(XmagicUIProperty<?> uiProperty, PanelMenu menu) {
        if (uiProperty.uiCategory == UICategory.BEAUTY || uiProperty.uiCategory == UICategory.BODY_BEAUTY) {
            XmagicPanelDataManager.getInstance().getBeautyList().remove(uiProperty);
            XmagicPanelDataManager.getInstance().getBeautyList().add(uiProperty);
        }
        if (menu == PanelMenu.FIRST_MENU) {
            String des = uiProperty.uiCategory.getDescription();
            XmagicPanelDataManager.getInstance().getSelectedItems().put(des, uiProperty);
        } else {
            if (uiProperty.uiCategory == UICategory.BEAUTY) {
                XmagicPanelDataManager.getInstance().getSelectedItems().put(uiProperty.rootDisplayName, uiProperty);
            } else {
                XmagicPanelDataManager.getInstance().getSelectedItems().put("动效2", uiProperty);
            }
        }
    }

    private void onRecycleViewItemChecked(XmagicUIProperty<?> xmagicUIProperty, PanelMenu menu) {
        //1. 因为动效、美妆、分割不能同时设置上去，所以需要在用户点击了这些item之后，当用户停留在对应的item 列表的时候设置对应的item，
        // 也就是用户 选中了美妆、动效的时候，如果用户从从美妆列表切换到了动效列表，则直接重新设置已经选中的动效item
        if (xmagicUIProperty.uiCategory == UICategory.MOTION
                || xmagicUIProperty.uiCategory == UICategory.MAKEUP
                || xmagicUIProperty.uiCategory == UICategory.SEGMENTATION) {
            if (xmagicUIProperty.property != null) {
                XmagicPanelDataManager.getInstance().setLastItem(xmagicUIProperty);
            }
        }
        callUpdateProperty(xmagicUIProperty);
        setSeekBarState(xmagicUIProperty);
    }


    private boolean checkIsSupportAndAuth(XmagicUIProperty<?> xmagicUIProperty) {
        if (xmagicUIProperty != null && xmagicUIProperty.property != null) {
            if (!xmagicUIProperty.property.isSupport) {
                String tip = getContext().getResources().getString(R.string.not_support);
                Toast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
                return false;
            } else if (!xmagicUIProperty.property.isAuth) {
                String tip = getContext().getResources().getString(R.string.not_auth);
                Toast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                errorTxt.setVisibility(View.GONE);
                return true;
            }
        }
        return true;
    }

    /**
     * 检测是否需要下载处理
     *
     * @param uiProperty
     * @return
     */
    private boolean checkIsNeedDownload(XmagicUIProperty<?> uiProperty, PanelMenu menu) {
        if (uiProperty != null && uiProperty.xmagicUIPropertyList == null && uiProperty.dlModel != null) {
            startDownload(uiProperty, menu);
            return true;
        }
        return false;
    }

    /**
     * 开始下载资源
     *
     * @param uiProperty
     * @param menu
     */
    private void startDownload(final XmagicUIProperty<?> uiProperty, final PanelMenu menu) {
        showProgressDialog();
        MotionDLModel dlModel = uiProperty.dlModel;
        final String TAG = "startDownloadResource " + dlModel.getName();
        ResDownloadUtil.checkOrDownloadMotions(getContext(), dlModel, new OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String directory) {
                Log.e(TAG, "onDownloadSuccess  " + directory + "   " + Thread.currentThread().getName());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        uiProperty.dlModel = null;
                        dismissDialog();
                        onRecycleViewItemClick(uiProperty, menu);
                    }
                });
            }

            @Override
            public void onDownloading(final int progress) {
                Log.e(TAG, "onDownloading  " + progress + "   " + Thread.currentThread().getName());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog != null) {
                            mProgressDialog.setMsg(progress + "%");
                        }
                    }
                });
            }

            @Override
            public void onDownloadFailed(int errorCode) {
                Log.e(TAG, "onDownloadFailed  " + errorCode + "   " + Thread.currentThread().getName());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        Toast.makeText(getContext(), "资源下载失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog();
            mProgressDialog.createLoadingDialog(getContext());
            mProgressDialog.setCancelable(false);               // 设置是否可以通过点击Back键取消
            mProgressDialog.setCanceledOnTouchOutside(false);   // 设置在点击Dialog外是否取消Dialog进度条
            mProgressDialog.show();
        }
    }

    private void dismissDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    /**
     * 设置进度条
     */
    private void setSeekBarState(XmagicUIProperty<?> xmagicUIProperty) {
        if (xmagicUIProperty.property != null
                && xmagicUIProperty.property.effValue instanceof XmagicProperty.XmagicPropertyValues) {
            XmagicPropertyValues effValue = (XmagicPropertyValues) xmagicUIProperty.property.effValue;
            int currentVale = (int) effValue.getCurrentDisplayValue();
            seekBarkLayout.setProgress((int) effValue.displayMinValue, (int) effValue.displayMaxValue, currentVale);
            seekBarkLayout.setVisibility(View.VISIBLE);
            seekBarkLayout.setTag(xmagicUIProperty);
        } else {
            seekBarkLayout.setVisibility(View.INVISIBLE);
            seekBarkLayout.setTag(null);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CHOOSE_PHOTO && resultCode == RESULT_OK) {
            String filePath = null;
            if (data != null) {
                Uri uri = data.getData();
                filePath = UriUtils.getFilePathByUri(getContext(), uri);
                boolean checkResult = checkBitmap(filePath);
                if (checkResult) {
                    onActivityResult(filePath);
                } else {
                    Toast.makeText(getContext(), getResources().getString(com.tencent.xmagic.R.string.xamgic_5000),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        segXmagicProperty = null;
    }

    /**
     * 自定义分割
     *
     * @param path
     */
    private void onActivityResult(String path) {
        if (!TextUtils.isEmpty(path)) {
            segXmagicProperty.property.effKey = path;
            //此处不进行回调，因为一般情况下，此时xmagic处于ondestroy状态，此时设置会导致随后的resume之后的设置不生效，所以屏蔽掉这次设置
            //callUpdateProperty(segXmagicProperty);
            XmagicPanelDataManager.getInstance().getSelectedItems().
                    put(segXmagicProperty.uiCategory.getDescription(), segXmagicProperty);
            setSeekBarState(segXmagicProperty);
            XmagicPanelDataManager.getInstance().setLastItem(segXmagicProperty);
            if (firstAdapter != null) {
                firstAdapter.notifyDataSetChanged();
            }
        }
    }


    private boolean checkBitmap(String segmentBgFileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(segmentBgFileName);
        if (contentTypeFor.contains("image")) {
            int maxHeight = 3840;
            int maxWidth = 2160;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(segmentBgFileName, options);
            int width = options.outWidth;
            int height = options.outHeight;
            boolean isVertical = width < height;
            boolean isMin0 = width <= 0 || height <= 0;
            boolean isVerticalResult =
                    (isVertical && (width > maxWidth || height > maxHeight));
            boolean isHorResult =
                    (!isVertical && (width > maxHeight || height > maxWidth));
            if (isVerticalResult || isHorResult || isMin0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 用于展示错误信息的tv
     *
     * @param errorMsg
     */
    public void showErrorMsg(String errorMsg) {
        if (TextUtils.isEmpty(errorMsg)) {
            return;
        }
        errorTxt.setVisibility(View.VISIBLE);
        errorTxt.setText(errorMsg);
    }


    private void callUpdateProperty(XmagicUIProperty<?> xmagicUIProperty) {
        if (panelViewCallBack != null && xmagicUIProperty.property != null) {
            panelViewCallBack.onUserUpdateProperty(xmagicUIProperty.property);
        }
    }


    /**
     * 用于判断是否点击了 分割中的自定义item
     *
     * @param xmagicUIProperty
     * @return
     */
    private boolean isCustomSegItem(XmagicUIProperty<?> xmagicUIProperty) {
        return (xmagicUIProperty.uiCategory == UICategory.SEGMENTATION
                && TextUtils.equals(xmagicUIProperty.displayName,
                getContext().getString(R.string.segmentation_custom_label)));
    }

    /**
     * 用于一键还原美颜
     * 重置UI模块
     */
    @SuppressLint("NotifyDataSetChanged")
    public void revertMenuList() {
        if (secondMenuLayout.getVisibility() == VISIBLE) {
            backImg.callOnClick();
        }
        if (radioGroup.getChildCount() > 0) {
            View view = radioGroup.getChildAt(0);
            if (radioGroup.getCheckedRadioButtonId() == view.getId()) {
                if (firstAdapter != null) {
                    firstAdapter.getCheckedPosition();
                    firstAdapter.notifyDataSetChanged();
                }
            } else {
                ((RadioButton) view).setChecked(true);
            }
        }
    }


    public interface PanelViewCallBack {
        void onUserUpdateProperty(XmagicProperty<?> xmagicProperty);

        void onClickCustomSegItem();

        void onRevertBtnClick();

        void onBeautySwitchCheckedChanged(boolean isChecked);   //美颜的总开关回调函数

        void onBeautyCloseOrOpen(boolean isOpen);

    }

    /**
     * 打开系统相册
     */
    public static void openPhotoAlbum(Activity activity) {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*|video/*");
        activity.startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO); // 打开相册，选择图片
    }

    public static void openPhotoAlbum(Fragment fragment) {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*|video/*");
        fragment.startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO); // 打开相册，选择图片
    }


    public enum PanelMenu {
        FIRST_MENU,
        SECOND_MENU
    }


    static class ValueAnimationListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
