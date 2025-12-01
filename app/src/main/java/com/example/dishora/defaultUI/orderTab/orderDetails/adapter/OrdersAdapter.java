package com.example.dishora.defaultUI.orderTab.orderDetails.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.orderTab.model.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(long orderId);
    }

    private final List<Order> orderList;
    private final OnOrderClickListener listener;
    private final Context context;

    public OrdersAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.getId());
        holder.tvPlacedDate.setText("Placed " + order.getPlacedDate());
        holder.tvVendor.setText("Vendor: " + order.getVendorName());
        holder.tvTotal.setText(String.format(Locale.getDefault(), "₱%.2f", order.getTotal()));

        // Status chip logic (no change needed here)
        holder.chipStatus.setText(order.getStatus());
        int colorId;
        switch (order.getStatus().toLowerCase(Locale.ROOT)) {
            case "pending":
                colorId = R.color.orange;
                break;
            case "preparing": // NEW
                colorId = R.color.blue;
                break;
            case "for delivery": // NEW
                colorId = R.color.red;
                break;
            case "completed":
                colorId = R.color.green;
                break;
            case "cancelled":
                colorId = R.color.gray;
                break;
            default:
                colorId = R.color.gray; // Fallback
        }
        holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, colorId)));
        holder.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.white));

        // CRITICAL CHANGE: Pass only the order ID
        holder.btnViewDetails.setOnClickListener(v -> listener.onOrderClick(order.getId()));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvPlacedDate, tvVendor, tvTotal;
        Chip chipStatus;
        MaterialButton btnViewDetails;
        MaterialCardView cardView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvPlacedDate = itemView.findViewById(R.id.tvPlacedDate);
            tvVendor = itemView.findViewById(R.id.tvVendor);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            cardView = (MaterialCardView) itemView;
        }
    }
}
