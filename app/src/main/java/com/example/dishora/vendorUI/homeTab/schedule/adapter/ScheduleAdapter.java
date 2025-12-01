package com.example.dishora.vendorUI.homeTab.schedule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.vendorUI.homeTab.schedule.model.ScheduleItem;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final List<ScheduleItem> scheduleList;

    public ScheduleAdapter(List<ScheduleItem> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem item = scheduleList.get(position);

        holder.tvDate.setText(item.availableDate);
        holder.tvCapacity.setText(String.valueOf(item.maxOrders));
        holder.tvCurrentOrders.setText(String.valueOf(item.currentOrders));

        int remaining = item.getRemainingCapacity();
        holder.tvStatus.setText(remaining > 0 ? remaining + " remaining" : "Capacity FULL");

        // Optional: Change color if full
        int colorRes = remaining > 0 ? R.color.green : R.color.red; // Define these colors in colors.xml
        holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(colorRes));
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvDate;
        public final TextView tvCapacity;
        public final TextView tvCurrentOrders;
        public final TextView tvStatus;

        public ViewHolder(View view) {
            super(view);
            // Assuming IDs in item_schedule.xml
            tvDate = view.findViewById(R.id.tvScheduleDate);
            tvCapacity = view.findViewById(R.id.tvMaxCapacity);
            tvCurrentOrders = view.findViewById(R.id.tvCurrentOrders);
            tvStatus = view.findViewById(R.id.tvStatus);
        }
    }
}