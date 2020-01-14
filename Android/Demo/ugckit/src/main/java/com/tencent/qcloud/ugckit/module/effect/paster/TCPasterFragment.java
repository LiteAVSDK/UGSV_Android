package com.tencent.qcloud.ugckit.module.effect.paster;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tencent.liteav.basic.log.TXCLog;

import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.effect.BaseRecyclerAdapter;
import com.tencent.qcloud.ugckit.module.effect.TimeLineView;
import com.tencent.qcloud.ugckit.module.effect.TimelineViewUtil;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.FileUtils;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.floatlayer.FloatLayerView;
import com.tencent.qcloud.ugckit.component.floatlayer.FloatLayerViewGroup;
import com.tencent.qcloud.ugckit.component.timeline.RangeSliderViewContainer;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressController;
import com.tencent.qcloud.ugckit.component.timeline.ViewConst;
import com.tencent.qcloud.ugckit.module.effect.paster.view.PasterView;
import com.tencent.qcloud.ugckit.module.effect.paster.view.TCPasterOperationViewFactory;
import com.tencent.qcloud.ugckit.module.effect.paster.view.PasterPannel;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TCPasterFragment extends Fragment implements BaseRecyclerAdapter.OnItemClickListener,
        FloatLayerViewGroup.OnItemClickListener,
        FloatLayerView.IOperationViewClickListener,
        IPasterPannel.OnItemClickListener,
        IPasterPannel.OnTabChangedListener,
        IPasterPannel.OnAddClickListener,
        View.OnClickListener, PlayerManagerKit.OnPlayStateListener {
    private final String TAG = "TCPasterFragment";

    private final int MSG_COPY_PASTER_FILES = 1;

    private final String PASTER_FOLDER_NAME = "paster";
    private final String ANIMATED_PASTER_FOLDER_NAME = "AnimatedPaster";
    private final String PASTER_LIST_JSON_FILE_NAME = "pasterList.json";
    @Nullable
    private String mPasterSDcardFolder;
    @Nullable
    private String mAnimatedPasterSDcardFolder;

    private TXVideoEditer mTXVideoEditer;

    private RecyclerView mRvPaster;
    private ImageView mIvDel;
    private View mFootView;
    private AddPasterAdapter mAddPasterAdapter;
    private List<TCPasterInfo> mAddPasterInfoList;

    private PasterPannel mPasterPannel; // 选择贴纸控件
    private FloatLayerViewGroup mFloatLayerViewGroup; // 图层父布局，承载贴纸
    private int mCurrentSelectedPos = -1;// 当前被选中的贴纸控件

    @Nullable
    private RangeSliderViewContainer.OnDurationChangeListener mOnDurationChangeListener;

    // 子线程
    private HandlerThread mWorkHandlerThread;
    @Nullable
    private Handler mWorkHandler;

    private List<TCPasterInfo> mPasterInfoList;
    private List<TCPasterInfo> mAnimatedPasterInfoList;

    private boolean mIsUpdatePng = false;

    //================================== 时间 ==============================
    private long mDuration;
    private long mDefaultWordStartTime;
    private long mDefaultWordEndTime;
    private VideoProgressController mVideoProgressController;

    // 定制UI
    private int addIcon = R.drawable.ic_edit_add_selector;
    private int deleteIcon = R.drawable.ic_word_del_normal;
    private int mCoverIcon;
    private int pasterTextSize;
    private int pasterTextColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PlayerManagerKit.getInstance().addOnPlayStateLitener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_paster, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

        initData();

        initHandler();

        initRangeDurationChangeListener();

        mWorkHandler.sendEmptyMessage(MSG_COPY_PASTER_FILES);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            mFloatLayerViewGroup.setVisibility(View.GONE);
            mVideoProgressController.showAllRangeSliderView(ViewConst.VIEW_TYPE_PASTER, false);
        } else {
            mVideoProgressController.showAllRangeSliderView(ViewConst.VIEW_TYPE_PASTER, true);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TimeLineView timeLineView = TimelineViewUtil.getInstance().getTimeLineView();
        if (timeLineView != null) {
            mVideoProgressController = timeLineView.getVideoProgressController();
        }

        recoverFromManager();
    }

    private void initRangeDurationChangeListener() {
        mOnDurationChangeListener = new RangeSliderViewContainer.OnDurationChangeListener() {
            @Override
            public void onDurationChange(long startTime, long endTime) {
                // 获取当选中的贴纸，并且将时间设置进去
                PasterView view = (PasterView) mFloatLayerViewGroup.getSelectedLayerOperationView();
                if (view != null) {
                    view.setStartToEndTime(startTime, endTime);
                }
                // 时间范围修改也马上设置到sdk中去
                addPasterListVideo();
                saveIntoManager();
            }
        };
    }

    private void initView(@NonNull View view) {
        mFootView = LayoutInflater.from(view.getContext()).inflate(R.layout.item_add, null);
        CircleImageView addView = (CircleImageView) mFootView.findViewById(R.id.add_paster_image);
        addView.setImageResource(addIcon);

        mAddPasterInfoList = new ArrayList<>();
        mRvPaster = (RecyclerView) view.findViewById(R.id.paster_rv_list);
        mRvPaster.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAddPasterAdapter = new AddPasterAdapter(mAddPasterInfoList, getActivity());
        mAddPasterAdapter.setCoverIconResouce(mCoverIcon);
        mAddPasterAdapter.setPasterTextSize(pasterTextSize);
        mAddPasterAdapter.setPasterTextColor(pasterTextColor);
        mAddPasterAdapter.setOnItemClickListener(this);
        mRvPaster.setAdapter(mAddPasterAdapter);
        mAddPasterAdapter.setFooterView(mFootView);

        mPasterPannel = (PasterPannel) getActivity().findViewById(R.id.paster_select_view);
        mPasterPannel.setOnTabChangedListener(this);
        mPasterPannel.setOnItemClickListener(this);
        mPasterPannel.setOnAddClickListener(this);
        mPasterPannel.setVisibility(View.GONE);

        mFloatLayerViewGroup = (FloatLayerViewGroup) getActivity().findViewById(R.id.paster_container);
        mFloatLayerViewGroup.setOnItemClickListener(this);
        mFloatLayerViewGroup.enableChildSingleClick(false); // 在容器里不响应子控件的单击事件
        mFloatLayerViewGroup.enableDoubleChildClick(false); // 在容器里不响应子控件的双击事件

        mIvDel = (ImageView) view.findViewById(R.id.iv_del);
        mIvDel.setOnClickListener(this);
    }

    private void initData() {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            File sdcardDir = fragmentActivity.getExternalFilesDir(null);
            if (sdcardDir != null) {
                mPasterSDcardFolder = sdcardDir + File.separator + PASTER_FOLDER_NAME + File.separator;
                mAnimatedPasterSDcardFolder = sdcardDir + File.separator + ANIMATED_PASTER_FOLDER_NAME + File.separator;
            }
        }

        mTXVideoEditer = VideoEditerSDK.getInstance().getEditer();

        long startTime = VideoEditerSDK.getInstance().getCutterStartTime();
        long endTime = VideoEditerSDK.getInstance().getCutterEndTime();

        mDuration = endTime - startTime;
        updateDefaultTime();
    }

    /**
     * 根据当前控件数量 更新默认的一个控件开始时间和结束时间
     */
    private void updateDefaultTime() {
        int count = mFloatLayerViewGroup != null ? mFloatLayerViewGroup.getChildCount() : 0;
        mDefaultWordStartTime = count * 1000; // 两个之间间隔1秒
        mDefaultWordEndTime = mDefaultWordStartTime + 2000;

        if (mDefaultWordStartTime > mDuration) {
            mDefaultWordStartTime = mDuration - 2000;
            mDefaultWordEndTime = mDuration;
        } else if (mDefaultWordEndTime > mDuration) {
            mDefaultWordEndTime = mDuration;
        }
    }

    private void initHandler() {
        mWorkHandlerThread = new HandlerThread("TCPasterFragment_handlerThread");
        mWorkHandlerThread.start();
        mWorkHandler = new Handler(mWorkHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_COPY_PASTER_FILES:
                        if (mIsUpdatePng) {
                            if (!TextUtils.isEmpty(mPasterSDcardFolder)) {
                                FileUtils.deleteFile(mPasterSDcardFolder);
                            }
                            if (!TextUtils.isEmpty(mAnimatedPasterSDcardFolder)) {
                                FileUtils.deleteFile(mAnimatedPasterSDcardFolder);
                            }
                        }
                        if (!TextUtils.isEmpty(mPasterSDcardFolder)) {
                            File pasterFolder = new File(mPasterSDcardFolder);
                            File animatedPasterFolder = new File(mAnimatedPasterSDcardFolder);
                            if (!pasterFolder.exists() || !animatedPasterFolder.exists()) {
                                copyPasterFilesToSdcard();
                            }
                            preparePasterInfoToShow();
                        }
                        break;
                }
            }
        };
    }

    // mAddPasterAdapter底部的已添加的贴纸列表选中
    @Override
    public void onItemClick(View view, int position) {
        if (position == mAddPasterInfoList.size()) {
            // 新增
            clickBtnAdd();
        } else {
            if (!mFloatLayerViewGroup.isShown()) {
                mFloatLayerViewGroup.setVisibility(View.VISIBLE);
                // 暂停播放
                mTXVideoEditer.refreshOneFrame(); // 将视频画面中的字幕清除  ，避免与上层控件造成混淆导致体验不好的问题。

                PlayerManagerKit.getInstance().pausePlay();
            }
            // 列表选中
            mAddPasterAdapter.setCurrentSelectedPos(position);
            // 预览界面选中
            mFloatLayerViewGroup.selectOperationView(position);
            // 进度条范围选中
            RangeSliderViewContainer lastSlider = mVideoProgressController.getRangeSliderView(ViewConst.VIEW_TYPE_PASTER, mCurrentSelectedPos);
            if (lastSlider != null) {
                lastSlider.setEditComplete();
            }
            RangeSliderViewContainer currentSlider = mVideoProgressController.getRangeSliderView(ViewConst.VIEW_TYPE_PASTER, position);
            if (currentSlider != null) {
                currentSlider.showEdit();
            }
            mCurrentSelectedPos = position;
        }
    }

    private void clickBtnAdd() {
        mPasterPannel.show();
        mFloatLayerViewGroup.setVisibility(View.VISIBLE);
        // 暂停播放
        mTXVideoEditer.refreshOneFrame(); // 将视频画面中的字幕清除  ，避免与上层控件造成混淆导致体验不好的问题。

        PlayerManagerKit.getInstance().pausePlay();
    }

    private void copyPasterFilesToSdcard() {
        if (!TextUtils.isEmpty(mPasterSDcardFolder)) {
            File pasterFolder = new File(mPasterSDcardFolder);
            if (!pasterFolder.exists()) {
                FileUtils.copyFilesFromAssets(getActivity(), PASTER_FOLDER_NAME, mPasterSDcardFolder);
            }
        }
        if (!TextUtils.isEmpty(mAnimatedPasterSDcardFolder)) {
            File animatedFolder = new File(mAnimatedPasterSDcardFolder);
            if (!animatedFolder.exists()) {
                FileUtils.copyFilesFromAssets(getActivity(), ANIMATED_PASTER_FOLDER_NAME, mAnimatedPasterSDcardFolder);
            }
        }
    }

    private void preparePasterInfoToShow() {
        mPasterInfoList = getPasterInfoList(PasterView.TYPE_CHILD_VIEW_PASTER, mPasterSDcardFolder, PASTER_LIST_JSON_FILE_NAME);
        mAnimatedPasterInfoList = getPasterInfoList(PasterView.TYPE_CHILD_VIEW_ANIMATED_PASTER, mAnimatedPasterSDcardFolder, PASTER_LIST_JSON_FILE_NAME);

        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentTab = mPasterPannel.getCurrentTab();
                changeListViewData(currentTab);
            }
        });
    }

    private void changeListViewData(int currentTab) {
        if (currentTab == IPasterPannel.TAB_PASTER) {
            mPasterPannel.setPasterInfoList(mPasterInfoList);
        } else if (currentTab == IPasterPannel.TAB_ANIMATED_PASTER) {
            mPasterPannel.setPasterInfoList(mAnimatedPasterInfoList);
        }
    }

    @NonNull
    private List<TCPasterInfo> getPasterInfoList(int pasterType, String fileFolder, String fileName) {
        List<TCPasterInfo> pasterInfoList = new ArrayList<TCPasterInfo>();

        if (TextUtils.isEmpty(fileFolder)) {
            return pasterInfoList;
        }
        String filePath = fileFolder + fileName;
        try {
            String jsonString = FileUtils.getJsonFromFile(filePath);
            if (TextUtils.isEmpty(jsonString)) {
                TXCLog.e(TAG, "getPasterInfoList, jsonString is empty");
                return pasterInfoList;
            }
            JSONObject pasterJson = new JSONObject(jsonString);
            JSONArray pasterInfoJsonArray = pasterJson.getJSONArray("pasterList");
            for (int i = 0; i < pasterInfoJsonArray.length(); i++) {
                JSONObject pasterInfoJsonObject = pasterInfoJsonArray.getJSONObject(i);
                TCPasterInfo tcPasterInfo = new TCPasterInfo();

                tcPasterInfo.setName(pasterInfoJsonObject.getString("name"));
                tcPasterInfo.setIconPath(fileFolder + pasterInfoJsonObject.getString("icon"));
                tcPasterInfo.setPasterType(pasterType);

                pasterInfoList.add(tcPasterInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pasterInfoList;
    }

    @Override
    public void onLayerOperationViewItemClick(FloatLayerView view, int lastSelectedPos, int currentSelectedPos) {
        pausePlay(true);

        RangeSliderViewContainer lastSlider = mVideoProgressController.getRangeSliderView(lastSelectedPos);
        if (lastSlider != null) {
            lastSlider.setEditComplete();
        }

        RangeSliderViewContainer currentSlider = mVideoProgressController.getRangeSliderView(currentSelectedPos);
        if (currentSlider != null) {
            currentSlider.showEdit();
        }

        mCurrentSelectedPos = currentSelectedPos;
    }

    private void pausePlay(boolean isShow) {
        PlayerManagerKit.getInstance().pausePlay();

        if (isShow) {
            // 将字幕控件显示出来
            mFloatLayerViewGroup.setVisibility(View.VISIBLE);
            mTXVideoEditer.refreshOneFrame();// 将视频画面中的字幕清除  ，避免与上层控件造成混淆导致体验不好的问题。
        }
        int selectedIndex = mFloatLayerViewGroup.getSelectedViewIndex();
        if (selectedIndex != -1) {// 说明有控件被选中 那么显示出时间区间的选择
            RangeSliderViewContainer view = mVideoProgressController.getRangeSliderView(selectedIndex);
            if (isShow) {
                view.showEdit();
            } else {
                view.setEditComplete();
            }
        }
    }

    // 选择贴纸
    @Override
    public void onItemClick(@NonNull TCPasterInfo tcPasterInfo, int position) {
        int index = mFloatLayerViewGroup.getSelectedViewIndex();
        TXCLog.i(TAG, "onItemClick: index = " + index);
        RangeSliderViewContainer lastSlider = mVideoProgressController.getRangeSliderView(index);
        if (lastSlider != null) {
            lastSlider.setEditComplete();
        } else {
            Log.e(TAG, "onItemClick: slider view is null");
        }

        String pasterPath = null;
        Bitmap bitmap = null;
        int pasterType = tcPasterInfo.getPasterType();
        if (pasterType == PasterView.TYPE_CHILD_VIEW_ANIMATED_PASTER) {
            AnimatedPasterConfig animatedPasterConfig = getAnimatedPasterParamFromPath(mAnimatedPasterSDcardFolder + tcPasterInfo.getName() + File.separator);
            if (animatedPasterConfig == null) {
                TXCLog.e(TAG, "onItemClick, animatedPasterConfig is null");
                return;
            }
            int keyFrameIndex = animatedPasterConfig.keyframe;
            String keyFrameName = animatedPasterConfig.frameArray.get(keyFrameIndex - 1).pictureName;

            if (!TextUtils.isEmpty(mAnimatedPasterSDcardFolder)) {
                pasterPath = mAnimatedPasterSDcardFolder + tcPasterInfo.getName() + File.separator + keyFrameName + ".png";
                bitmap = BitmapFactory.decodeFile(pasterPath);
            }
        } else if (pasterType == PasterView.TYPE_CHILD_VIEW_PASTER) {
            if (!TextUtils.isEmpty(mPasterSDcardFolder)) {
                pasterPath = mPasterSDcardFolder + tcPasterInfo.getName() + File.separator + tcPasterInfo.getName() + ".png";
                bitmap = BitmapFactory.decodeFile(pasterPath);
            }
        }
        // 更新一下默认配置的时间
        updateDefaultTime();

        PasterView pasterOperationView = TCPasterOperationViewFactory.newOperationView(getActivity());
        pasterOperationView.setPasterPath(pasterPath);
        pasterOperationView.setChildType(tcPasterInfo.getPasterType());
        pasterOperationView.setIconPath(tcPasterInfo.getIconPath());
        pasterOperationView.setCenterX(mFloatLayerViewGroup.getWidth() / 2);
        pasterOperationView.setCenterY(mFloatLayerViewGroup.getHeight() / 2);
        pasterOperationView.setStartToEndTime(mDefaultWordStartTime, mDefaultWordEndTime);
        pasterOperationView.setIOperationViewClickListener(this);
        pasterOperationView.setPasterName(tcPasterInfo.getName());
        pasterOperationView.showDelete(false);
        pasterOperationView.showEdit(false);

        RangeSliderViewContainer rangeSliderView = new RangeSliderViewContainer(getActivity());
        rangeSliderView.init(mVideoProgressController, mDefaultWordStartTime, mDefaultWordEndTime - mDefaultWordStartTime, mDuration);
        rangeSliderView.setDurationChangeListener(mOnDurationChangeListener);
        mVideoProgressController.addRangeSliderView(ViewConst.VIEW_TYPE_PASTER, rangeSliderView);
        mVideoProgressController.setCurrentTimeMs(mDefaultWordStartTime);

        mFloatLayerViewGroup.addOperationView(pasterOperationView);
        if (bitmap != null) {
            pasterOperationView.setImageBitamp(bitmap);
        }
        mPasterPannel.dismiss();

        // 更新下方的贴纸列表
        mAddPasterInfoList.add(tcPasterInfo);
        mAddPasterAdapter.notifyDataSetChanged();
        mAddPasterAdapter.setCurrentSelectedPos(mAddPasterInfoList.size() - 1);

        mCurrentSelectedPos = mAddPasterInfoList.size() - 1;

        addPasterListVideo();
        saveIntoManager();
    }

    // 动态、静态切换
    @Override
    public void onTabChanged(int currentTab) {
        changeListViewData(currentTab);
    }

    /****** 可编辑控件的回调start ******/
    @Override
    public void onDeleteClick() {
//        int index = mTCLayerViewGroup.getSelectedViewIndex();
//        PasterOperationView view = (PasterOperationView) mTCLayerViewGroup.getSelectedLayerOperationView();
//        if (view != null) {
//            mTCLayerViewGroup.removeOperationView(view);
//        }
//        ((TCVideoEffectActivity) getActivity()).mVideoProgressController.removeRangeSliderView(index);
//
//        mAddPasterInfoList.remove(index);
//        mAddPasterAdapter.notifyDataSetChanged();
//        mAddPasterAdapter.setCurrentSelectedPos(-1);
    }

    @Override
    public void onEditClick() {

    }

    // 拖动、旋转的回调
    @Override
    public void onRotateClick() {
        addPasterListVideo();
        saveIntoManager();
    }

    /****** 可编辑控件的回调end ******/

    @Override
    public void onClick(@NonNull View v) {
        int i = v.getId();
        if (i == R.id.iv_del) {
            deletePaster();
        }
    }

    private void deletePaster() {
        int index = mFloatLayerViewGroup.getSelectedViewIndex();
        if (index < 0) {
            return;
        }
        PasterView view = (PasterView) mFloatLayerViewGroup.getSelectedLayerOperationView();
        if (view != null) {
            mFloatLayerViewGroup.removeOperationView(view);
        }
        mVideoProgressController.removeRangeSliderView(ViewConst.VIEW_TYPE_PASTER, index);

        if (mAddPasterInfoList.size() > 0) {
            mAddPasterInfoList.remove(index);
        }

        mAddPasterAdapter.notifyDataSetChanged();

        mCurrentSelectedPos = -1;
        mAddPasterAdapter.setCurrentSelectedPos(mCurrentSelectedPos);

        addPasterListVideo();
        saveIntoManager();
    }

    /**
     * 从指定路径加载贴纸配置
     *
     * @param pathFolder
     * @return
     */
    @Nullable
    private AnimatedPasterConfig getAnimatedPasterParamFromPath(String pathFolder) {
        AnimatedPasterConfig animatedPasterConfig = null;
        String configPath = pathFolder + AnimatedPasterConfig.FILE_NAME;

        String configJsonStr = FileUtils.getJsonFromFile(configPath);

        if (TextUtils.isEmpty(configJsonStr)) {
            TXCLog.e(TAG, "getTXAnimatedPasterParamFromPath, configJsonStr is empty");
            return animatedPasterConfig;
        }

        JSONObject jsonObjectConfig = null;
        try {
            jsonObjectConfig = new JSONObject(configJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObjectConfig == null) {
            TXCLog.e(TAG, "getTXAnimatedPasterParamFromPath, jsonObjectConfig is null");
            return animatedPasterConfig;
        }

        animatedPasterConfig = new AnimatedPasterConfig();
        try {
            animatedPasterConfig.name = jsonObjectConfig.getString(AnimatedPasterConfig.CONFIG_NAME);
            animatedPasterConfig.count = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_COUNT);
            animatedPasterConfig.period = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_PERIOD);
            animatedPasterConfig.width = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_WIDTH);
            animatedPasterConfig.height = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_HEIGHT);
            animatedPasterConfig.keyframe = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_KEYFRAME);
            JSONArray frameJsonArray = jsonObjectConfig.getJSONArray(AnimatedPasterConfig.CONFIG_KEYFRAME_ARRAY);
            for (int i = 0; i < animatedPasterConfig.count; i++) {
                JSONObject frameNameObject = frameJsonArray.getJSONObject(i);
                AnimatedPasterConfig.PasterPicture pasterPicture = new AnimatedPasterConfig.PasterPicture();
                pasterPicture.pictureName = frameNameObject.getString(AnimatedPasterConfig.PasterPicture.PICTURE_NAME);

                animatedPasterConfig.frameArray.add(pasterPicture);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return animatedPasterConfig;
    }

    /**
     * ===========================将贴纸添加到SDK中去=================================
     */
    private void addPasterListVideo() {
        List<TXVideoEditConstants.TXAnimatedPaster> animatedPasterList = new ArrayList<>();
        List<TXVideoEditConstants.TXPaster> pasterList = new ArrayList<>();
        for (int i = 0; i < mFloatLayerViewGroup.getChildCount(); i++) {
            PasterView view = (PasterView) mFloatLayerViewGroup.getOperationView(i);
            TXVideoEditConstants.TXRect rect = new TXVideoEditConstants.TXRect();
            rect.x = view.getImageX();
            rect.y = view.getImageY();
            rect.width = view.getImageWidth();
            TXCLog.i(TAG, "addPasterListVideoToEditer, adjustPasterRect, paster x y = " + rect.x + "," + rect.y);

            int childType = view.getChildType();
            if (childType == PasterView.TYPE_CHILD_VIEW_ANIMATED_PASTER) {
                TXVideoEditConstants.TXAnimatedPaster txAnimatedPaster = new TXVideoEditConstants.TXAnimatedPaster();

                txAnimatedPaster.animatedPasterPathFolder = mAnimatedPasterSDcardFolder + view.getPasterName() + File.separator;
                txAnimatedPaster.startTime = view.getStartTime();
                txAnimatedPaster.endTime = view.getEndTime();
                txAnimatedPaster.frame = rect;
                txAnimatedPaster.rotation = view.getImageRotate();

                animatedPasterList.add(txAnimatedPaster);
                TXCLog.i(TAG, "addPasterListVideoToEditer, txAnimatedPaster startTimeMs, endTime is : " + txAnimatedPaster.startTime + ", " + txAnimatedPaster.endTime);
            } else if (childType == PasterView.TYPE_CHILD_VIEW_PASTER) {
                TXVideoEditConstants.TXPaster txPaster = new TXVideoEditConstants.TXPaster();

                txPaster.pasterImage = view.getRotateBitmap();
                txPaster.startTime = view.getStartTime();
                txPaster.endTime = view.getEndTime();
                txPaster.frame = rect;

                pasterList.add(txPaster);
                TXCLog.i(TAG, "addPasterListVideoToEditer, txPaster startTimeMs, endTime is : " + txPaster.startTime + ", " + txPaster.endTime);
            }
        }
        mTXVideoEditer.setAnimatedPasterList(animatedPasterList);
        mTXVideoEditer.setPasterList(pasterList);
    }

    /**
     * ===========================将贴纸控件参数保存到Manager中去=================================
     * <p>
     * 将贴纸控件的相关参数保存到Manager中去，方便出去之后可以重新进来再次编辑贴纸
     */
    private void saveIntoManager() {
        TXCLog.i(TAG, "saveIntoManager");
        TCPasterViewInfoManager manager = TCPasterViewInfoManager.getInstance();
        manager.clear();
        for (int i = 0; i < mFloatLayerViewGroup.getChildCount(); i++) {
            PasterView view = (PasterView) mFloatLayerViewGroup.getOperationView(i);

            TXCLog.i(TAG, "saveIntoManager, view centerX and centerY = " + view.getCenterX() + ", " + view.getCenterY() +
                    ", start end time = " + view.getStartTime() + ", " + view.getEndTime());

            TCPasterViewInfo info = new TCPasterViewInfo();
            info.setViewCenterX(view.getCenterX());
            info.setViewCenterY(view.getCenterY());
            info.setRotation(view.getImageRotate());
            info.setImageScale(view.getImageScale());
            info.setPasterPath(view.getPasterPath());
            info.setIconPath(view.getIconPath());
            info.setStartTime(view.getStartTime());
            info.setEndTime(view.getEndTime());
            info.setName(view.getPasterName());
            info.setViewType(view.getChildType());

            manager.add(info);
        }
    }

    /**
     * 将贴纸控件的相关参数从Manager中重新恢复出来，恢复贴纸编辑的场景。 以便继续编辑
     */
    private void recoverFromManager() {
        TCPasterViewInfoManager manager = TCPasterViewInfoManager.getInstance();
        TXCLog.i(TAG, "recoverFromManager, manager.size = " + manager.getSize());
        for (int i = 0; i < manager.getSize(); i++) {
            TCPasterViewInfo info = manager.get(i);
            Bitmap pasterBitmap = BitmapFactory.decodeFile(info.getPasterPath());
            TXCLog.i(TAG, "recoverFromManager, info.getPasterPath() = " + info.getPasterPath());
            if (pasterBitmap == null) {
                TXCLog.e(TAG, "recoverFromManager, pasterBitmap is null!");
                continue;
            }
            PasterView view = TCPasterOperationViewFactory.newOperationView(getActivity());
            view.setImageBitamp(pasterBitmap);
            view.setChildType(info.getViewType());
            view.setCenterX(info.getViewCenterX());
            view.setCenterY(info.getViewCenterY());
            view.setImageRotate(info.getRotation());
            view.setImageScale(info.getImageScale());
            view.setPasterPath(info.getPasterPath());
            view.setIconPath(info.getIconPath());
            view.setPasterName(info.getName());
            view.showDelete(false);
            view.showEdit(false);
            view.setIOperationViewClickListener(this);

            // 恢复时间的时候，需要检查一下是否符合这一次区间的startTime和endTime
            long viewStartTime = info.getStartTime();
            long viewEndTime = info.getEndTime();
            view.setStartToEndTime(viewStartTime, viewEndTime);

            RangeSliderViewContainer rangeSliderView = new RangeSliderViewContainer(getActivity());
            rangeSliderView.init(mVideoProgressController, viewStartTime, viewEndTime - viewStartTime, mDuration);
            rangeSliderView.setDurationChangeListener(mOnDurationChangeListener);
            rangeSliderView.setEditComplete();

            mVideoProgressController.addRangeSliderView(ViewConst.VIEW_TYPE_PASTER, rangeSliderView);
            mFloatLayerViewGroup.addOperationView(view);// 添加到Group中去管理

            TCPasterInfo tcPasterInfo = new TCPasterInfo();
            tcPasterInfo.setName(info.getName());
            tcPasterInfo.setIconPath(info.getIconPath());
            tcPasterInfo.setPasterType(info.getViewType());
            mAddPasterInfoList.add(tcPasterInfo);
        }
        mCurrentSelectedPos = manager.getSize() - 1;

        mAddPasterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAddPaster() {
        addPasterListVideo();
    }

    @Override
    public void onPlayStateStart() {
        if (mFloatLayerViewGroup != null) {
            mFloatLayerViewGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayStateResume() {
        if (mFloatLayerViewGroup != null) {
            mFloatLayerViewGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayStatePause() {
        // 将视频画面中的字幕清除  ，避免与上层控件造成混淆导致体验不好的问题。
        mTXVideoEditer.refreshOneFrame();
        if (mFloatLayerViewGroup != null) {
            mFloatLayerViewGroup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPlayStateStop() {

    }

}
