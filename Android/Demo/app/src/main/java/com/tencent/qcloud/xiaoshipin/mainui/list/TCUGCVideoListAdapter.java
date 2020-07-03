package com.tencent.qcloud.xiaoshipin.mainui.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.tencent.qcloud.ugckit.utils.TCUtils;
import com.tencent.qcloud.ugckit.module.effect.BaseRecyclerAdapter;
import com.tencent.qcloud.xiaoshipin.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 小视频列表的Adapter
 * 列表项布局格式: R.layout.listview_ugc_item
 * 列表项数据格式: TCLiveInfo
 */
public class TCUGCVideoListAdapter extends BaseRecyclerAdapter<TCUGCVideoListAdapter.VideoVideoHolder> {
    private List<TCVideoInfo> mList;
    private Context mContext;

    public TCUGCVideoListAdapter(Context context, List<TCVideoInfo> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public void onBindVH(VideoVideoHolder holder, int position) {
        TCVideoInfo data = mList.get(position);
        //UGC预览图
        String cover = data.frontcover;
        if (TextUtils.isEmpty(cover) || data.review_status == TCVideoInfo.REVIEW_STATUS_PORN) { // 涉黄的图片不显示
            holder.ivCover.setImageResource(R.drawable.bg_ugc);
        } else {
            RequestManager req = Glide.with(holder.itemView.getContext());
            req.load(cover).placeholder(R.drawable.bg_ugc).into(holder.ivCover);
        }
        //头像
        TCUtils.showPicWithUrl(holder.itemView.getContext(), holder.ivAvatar, data.headpic, R.drawable.face);
        //昵称
        if (TextUtils.isEmpty(data.nickname) || "null".equals(data.nickname)) {
            holder.tvHost.setText(TCUtils.getLimitString(data.userid, 10));
        } else {
            holder.tvHost.setText(TCUtils.getLimitString(data.nickname, 10));
        }
        //小视频创建时间（发布时间）
        holder.tvCreateTime.setText(generateTimeStr(convertTimeToLong(data.createTime)));

        if (data.review_status == TCVideoInfo.REVIEW_STATUS_NORMAL) {
            holder.reviewStatus.setText(mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_video_state_normal));
        } else if (data.review_status == TCVideoInfo.REVIEW_STATUS_NOT_REVIEW) { // 审核中
            holder.reviewStatus.setText(mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_video_state_in_audit));
        } else if (data.review_status == TCVideoInfo.REVIEW_STATUS_PORN) { // 涉黄
            holder.reviewStatus.setText(mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_video_state_pornographic));
        }
    }

    public static Long convertTimeToLong(String time) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = sdf.parse(time);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    public VideoVideoHolder onCreateVH(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_ugc_item, null);
        DisplayMetrics dm = parent.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        itemView.setLayoutParams(new RecyclerView.LayoutParams(width / 2, width / 2));
        return new VideoVideoHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public String generateTimeStr(long timestamp) {
        String result = mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_just_now);
        long timeDistanceinSec = (System.currentTimeMillis() - timestamp) / 1000;
        if (timeDistanceinSec >= 60 && timeDistanceinSec < 3600) {
            long min = timeDistanceinSec / 60;
            if(min > 1){
                result = String.format("%d ", (timeDistanceinSec) / 60) +
                        mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_mins_ago);
            }else{
                result = String.format("%d ", (timeDistanceinSec) / 60) +
                        mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_min_ago);
            }
        } else if (timeDistanceinSec >= 3600 && timeDistanceinSec < 60 * 60 * 24) {
            long hour = timeDistanceinSec / 3600;
            if(hour > 1){
                result = String.format("%d ", (timeDistanceinSec) / 3600) +
                        mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_hours_ago);
            }else{
                result = String.format("%d ", (timeDistanceinSec) / 3600) +
                        mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_hour_ago);
            }
        } else if (timeDistanceinSec >= 3600 * 24) {
            long day = timeDistanceinSec / (3600 * 24);
            if(day > 1){
                result = String.format("%d ", (timeDistanceinSec) / (3600 * 24)) +
                        mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_days_ago);
            }else{
                result = String.format("%d ", (timeDistanceinSec) / (3600 * 24)) +
                        mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_day_ago);
            }

        }
        return result;
    }


    public static class VideoVideoHolder extends RecyclerView.ViewHolder {
        TextView tvHost;
        ImageView ivCover;
        ImageView ivAvatar;
        TextView tvCreateTime;
        TextView reviewStatus;

        public VideoVideoHolder(View itemView) {
            super(itemView);
            ivCover = (ImageView) itemView.findViewById(R.id.cover);
            tvHost = (TextView) itemView.findViewById(R.id.host_name);
            ivAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            tvCreateTime = (TextView) itemView.findViewById(R.id.create_time);
            reviewStatus = (TextView) itemView.findViewById(R.id.review_status);
        }
    }
}


