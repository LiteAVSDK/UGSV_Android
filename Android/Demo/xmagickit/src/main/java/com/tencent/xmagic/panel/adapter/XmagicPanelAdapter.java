package com.tencent.xmagic.panel.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tencent.xmagic.demo.R;
import com.tencent.xmagic.module.XmagicUIProperty;
import com.tencent.xmagic.panel.XmagicPanelDataManager;
import com.tencent.xmagic.panel.adapter.XmagicPanelAdapter.PropertyHolder;

import java.util.List;

public class XmagicPanelAdapter extends RecyclerView.Adapter<PropertyHolder> implements View.OnClickListener {

    private XmagicPanelItemClickListener mListener;
    private List<XmagicUIProperty<?>> properties;

    public XmagicPanelAdapter(XmagicPanelItemClickListener xmagicPanelItemClickListener) {
        mListener = xmagicPanelItemClickListener;
    }

    @NonNull
    @Override
    public PropertyHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_property_view, viewGroup, false);
        return new PropertyHolder(view);
    }

    @Override
    public int getItemCount() {
        if (properties == null) {
            return 0;
        }
        return properties.size();
    }

    @Override
    public void onBindViewHolder(final PropertyHolder holder, int position) {
        XmagicUIProperty<?> xmagicProperty = properties.get(position);
        if (xmagicProperty.uiCategory == XmagicUIProperty.UICategory.KV) {
            holder.itemView.setOnClickListener(null);
            boolean isOpen = XmagicPanelDataManager.getInstance().isPanelBeautyOpen();
            final String closeTip = holder.itemView.getResources().getString(R.string.pannel_beauty_switch_close_txt);
            final String openTip = holder.itemView.getResources().getString(R.string.pannel_beauty_switch_open_txt);
            holder.tvItemName.setText(isOpen ? closeTip : openTip);
            holder.tvItemName.setTextColor(Color.parseColor("#ffffff"));
            holder.foregroundImg.setVisibility(View.GONE);
            holder.ivItemIcon.setVisibility(View.INVISIBLE);
            holder.aSwitch.setVisibility(View.VISIBLE);
            holder.rightLine.setVisibility(View.VISIBLE);
            holder.aSwitch.setOnCheckedChangeListener(null);
            holder.aSwitch.setChecked(XmagicPanelDataManager.getInstance().isPanelBeautyOpen());
            holder.aSwitch.setEnabled(true);
            holder.aSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    buttonView.setEnabled(false);
                    XmagicPanelDataManager.getInstance().setPanelBeautyOpen(isChecked);
                    if (mListener != null) {
                        mListener.onBeautySwitchCheckedChange(isChecked);
                    }
                    holder.tvItemName.setText(isChecked ? closeTip : openTip);
                    holder.tvItemName.setTextColor(Color.parseColor("#ffffff"));
                    notifyDataSetChanged();
                }
            }));
        } else {
            holder.aSwitch.setVisibility(View.GONE);
            holder.rightLine.setVisibility(View.GONE);
            //如果是美颜项，并且美颜开关关闭的情况
            holder.itemView.setTag(properties.get(position));
            holder.tvItemName.setText(xmagicProperty.displayName);
            holder.ivItemIcon.setVisibility(View.VISIBLE);
            if (xmagicProperty.thumbDrawable != 0) {
                Glide.with(holder.ivItemIcon.getContext()).load(xmagicProperty.thumbDrawable).into(holder.ivItemIcon);
            } else {
                Glide.with(holder.ivItemIcon.getContext()).load(xmagicProperty.thumbImagePath).into(holder.ivItemIcon);
            }
            boolean isPanelBeautyState = XmagicPanelDataManager.getInstance().isPanelBeautyOpen();
            if (xmagicProperty.uiCategory == XmagicUIProperty.UICategory.BEAUTY && !isPanelBeautyState) {
                holder.itemView.setOnClickListener(null);
                holder.foregroundImg.setVisibility(View.VISIBLE);
                holder.tvItemName.setTextColor(Color.parseColor("#80ffffff"));
                holder.tvItemName.setTypeface(null, Typeface.NORMAL);
                Drawable drawable = holder.itemView.getResources().getDrawable(R.drawable.ratio_bg_00ffffff);
                holder.ivItemIcon.setBackground(drawable);
            } else {
                holder.itemView.setOnClickListener(this);
                holder.foregroundImg.setVisibility(View.GONE);
                if (isSelected(xmagicProperty)) {
                    holder.tvItemName.setTextColor(Color.parseColor("#F14257"));
                    holder.tvItemName.setTypeface(null, Typeface.BOLD);
                    Drawable drawable = holder.itemView.getResources().getDrawable(R.drawable.ratio_bg_f14257);
                    holder.ivItemIcon.setBackground(drawable);
                } else {
                    holder.tvItemName.setTextColor(Color.parseColor("#ffffff"));
                    holder.tvItemName.setTypeface(null, Typeface.NORMAL);
                    Drawable drawable = holder.itemView.getResources().getDrawable(R.drawable.ratio_bg_00ffffff);
                    holder.ivItemIcon.setBackground(drawable);
                }
            }
        }
    }


    private long lastTime = 0L;

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            XmagicUIProperty<?> xmagicUIProperty = (XmagicUIProperty<?>) v.getTag();
            if (xmagicUIProperty != null
                    && xmagicUIProperty.uiCategory == XmagicUIProperty.UICategory.SEGMENTATION
                    && xmagicUIProperty.property != null
                    && ("video_segmentation_transparent_bg".equals(xmagicUIProperty.property.id)
                    || "video_segmentation_blur_75".equals(xmagicUIProperty.property.id))) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTime < 3 * 1000) {
                    return;
                }
                lastTime = currentTime;
            }
            mListener.onItemClick(xmagicUIProperty);
        }
    }

    public void setProperties(List<XmagicUIProperty<?>> properties) {
        this.properties = properties;
        notifyDataSetChanged();
    }


    /**
     * 获取被选中的item的位置
     * Get the position of the checked item
     *
     * @return position 如果返回值为-1，则表示没有选中任何一项
     * @return position if position is -1 ,Indicates that none of the items are selected
     */
    public int getCheckedPosition() {
        for (int i = 0; i < this.properties.size(); i++) {
            XmagicUIProperty<?> xmagicUIProperty = this.properties.get(i);
            if (xmagicUIProperty != null && isSelected(xmagicUIProperty)) {
                if (mListener != null) {
                    mListener.onChecked(xmagicUIProperty);
                    return i;
                }
            }
        }
        return -1;
    }

    public interface XmagicPanelItemClickListener {
        void onItemClick(XmagicUIProperty<?> xmagicUIProperty);

        void onChecked(XmagicUIProperty<?> xmagicUIProperty);

        void onBeautySwitchCheckedChange(boolean isChecked);
    }

    /**
     * 判断当前item是否是选中状态
     * Determine whether the current item is checked
     *
     * @param xmagicUIProperty
     * @return
     */
    private boolean isSelected(XmagicUIProperty xmagicUIProperty) {
        return XmagicPanelDataManager.getInstance().getSelectedItems().containsValue(xmagicUIProperty);
    }

    static class PropertyHolder extends RecyclerView.ViewHolder {
        TextView tvItemName;
        ImageView ivItemIcon;
        View itemView;
        ImageView foregroundImg;
        Switch aSwitch;
        View rightLine;

        public PropertyHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            ivItemIcon = itemView.findViewById(R.id.iv_item_icon);
            foregroundImg = itemView.findViewById(R.id.img_item_foreground_gray);
            aSwitch = itemView.findViewById(R.id.item_beauty_switch);
            rightLine = itemView.findViewById(R.id.item_right_line);
        }
    }
}
