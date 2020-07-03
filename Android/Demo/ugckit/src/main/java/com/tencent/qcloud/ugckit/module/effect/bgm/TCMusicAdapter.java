package com.tencent.qcloud.ugckit.module.effect.bgm;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.module.effect.BaseRecyclerAdapter;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.progressbutton.SampleProgressButton;
import com.tencent.rtmp.TXLog;


import java.util.List;

public class TCMusicAdapter extends BaseRecyclerAdapter<TCMusicAdapter.LinearMusicViewHolder> implements View.OnClickListener {
    private static final String TAG = "TCMusicAdapter";
    private Context mContext;
    private List<TCMusicInfo> mBGMList;

    private OnClickSubItemListener mOnClickSubItemListener;

    @NonNull
    private SparseArray<LinearMusicViewHolder> mProgressButtonIndexMap = new SparseArray<LinearMusicViewHolder>();

    public TCMusicAdapter(Context context, List<TCMusicInfo> list) {
        mContext = context;
        mBGMList = list;
    }

    @NonNull
    @Override
    public LinearMusicViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        LinearMusicViewHolder linearMusicViewHolder = new LinearMusicViewHolder(View.inflate(parent.getContext(), R.layout.item_editer_bgm, null));
        return linearMusicViewHolder;
    }

    @Override
    public void onBindVH(@NonNull LinearMusicViewHolder holder, int position) {
        TCMusicInfo info = mBGMList.get(position);

        holder.btnUse.setMax(100);
        if (info.status == TCMusicInfo.STATE_UNDOWNLOAD) {
            holder.btnUse.setText(mContext.getString(R.string.download));
            holder.btnUse.setState(SampleProgressButton.STATE_NORMAL);
            holder.btnUse.setNormalColor(Color.parseColor("#6C7B8B"));
        } else if (info.status == TCMusicInfo.STATE_DOWNLOADED) {
            holder.btnUse.setText(mContext.getString(R.string.use));
            holder.btnUse.setState(SampleProgressButton.STATE_NORMAL);
            holder.btnUse.setNormalColor(Color.parseColor("#FF6347"));
        } else if (info.status == TCMusicInfo.STATE_DOWNLOADING) {
            holder.btnUse.setText(mContext.getString(R.string.downloading));
            holder.btnUse.setState(SampleProgressButton.STATE_PROGRESS);
            holder.btnUse.setProgress(info.progress);
            holder.btnUse.setNormalColor(Color.parseColor("#FF6347"));
        }
        TXLog.d(TAG, "onBindVH   info.status:" + info.status);

        holder.tvName.setText(info.name);
        holder.itemView.setTag(position);
        holder.setPosition(position);
        holder.setOnClickSubItemListener(mOnClickSubItemListener);
        holder.setOnItemClickListener(mOnItemClickListener);

        mProgressButtonIndexMap.put(position, holder);
    }

    @Override
    public void onBindViewHolder(LinearMusicViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return mBGMList.size();
    }

    public void updateItem(int position, @NonNull TCMusicInfo info) {
        LinearMusicViewHolder holder = mProgressButtonIndexMap.get(position);
        if (holder == null) {
            return;
        }
        if (info.status == TCMusicInfo.STATE_UNDOWNLOAD) {
            holder.btnUse.setText(mContext.getString(R.string.download));
            holder.btnUse.setState(SampleProgressButton.STATE_NORMAL);
            holder.btnUse.setNormalColor(Color.parseColor("#6C7B8B"));
        } else if (info.status == TCMusicInfo.STATE_DOWNLOADED) {
            holder.btnUse.setText(mContext.getString(R.string.use));
            holder.btnUse.setState(SampleProgressButton.STATE_NORMAL);
            holder.btnUse.setNormalColor(Color.parseColor("#FF6347"));
        } else if (info.status == TCMusicInfo.STATE_DOWNLOADING) {
            holder.btnUse.setText(mContext.getString(R.string.downloading));
            holder.btnUse.setState(SampleProgressButton.STATE_PROGRESS);
            holder.btnUse.setProgress(info.progress);
            holder.btnUse.setNormalColor(Color.parseColor("#FF6347"));
        }
        TXLog.d(TAG, "onBindVH   info.status:" + info.status);

        holder.tvName.setText(info.name);
        holder.itemView.setTag(position);
        holder.setPosition(position);
        holder.setOnClickSubItemListener(mOnClickSubItemListener);
        holder.setOnItemClickListener(mOnItemClickListener);
    }

    public static class LinearMusicViewHolder extends RecyclerView.ViewHolder {
        private SampleProgressButton btnUse;
        private TextView tvName;
        private OnItemClickListener onItemClickListener;
        private int position;

        public LinearMusicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.bgm_tv_name);
            btnUse = (SampleProgressButton) itemView.findViewById(R.id.btn_use);
            btnUse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnClickSubItemListener != null) {
                        mOnClickSubItemListener.onClickUseBtn(btnUse, position);
                    }
                }
            });
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        private OnClickSubItemListener mOnClickSubItemListener;

        public void setOnClickSubItemListener(OnClickSubItemListener onClickSubItemListener) {
            mOnClickSubItemListener = onClickSubItemListener;
        }
    }

    public void setOnClickSubItemListener(OnClickSubItemListener onClickSubItemListener) {
        mOnClickSubItemListener = onClickSubItemListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnClickSubItemListener {
        void onClickUseBtn(SampleProgressButton button, int position);
    }

}
