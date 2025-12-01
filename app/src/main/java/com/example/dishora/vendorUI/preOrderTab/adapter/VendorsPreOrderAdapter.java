package com.example.dishora.vendorUI.preOrderTab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.vendorUI.preOrderTab.model.GroupedPreOrder;
import com.example.dishora.vendorUI.preOrderTab.model.PreOrderItem;

import java.util.ArrayList;
import java.util.List;

public class VendorsPreOrderAdapter extends RecyclerView.Adapter<VendorsPreOrderAdapter.PreOrderViewHolder> {

    private List<GroupedPreOrder> orderGroups;

    private OnStatusUpdateListener statusUpdateListener;
    public interface OnStatusUpdateListener {
        void onStatusUpdate(GroupedPreOrder group, String newStatus);
    }
    public void setOnStatusUpdateListener(OnStatusUpdateListener listener) {
        this.statusUpdateListener = listener;
    }


    public VendorsPreOrderAdapter() {
        this.orderGroups = new ArrayList<>();
    }

    public void setOrders(List<GroupedPreOrder> orderGroups) {
        this.orderGroups = orderGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PreOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ CORRECTED: Use the item_vendor_order_GROUP layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor_order_group, parent, false);
        return new PreOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreOrderViewHolder holder, int position) {
        GroupedPreOrder group = orderGroups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return orderGroups.size();
    }

    // --- ViewHolder ---
    class PreOrderViewHolder extends RecyclerView.ViewHolder {

        private TextView textCustomerName, textTotal, textStatus, textOrderId;
        private RecyclerView recyclerOrderDetails;
        private Button btnAccept, btnCancel;
        private Context context;

        public PreOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();

            // ✅ CORRECTED: Uncommented all views
            textCustomerName = itemView.findViewById(R.id.textCustomerName);
            textTotal = itemView.findViewById(R.id.textTotalAmount);
            textStatus = itemView.findViewById(R.id.textOrderStatus);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            recyclerOrderDetails = itemView.findViewById(R.id.recyclerOrderDetails);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        public void bind(GroupedPreOrder group) {
            textCustomerName.setText(group.getCustomerName());
            textOrderId.setText("Order #" + group.getGroupId());
            textTotal.setText("Total: ₱" + String.format("%.2f", group.getTotalAmount()));
            textStatus.setText(group.getStatus());

            OrderDetailsAdapter detailsAdapter = new OrderDetailsAdapter(group.getItems());
            recyclerOrderDetails.setLayoutManager(new LinearLayoutManager(context));
            recyclerOrderDetails.setAdapter(detailsAdapter);

            String status = group.getStatus().toLowerCase();
            if (status.equals("pending")) {
                btnAccept.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
            } else {
                btnAccept.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
            }

            btnAccept.setOnClickListener(v -> {
                if (statusUpdateListener != null) {
                    statusUpdateListener.onStatusUpdate(group, "Preparing");
                }
            });

            btnCancel.setOnClickListener(v -> {
                if (statusUpdateListener != null) {
                    statusUpdateListener.onStatusUpdate(group, "Cancelled");
                }
            });
        }
    }

    // --- Nested Adapter for Order Items ---
    class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.DetailsViewHolder> {

        private List<PreOrderItem> items;

        public OrderDetailsAdapter(List<PreOrderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public DetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // ✅ CORRECTED: Use the item_vendor_order_DETAILS layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vendor_order_details, parent, false);
            return new DetailsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DetailsViewHolder holder, int position) {
            PreOrderItem item = items.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class DetailsViewHolder extends RecyclerView.ViewHolder {
            TextView textItemName, textItemQuantity, textItemPrice;

            public DetailsViewHolder(@NonNull View itemView) {
                super(itemView);
                textItemName = itemView.findViewById(R.id.textItemName);
                textItemQuantity = itemView.findViewById(R.id.textItemQuantity);
                textItemPrice = itemView.findViewById(R.id.textItemPrice);
            }

            public void bind(PreOrderItem item) {
                textItemName.setText(item.getMenuItemName());
                textItemQuantity.setText(item.getQuantity() + "x");
                textItemPrice.setText("₱" + String.format("%.2f", item.getPrice()));
            }
        }
    }
}