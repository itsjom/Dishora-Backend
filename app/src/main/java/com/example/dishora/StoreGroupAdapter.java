package com.example.dishora;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoreGroupAdapter extends RecyclerView.Adapter<StoreGroupAdapter.GroupViewHolder> {

    Context context;
    List<StoreOrderGroup> groups;

    public StoreGroupAdapter(Context context, List<StoreOrderGroup> groups) {
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        StoreOrderGroup group = groups.get(position);

        // Clear and add all food items dynamically
        holder.itemContainer.removeAllViews();

        for (OrderItem item : group.itemList) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_order, holder.itemContainer, false);

            TextView name = itemView.findViewById(R.id.textViewName);
            TextView location = itemView.findViewById(R.id.textViewLocation);
            TextView price = itemView.findViewById(R.id.textViewPrice);
            ImageView image = itemView.findViewById(R.id.imageView);

            name.setText(item.name);
            location.setText("Zone 6, Maguikay Ave, Naga City");
            price.setText(item.price);
            image.setImageResource(item.image);

            holder.itemContainer.addView(itemView);
        }

        // You can add listener to Pay button
        holder.payBtn.setOnClickListener(v -> {
            Toast.makeText(context, "Paying for " + group.storeName, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemContainer;
        Button payBtn;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.itemContainer);
            payBtn = itemView.findViewById(R.id.payButton);
        }
    }
}

