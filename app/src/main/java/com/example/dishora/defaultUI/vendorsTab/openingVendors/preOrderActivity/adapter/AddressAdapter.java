package com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
// --- CRITICAL FIX: Ensure the adapter uses the correct UserAddress model ---
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.model.UserAddress;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final List<UserAddress> addressList;
    private final OnAddressClickListener selectListener;
    private final OnAddressClickListener editListener;

    // Define the click listener interface
    public interface OnAddressClickListener {
        void onClick(UserAddress address);
    }

    public AddressAdapter(List<UserAddress> addressList, OnAddressClickListener selectListener, OnAddressClickListener editListener) {
        this.addressList = addressList;
        this.selectListener = selectListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        UserAddress address = addressList.get(position);

        // Populate the views using the UserAddress getters
        holder.tvRecipientName.setText(address.getName());
        holder.tvPhoneNumber.setText(address.getPhone());
        holder.tvFullAddress.setText(address.getFullAddress());

        // Set up the click listener for selecting the address (tapping the whole card)
        holder.itemView.setOnClickListener(v -> {
            if (selectListener != null) {
                selectListener.onClick(address);
            }
        });

        // Set up the click listener for editing the address (tapping the icon)
        holder.btnEditAddress.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onClick(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    // ViewHolder class
    static class AddressViewHolder extends RecyclerView.ViewHolder {
        final TextView tvRecipientName;
        final TextView tvPhoneNumber;
        final TextView tvFullAddress;
        final ImageView btnEditAddress;

        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipientName = itemView.findViewById(R.id.tvRecipientName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvFullAddress = itemView.findViewById(R.id.tvFullAddress);
            btnEditAddress = itemView.findViewById(R.id.btnEditAddress);
        }
    }
}
