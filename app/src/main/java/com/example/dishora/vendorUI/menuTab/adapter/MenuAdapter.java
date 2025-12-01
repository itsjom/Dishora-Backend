package com.example.dishora.vendorUI.menuTab.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dishora.R;
import com.example.dishora.network.ApiClient;
import com.example.dishora.vendorUI.menuTab.productAddUpdate.ProductFormFragment;
import com.example.dishora.vendorUI.menuTab.api.ProductApiService;

import com.example.dishora.vendorUI.menuTab.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private final Context context;
    private List<Product> productList;
    private final ProductApiService api;

    public MenuAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
        this.api = ApiClient.getBackendClient().create(ProductApiService.class); // ✅ reuse API service
    }

    public void updateData(List<Product> newList) {
        this.productList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.txtName.setText(product.getItem_name());
        holder.txtPrice.setText(String.format(Locale.US, "₱%.2f", product.getPrice()));

        // Prevent triggering API on bind
        holder.switchAvailability.setOnCheckedChangeListener(null);
        holder.switchAvailability.setChecked(product.isAvailable());

        Glide.with(context)
                .load(product.getImage_url())
                .placeholder(R.drawable.ic_food_placeholder)
                .into(holder.imgDish);

        // ✅ Edit product (show dialog placeholder for now)
        holder.btnEdit.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Product prod = productList.get(pos);

            // Open the unified ProductFormFragment in edit mode
            FragmentActivity activity = (FragmentActivity) context;
            ProductFormFragment fragment = ProductFormFragment.newEditInstance(prod);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        // ✅ Delete with confirmation
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(context)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete " + product.getItem_name() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        api.deleteProduct(product.getProduct_id()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(context, "Removed " + product.getItem_name(), Toast.LENGTH_SHORT).show();
                                    productList.remove(pos);
                                    notifyItemRemoved(pos);
                                    notifyItemRangeChanged(pos, productList.size()); // ✅ keep list in sync
                                } else {
                                    Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // ✅ Handle switch toggle safely
        holder.switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            api.updateAvailability(product.getProduct_id(), isChecked).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context,
                                product.getItem_name() + " is now " + (isChecked ? "Available" : "Unavailable"),
                                Toast.LENGTH_SHORT).show();
                        product.setAvailable(isChecked); // ✅ update local model
                    } else {
                        rollbackSwitch(holder, product.isAvailable());
                        Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    rollbackSwitch(holder, product.isAvailable());
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void rollbackSwitch(MenuViewHolder holder, boolean oldState) {
        holder.switchAvailability.setOnCheckedChangeListener(null);
        holder.switchAvailability.setChecked(oldState);
        holder.switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // re-attach listener
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        SwitchCompat switchAvailability;
        ImageView imgDish;
        TextView txtName, txtPrice;
        Button btnEdit, btnRemove;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            switchAvailability = itemView.findViewById(R.id.switchAvailability);
            imgDish = itemView.findViewById(R.id.imgDish);
            txtName = itemView.findViewById(R.id.txtDishName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
