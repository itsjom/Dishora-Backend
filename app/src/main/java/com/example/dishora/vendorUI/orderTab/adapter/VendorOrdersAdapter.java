package com.example.dishora.vendorUI.orderTab.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dishora.R;
import com.example.dishora.vendorUI.orderTab.model.GroupedOrder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for displaying GROUPED vendor orders (customer cards).
 */
public class VendorOrdersAdapter extends RecyclerView.Adapter<VendorOrdersAdapter.ViewHolder> {

    // Interface now sends the entire GroupedOrder object
    public interface OnStatusUpdateListener {
        void onUpdate(GroupedOrder group, String newStatus);
    }
    private OnStatusUpdateListener statusUpdateListener;
    public void setOnStatusUpdateListener(OnStatusUpdateListener listener) {
        this.statusUpdateListener = listener;
    }

    // The list now holds GroupedOrder objects
    private final List<GroupedOrder> orders = new ArrayList<>();

    public void setOrders(List<GroupedOrder> newOrders) {
        orders.clear();
        if (newOrders != null) orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupedOrder group = orders.get(position);
        if (group == null) return;

        String status = group.getStatus();

        // --- Bind Group Data ---
        holder.customerName.setText(group.getCustomerName());
        holder.orderDate.setText("Order Date: " + group.getOrderDate());
        holder.statusChip.setText(status);

        // Contact Number
        if (group.getContactNumber() != null && !group.getContactNumber().isEmpty()) {
            holder.contactNumber.setText("Contact: " + group.getContactNumber());
            holder.contactNumber.setVisibility(View.VISIBLE);
        } else {
            holder.contactNumber.setVisibility(View.GONE);
        }

        // Address
        if (group.getDeliveryAddress() != null && !group.getDeliveryAddress().isEmpty()) {
            holder.address.setText("Address: " + group.getDeliveryAddress());
            holder.address.setVisibility(View.VISIBLE);
        } else {
            holder.address.setVisibility(View.GONE);
        }

        // Delivery Date
        if (group.getDeliveryDate() != null) {
            holder.deliveryDate.setText("Delivery: " + group.getDeliveryDate().split("T")[0]);
            holder.deliveryDate.setVisibility(View.VISIBLE);
        } else {
            holder.deliveryDate.setVisibility(View.GONE);
        }

        // --- Setup Nested RecyclerView ---
        OrderProductAdapter itemAdapter = new OrderProductAdapter(group.getItems());
        holder.internalRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.internalRecyclerView.setAdapter(itemAdapter);

        // --- Payment Info ---
        String formattedTotal = String.format("%.2f", group.getTotalPrice());
        String paymentMethod = group.getPaymentMethod();

        if (paymentMethod != null && paymentMethod.equalsIgnoreCase("Cash on Delivery")) {
            holder.payment.setText("Collect: ₱" + formattedTotal);
            holder.payment.setTextColor(Color.parseColor("#007BFF"));
        } else {
            holder.payment.setText("Paid: ₱" + formattedTotal);
            holder.payment.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.orange));
        }

        // --- Status Chip Color ---
        int color;
        switch (status.toLowerCase()) {
            case "pending": color = Color.parseColor("#FFA500"); break;
            case "preparing": color = Color.parseColor("#2196F3"); break;
            case "for delivery": color = Color.parseColor("#4CAF50"); break;
            case "completed": color = Color.parseColor("#9C27B0"); break;
            case "cancelled": color = Color.parseColor("#BDBDBD"); break;
            default: color = Color.GRAY;
        }
        holder.statusChip.setBackgroundTintList(ColorStateList.valueOf(color));

        // --- Show/Hide Update Controls ---
        if ("completed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
            holder.updateControlsLayout.setVisibility(View.GONE);
        } else {
            holder.updateControlsLayout.setVisibility(View.VISIBLE);

            // Set up dropdown options
            List<String> nextOptions;
            switch (status.toLowerCase()) {
                case "pending": nextOptions = Arrays.asList("Preparing", "Cancelled", "For Delivery", "Completed"); break;
                case "preparing": nextOptions = Arrays.asList("For Delivery", "Completed"); break;
                case "for delivery": nextOptions = Collections.singletonList("Completed"); break;
                default: nextOptions = Collections.emptyList();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(), R.layout.list_item_status, nextOptions);
            holder.dropdownStatus.setAdapter(adapter);
            holder.dropdownStatus.setText(status, false); // Pre-select current status
            holder.dropdownStatus.setEnabled(!nextOptions.isEmpty());
            holder.textInputLayout.setHint(nextOptions.isEmpty() ? "No further updates" : "Select next status");

            // Set button click listener
            holder.buttonApplyStatus.setOnClickListener(v -> {
                String selected = holder.dropdownStatus.getText().toString().trim();

                if (selected.isEmpty() || selected.equalsIgnoreCase(status)) {
                    Toast.makeText(v.getContext(), "Please select a *new* status.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call the new listener with the whole group
                if (statusUpdateListener != null) {
                    statusUpdateListener.onUpdate(group, selected);
                }
            });
        }
    }

    @Override
    public int getItemCount() { return orders.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // Card fields
        TextView customerName, statusChip, payment,
                orderDate, deliveryDate,
                contactNumber, address;

        // Nested list
        RecyclerView internalRecyclerView;

        // Update controls
        LinearLayout updateControlsLayout;
        TextInputLayout textInputLayout;
        AutoCompleteTextView dropdownStatus;
        MaterialButton buttonApplyStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Card fields
            customerName = itemView.findViewById(R.id.textCustomerName);
            statusChip = itemView.findViewById(R.id.textStatus);
            payment = itemView.findViewById(R.id.textPayment);
            orderDate = itemView.findViewById(R.id.textOrderDate);
            deliveryDate = itemView.findViewById(R.id.textDeliveryDate);
            contactNumber = itemView.findViewById(R.id.textContactNumber);
            address = itemView.findViewById(R.id.textAddress);

            // Nested list
            internalRecyclerView = itemView.findViewById(R.id.recyclerInternalItems);

            // Update controls
            updateControlsLayout = itemView.findViewById(R.id.layoutUpdateControls);
            textInputLayout = itemView.findViewById(R.id.textInputStatus);
            dropdownStatus = itemView.findViewById(R.id.dropdownStatus);
            buttonApplyStatus = itemView.findViewById(R.id.buttonApplyStatus);
        }
    }
}