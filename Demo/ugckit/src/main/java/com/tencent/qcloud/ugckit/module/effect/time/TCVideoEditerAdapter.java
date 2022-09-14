package com.tencent.qcloud.ugckit.module.effect.time;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.tencent.qcloud.ugckit.R;

import java.util.ArrayList;

public class TCVideoEditerAdapter extends RecyclerView.Adapter<TCVideoEditerAdapter.ViewHolder> {
    private final Context           mContext;
    @NonNull
    private       ArrayList<Bitmap> data = new ArrayList<Bitmap>();

    public TCVideoEditerAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int height = mContext.getResources().getDimensionPixelOffset(R.dimen.ugckit_item_thumb_height);
        ImageView view = new ImageView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(height, height));
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.thumb.setImageBitmap(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(int position, Bitmap b) {
        data.add(b);
        notifyItemInserted(position);
    }

    public void clearAllBitmap() {
        data.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ImageView thumb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = (ImageView) itemView;
        }
    }

}
