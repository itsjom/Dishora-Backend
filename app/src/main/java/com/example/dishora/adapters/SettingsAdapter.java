package com.example.dishora.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.models.SettingItem;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private final List<SettingItem> settingItems;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SettingItem item, int position);
    }

    public SettingsAdapter(Context context, List<SettingItem> settingItems, OnItemClickListener listener) {
        this.context = context;
        this.settingItems = settingItems;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            label = view.findViewById(R.id.label);
        }

        public void bind(SettingItem item, int position, OnItemClickListener listener) {
            label.setText(item.getLabel());
            icon.setImageResource(item.getIconResId());
            itemView.setOnClickListener(v -> listener.onItemClick(item, position));
        }
    }

    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SettingsAdapter.ViewHolder holder, int position) {
        holder.bind(settingItems.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return settingItems.size();
    }
}
