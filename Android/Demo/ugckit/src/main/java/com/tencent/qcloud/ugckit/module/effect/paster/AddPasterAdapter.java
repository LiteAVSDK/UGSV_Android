package com.tencent.qcloud.ugckit.module.effect.paster;

import android.content.Context;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.module.effect.BaseRecyclerAdapter;
import com.tencent.qcloud.ugckit.R;


import java.util.List;

public class AddPasterAdapter extends BaseRecyclerAdapter<AddPasterAdapter.AddPasterViewHolder> {
    public static final int TYPE_FOOTER = 0;  // 带有Footer的
    public static final int TYPE_NORMAL = 1;  // 真实数据

    private Context            mContext;
    private View               mFooterView;
    private int                mCurrentSelectedPos = -1;
    private int                mPasterTextSize;
    private int                mPasterTextColor;
    private int                mCoverIcon;
    private List<TCPasterInfo> mPasterInfoList;

    public AddPasterAdapter(List<TCPasterInfo> pasterInfoList, Context context) {
        mContext = context;
        mPasterInfoList = pasterInfoList;
    }

    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(getItemCount() - 1);
    }

    public void setCurrentSelectedPos(int pos) {
        int tPos = mCurrentSelectedPos;
        mCurrentSelectedPos = pos;
        this.notifyItemChanged(tPos);
        this.notifyItemChanged(mCurrentSelectedPos);
    }

    @Override
    public int getItemViewType(int position) {
        if (mFooterView == null) {
            return TYPE_NORMAL;
        }
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    @Override
    public void onBindVH(@NonNull AddPasterViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            return;
        }
        String pasterPath = mPasterInfoList.get(position).getIconPath();
        if (!TextUtils.isEmpty(pasterPath)) {
            holder.ivAddPaster.setImageBitmap(BitmapFactory.decodeFile(pasterPath));
        }
        holder.tvAddPasterText.setText(UGCKit.getAppContext().getResources().getString(R.string.ugckit_add_paster_adapter_paster) + String.valueOf(position + 1));
        if (mCoverIcon != 0) {
            if (mPasterTextSize != 0) {
                holder.tvAddPasterText.setTextSize(mPasterTextSize);
            }
            if (mPasterTextColor != 0) {
                holder.tvAddPasterText.setTextColor(mContext.getResources().getColor(mPasterTextColor));
            }
            holder.ivAddPasterTint.setImageResource(mCoverIcon);
        }
        if (mCurrentSelectedPos == position) {
            holder.ivAddPasterTint.setVisibility(View.VISIBLE);
        } else {
            holder.ivAddPasterTint.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public AddPasterViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        if (mFooterView != null && viewType == TYPE_FOOTER) {
            return new AddPasterViewHolder(mFooterView);
        }
        return new AddPasterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ugckit_item_add_paster, parent, false));
    }

    @Override
    public int getItemCount() {
        if (mFooterView != null) {
            return mPasterInfoList.size() + 1;
        }
        return mPasterInfoList.size();
    }

    public void setPasterTextSize(int textSize) {
        mPasterTextSize = textSize;
    }

    public void setPasterTextColor(int textColor) {
        mPasterTextColor = textColor;
    }

    public void setCoverIconResouce(int icon) {
        mCoverIcon = icon;
    }

    public class AddPasterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAddPaster;
        ImageView ivAddPasterTint;
        TextView  tvAddPasterText;

        public AddPasterViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView == mFooterView) {
                return;
            }
            ivAddPaster = (ImageView) itemView.findViewById(R.id.add_paster_image);
            ivAddPasterTint = (ImageView) itemView.findViewById(R.id.add_paster_tint);
            tvAddPasterText = (TextView) itemView.findViewById(R.id.add_paster_tv_name);
        }
    }
}
