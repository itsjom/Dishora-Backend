package com.example.dishora.defaultUI.vendorsTab.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dishora.R;
import com.example.dishora.defaultUI.vendorsTab.model.Vendor;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.VendorDetail;

import java.util.List;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.VendorViewHolder> {

    private List<Vendor> vendorList;
    private Context context;

    public VendorAdapter(List<Vendor> vendorList, Context context) {
        this.vendorList = vendorList;
        this.context = context;
    }

    @Override
    public VendorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vendor, parent, false);
        return new VendorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VendorViewHolder holder, int position) {
        Vendor vendor = vendorList.get(position);

        holder.vendorName.setText(vendor.getBusinessName());
        holder.vendorRating.setText("Rating: " + vendor.getRating());

        Glide.with(context)
                .load(vendor.getBusinessImage())
                .placeholder(R.drawable.ic_vendor_placeholder)
                .error(R.drawable.ic_vendor_placeholder)
                .centerCrop()
                .into(holder.vendorImage);

        holder.openButton.setOnClickListener(v -> {
            VendorDetail fragment = new VendorDetail();
            Bundle bundle = new Bundle();
            bundle.putString("name", vendor.getBusinessName());
            bundle.putString("rating", String.valueOf(vendor.getRating()));
            bundle.putString("imageUrl", vendor.getBusinessImage());
            bundle.putString("address", vendor.getBusinessAddress());
            bundle.putLong("vendorId", vendor.getVendorId());
            bundle.putLong("businessId", vendor.getBusinessId());
            bundle.putString("description", vendor.getBusinessDescription());
            bundle.putLong("vendorUserId", vendor.getUserId());

            fragment.setArguments(bundle);

            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    public static class VendorViewHolder extends RecyclerView.ViewHolder {
        TextView vendorName, vendorRating, openButton;
        ImageView vendorImage;

        public VendorViewHolder(View itemView) {
            super(itemView);
            vendorName = itemView.findViewById(R.id.txtVendorName);
            vendorRating = itemView.findViewById(R.id.txtRating);
            vendorImage = itemView.findViewById(R.id.imageVendor);
            openButton = itemView.findViewById(R.id.txtOpenButton);
        }
    }
}