package com.tencent.qcloud.ugckit.module.effect.transition;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.liteav.basic.enums.TXEVideoTransitionDef;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.timeline.ColorfulProgress;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressController;
import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.effect.TimeLineView;
import com.tencent.qcloud.ugckit.module.effect.TimelineViewUtil;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.util.ArrayList;
import java.util.List;

public class TCTransitionFragment extends Fragment {
    private static final String                  TAG            = "TCTransitionFragment";
    private static final String                  GIF_URL_PREFIX = "https://liteav.sdk.qcloud.com/app/res/gif/ugc/transition/";
    private              TXVideoEditer           mTXVideoEditer;
    private              RecyclerView            mRecycler;
    private              TransitionAdapter       mAdapter;
    private              List<TransitionModel>   mModelList;
    private              ColorfulProgress        mColorfulProgress;
    private              VideoProgressController mVideoProgressController;
    private              ImageView               mImageUndo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ugckit_fragment_transition, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoEditerSDK wrapper = VideoEditerSDK.getInstance();
        mTXVideoEditer = wrapper.getEditer();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TimeLineView timeLineView = TimelineViewUtil.getInstance().getTimeLineView();
        if (timeLineView != null) {
            mVideoProgressController = timeLineView.getVideoProgressController();
        }
        initData();
        initViews(getView());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        mColorfulProgress.setVisibility(hidden ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        List<ColorfulProgress.MarkInfo> markInfoList = mColorfulProgress.getMarkInfoList();
        TCTransitionViewInfoManager.getInstance().setMarkInfoList(markInfoList);
        super.onDestroyView();
    }

    private void initData() {
        mModelList = new ArrayList<>();
        TransitionModel modelNone = new TransitionModel(TXEVideoTransitionDef.NONE, R.string.trans_none, "none.gif");
        modelNone.isSelected = true;
        mModelList.add(modelNone);
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.DIRECTIONAL_RIGHT_TO_LEFT, R.string.trans_wipeLeft, "wipeLeft.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.DIRECTIONAL_LEFT_TO_RIGHT, R.string.trans_wipeRight, "wipeRight.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.DIRECTIONAL_BOTTOM_TO_TOP, R.string.trans_wipeUp, "wipeUp.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.DIRECTIONAL_TOP_TO_BOTTOM, R.string.trans_wipeDown, "wipeDown.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.FADE_COLOR, R.string.trans_fadecolor, "fadecolor.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.SIMPLE_ZOOM, R.string.trans_SimpleZoom, "SimpleZoom.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.LINEAR_BLUR, R.string.trans_LinearBlur, "LinearBlur.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.WATER_DROP, R.string.trans_WaterDrop, "WaterDrop.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.INVERTED_PAGE_CURL, R.string.trans_InvertedPageCurl, "InvertedPageCurl.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.GLITCH_MEMORIES, R.string.trans_GlitchMemories, "GlitchMemories.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.STEREO_VIEWER, R.string.trans_StereoViewer, "StereoViewer.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.DIRECTIONAL_WARP, R.string.trans_directionalwarp, "directionalwarp.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.BOUNCE, R.string.trans_Bounce, "Bounce.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.CIRCLE_CROP, R.string.trans_CircleCrop, "CircleCrop.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.SWIRL, R.string.trans_Swirl, "Swirl.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.CROSS_ZOOM, R.string.trans_CrossZoom, "CrossZoom.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.GRID_FLIP, R.string.trans_GridFlip, "GridFlip.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.MOSAIC, R.string.trans_Mosaic, "Mosaic.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.KALEIDO_SCOPE, R.string.trans_kaleidoscope, "kaleidoscope.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.HEXAGONALIZE, R.string.trans_hexagonalize, "hexagonalize.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.GLITCH_DISPLACE, R.string.trans_GlitchDisplace, "GlitchDisplace.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.DREAMY_ZOOM, R.string.trans_DreamyZoom, "DreamyZoom.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.BURN, R.string.trans_burn, "burn.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.CIRCLE, R.string.trans_circle, "circle.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.CROSS_WARP, R.string.trans_crosswarp, "crosswarp.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.CUBE, R.string.trans_cube, "cube.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.DOOR_WAY, R.string.trans_doorway, "doorway.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.FADE_GRAY_SCALE, R.string.trans_fadegrayscale, "fadegrayscale.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.FLY_EYE, R.string.trans_flyeye, "flyeye.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.PIXELIZE, R.string.trans_pixelize, "pixelize.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.SQUEEZE, R.string.trans_squeeze, "squeeze.gif"));
        mModelList.add(new TransitionModel(TXEVideoTransitionDef.SWAP, R.string.trans_swap, "swap.gif"));
    }

    private void initViews(View view) {
        mRecycler = (RecyclerView) view.findViewById(R.id.transition_recycler_view);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new TransitionAdapter();
        mAdapter.setData(mModelList);
        mRecycler.setAdapter(mAdapter);

        mColorfulProgress = new ColorfulProgress(getContext());
        mColorfulProgress.setWidthHeight(mVideoProgressController.getThumbnailPicListDisplayWidth(), getResources().getDimensionPixelOffset(R.dimen.ugckit_video_progress_height));
        mColorfulProgress.setMarkInfoList(TCTransitionViewInfoManager.getInstance().getMarkInfoList());
        mVideoProgressController.addColorfulProgress(mColorfulProgress);

        mImageUndo = (ImageView) view.findViewById(R.id.iv_undo);
        mImageUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoTransition();
            }
        });
        updateUndoVisibility();
    }

    private void updateUndoVisibility() {
        if (mColorfulProgress.getMarkListSize() > 0) {
            mImageUndo.setVisibility(View.VISIBLE);
        } else {
            mImageUndo.setVisibility(View.GONE);
        }
    }

    private void undoTransition() {
        ColorfulProgress.MarkInfo markInfo = mColorfulProgress.deleteLastMark();
        if (markInfo != null) {
            mVideoProgressController.setCurrentTimeMs(markInfo.startTimeMs);
            PlayerManagerKit.getInstance().previewAtTime(markInfo.startTimeMs);
        }

        mTXVideoEditer.deleteLastTransitionEffect();
        updateUndoVisibility();
    }

    private class TransitionModel {
        public int     type;
        public String  name;
        public String  gifUrl;
        public boolean isSelected = false;

        public TransitionModel(int type, int name, String gifName) {
            this.type = type;
            this.name = getResources().getString(name);
            this.gifUrl = GIF_URL_PREFIX + gifName;
        }
    }

    private class TransitionAdapter extends RecyclerView.Adapter<TransitionAdapter.TransitionViewHolder> {
        private List<TransitionModel> modelList;

        @Override
        public TransitionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.ugckit_item_transition, null);
            return new TransitionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TransitionViewHolder holder, int position) {
            final TransitionModel model = modelList.get(position);
            Glide.with(getContext()).load(model.gifUrl).into(holder.imageEffect);
            holder.tvName.setText(model.name);
            if (model.isSelected) {
                holder.imageSelect.setVisibility(View.VISIBLE);
            } else {
                holder.imageSelect.setVisibility(View.INVISIBLE);
            }
            holder.imageEffect.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean setResult = onEffectSelected(model);
                    if (!setResult) {
                        return;
                    }
                    for (TransitionModel tempModel : modelList) {
                        if (tempModel.type != model.type) {
                            tempModel.isSelected = false;
                            continue;
                        }
                        tempModel.isSelected = true;
                    }
                    notifyDataSetChanged();
                }

            });
        }

        private boolean onEffectSelected(TransitionModel model) {
            long startTime = mVideoProgressController.getCurrentTimeMs();
            long transitionDuration = 1000;
            long totalDuration = VideoEditerSDK.getInstance().getVideoDuration();
            boolean setTransitionResult = mTXVideoEditer.setTransitionEffect(model.type, totalDuration, startTime, transitionDuration);
            Log.d(TAG, "setTransitionResult=" + setTransitionResult);
            if (setTransitionResult) {
                mColorfulProgress.addMark(getResources().getColor(R.color.ugckit_transition_mark_color), transitionDuration);
                mColorfulProgress.invalidate();
                updateUndoVisibility();
            } else {
                ToastUtil.toastLongMessage(getResources().getString(R.string.trans_invalid_timestamp));
            }
            return setTransitionResult;
        }

        @Override
        public int getItemCount() {
            return modelList == null ? 0 : modelList.size();
        }

        public void setData(List<TransitionModel> mModelList) {
            modelList = mModelList;
        }

        private class TransitionViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageEffect;
            private ImageView imageSelect;
            private TextView  tvName;

            public TransitionViewHolder(View itemView) {
                super(itemView);
                imageEffect = (ImageView) itemView.findViewById(R.id.transition_image);
                imageSelect = (ImageView) itemView.findViewById(R.id.transition_select);
                tvName = (TextView) itemView.findViewById(R.id.transition_name);
            }
        }
    }
}
