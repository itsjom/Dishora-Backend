package com.example.dishora.defaultUI.homeTab.vendorSection;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import com.example.dishora.R;

import java.util.List;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.VendorViewHolder> {

    private Context context;
    private List<Vendor> vendorList;

    public VendorAdapter(Context context, List<Vendor> vendorList) {
        this.context = context;
        this.vendorList = vendorList;
    }

    @NonNull
    @Override
    public VendorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_vendor.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_vendor, parent, false);
        return new VendorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorViewHolder holder, int position) {
        // Get the vendor for the current position
        Vendor vendor = vendorList.get(position);

        // Set the data
        holder.vendorNameTextView.setText(vendor.getName());

        // Use Glide to load the logo from a URL
        Glide.with(context)
                .load(vendor.getLogoUrl()) // Get the URL from your object
                .placeholder(R.drawable.ic_vendor_placeholder) // A default image
                .error(R.drawable.ic_vendor_placeholder) // Image if loading fails
                .into(holder.vendorLogoImageView);
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    // The ViewHolder class
    public static class VendorViewHolder extends RecyclerView.ViewHolder {
        ImageView vendorLogoImageView;
        TextView vendorNameTextView;

        public VendorViewHolder(@NonNull View itemView) {
            super(itemView);
            vendorLogoImageView = itemView.findViewById(R.id.vendorLogoImageView);
            vendorNameTextView = itemView.findViewById(R.id.vendorNameTextView);
        }
    }
}