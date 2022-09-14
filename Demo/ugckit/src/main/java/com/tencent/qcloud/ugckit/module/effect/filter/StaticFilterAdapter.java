package com.tencent.qcloud.ugckit.module.effect.filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.module.effect.BaseRecyclerAdapter;
import com.tencent.qcloud.ugckit.R;


import java.util.List;

public class StaticFilterAdapter extends BaseRecyclerAdapter<StaticFilterAdapter.FilterViewHolder> {
    private List<Integer> mFilterList;
    private List<String>  mFileterNameList;
    private int           mCurrentSelectedPos;

    public StaticFilterAdapter(List<Integer> list, List<String> filerNameList) {
        mFilterList = list;
        mFileterNameList = filerNameList;
    }


    public void setCurrentSelectedPos(int pos) {
        int tPos = mCurrentSelectedPos;
        mCurrentSelectedPos = pos;
        this.notifyItemChanged(tPos);
        this.notifyItemChanged(mCurrentSelectedPos);
    }

    @Override
    public void onBindVH(@NonNull FilterViewHolder holder, int position) {
        holder.ivImage.setImageResource(mFilterList.get(position));
        holder.tvName.setText(mFileterNameList.get(position));
        if (mCurrentSelectedPos == position) {
            holder.ivImageTint.setVisibility(View.VISIBLE);
        } else {
            holder.ivImageTint.setVisibility(View.GONE);
        }
    }


    @NonNull
    @Override
    public FilterViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        return new FilterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ugckit_filter_layout, parent, false));
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivImageTint;
        TextView  tvName;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.filter_image);
            ivImageTint = (ImageView) itemView.findViewById(R.id.filter_image_tint);
            tvName = (TextView) itemView.findViewById(R.id.filter_tv_name);
        }
    }
}
