package com.example.dishora.defaultUI.vendorsTab.openingVendors.adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dishora.R;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.cart.CartManager;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.model.Product;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.PreOrderCheckoutActivity;

import java.util.List;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private final FragmentActivity activity;
    private final List<Product> productList;
    private final String vendorName;
    private final String vendorAddress;
    private final String vendorLogoUrl;  // ✅ changed to String URL
    private final long businessId;   // ✅ new field

    public MenuAdapter(FragmentActivity activity,
                       List<Product> productList,
                       String vendorName,
                       String vendorAddress,
                       String vendorLogoUrl,
                       long businessId) {
        this.activity = activity;
        this.productList = productList;
        this.vendorName = vendorName;
        this.vendorAddress = vendorAddress;
        this.vendorLogoUrl = vendorLogoUrl;
        this.businessId = businessId;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.vendor_menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Product item = productList.get(position);
        holder.txtName.setText(item.getItemName());
        holder.txtPrice.setText(String.format(Locale.US, "₱%.2f", item.getPrice()));

        Glide.with(activity)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_product_placeholder)
                .error(R.drawable.ic_product_placeholder)
                .into(holder.imgFood);

        // --- ADD‑TO‑CART permission logic ---
        boolean available = item.isAvailable();
        boolean preOrder = item.isPreOrder();

        if (available && preOrder) {
            // Product is pre‑order + available → disable Add to Cart
            holder.addToCartBtn.setEnabled(false);
            holder.addToCartBtn.setAlpha(0.5f);
            holder.addToCartBtn.setTextColor(ContextCompat.getColor(activity, R.color.gray));
        } else if (!available) {
            holder.addToCartBtn.setEnabled(false);
            holder.addToCartBtn.setAlpha(0.5f);
            holder.addToCartBtn.setText("Unavailable");
            holder.addToCartBtn.setTextColor(ContextCompat.getColor(activity, R.color.gray));
        } else {
            holder.addToCartBtn.setEnabled(true);
            holder.addToCartBtn.setAlpha(1f);
            holder.addToCartBtn.setText("Add to Cart");
            holder.addToCartBtn.setTextColor(ContextCompat.getColor(activity, R.color.black));
        }


        if (preOrder) {
            // Product IS available for pre-order, ensure button is enabled
            holder.preOrderBtn.setEnabled(true);
            holder.preOrderBtn.setAlpha(1f);
            holder.preOrderBtn.setOnClickListener(v -> showPreOrderDialog(position));
        } else {
            // Product is NOT available for pre-order, disable button
            holder.preOrderBtn.setEnabled(false);
            holder.preOrderBtn.setAlpha(0.5f);
            holder.preOrderBtn.setOnClickListener(null); // Remove click listener
        }

        holder.addToCartBtn.setOnClickListener(v -> {
            if (!holder.addToCartBtn.isEnabled()) return;

            Log.d("MenuAdapter",
                    "AddToCart → vendorName=" + vendorName +
                            " | vendorAddress=" + vendorAddress +
                            " | businessId=" + businessId);

            CartManager.getInstance().addToCart(vendorName, vendorAddress, vendorLogoUrl, businessId, item);
            Toast.makeText(activity, item.getItemName() + " added to cart", Toast.LENGTH_SHORT).show();
        });
    }

    private void showPreOrderDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_preorder, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            showCustomizeDialog(position);
        });

        btnNo.setOnClickListener(v -> {
            // Intent intent = new Intent(activity, CheckoutActivity.class);
            // intent.putExtra("productId", productList.get(position).getProductId());
            // activity.startActivity(intent);
            dialog.dismiss();
        });
    }

    private void showCustomizeDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_customize_order, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Product item = productList.get(position);
        TextView tvFoodName = dialogView.findViewById(R.id.tvFoodName);
        ImageView imgFood = dialogView.findViewById(R.id.imgFood);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnApply = dialogView.findViewById(R.id.btnApply);

        tvFoodName.setText(item.getItemName());

        Glide.with(activity)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_food_placeholder)
                .into(imgFood);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnApply.setOnClickListener(v -> {
            String note = etNote.getText().toString();
            dialog.dismiss();
            openPreOrderActivity(position, note);
        });
    }

    private void openPreOrderActivity(int position, String note) {
        Product item = productList.get(position);
        Intent intent = new Intent(activity, PreOrderCheckoutActivity.class);
        intent.putExtra("product", item);
        intent.putExtra("note", note);
        intent.putExtra("mode", "preorder");
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView txtName, txtPrice;
        Button preOrderBtn, addToCartBtn;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.itemImage);
            txtName = itemView.findViewById(R.id.itemName);
            txtPrice = itemView.findViewById(R.id.itemPrice);
            preOrderBtn = itemView.findViewById(R.id.preOrderBtn);
            addToCartBtn = itemView.findViewById(R.id.addToCartBtn);
        }
    }
}