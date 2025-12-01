package com.example.dishora.defaultUI.homeTab.cart.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.item.CartItem;
import com.example.dishora.defaultUI.homeTab.cart.model.CartModel;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.ShopCartDetailActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartModel> cartList;
    private final Context context;

    public CartAdapter(List<CartModel> cartList, Context context) {
        this.cartList = cartList;
        this.context = context;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartModel cart = cartList.get(position);

        Log.d("CartAdapter",
                "Shop: " + cart.getShopName() +
                        " | Address: " + cart.getShopAddress());

        holder.shopName.setText(cart.getShopName());
        holder.shopAddress.setText(cart.getShopAddress());

        // ✅ Load vendor logo from URL
        if (cart.getShopLogoUrl() != null && !cart.getShopLogoUrl().isEmpty()) {
            Glide.with(context)
                    .load(cart.getShopLogoUrl())
                    .placeholder(R.drawable.ic_vendor_placeholder)
                    .error(R.drawable.ic_vendor_placeholder)
                    .into(holder.shopLogo);
        } else {
            holder.shopLogo.setImageResource(R.drawable.ic_vendor_placeholder);
        }

        // ✅ Clear previous thumbnails
        holder.thumbnails.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(context);

        // ✅ Add thumbnails for each product
        for (CartItem item : cart.getItems()) {
            // Inflate your XML-defined thumbnail layout
            View thumbView = inflater.inflate(R.layout.item_cart_thumbnail, holder.thumbnails, false);

            ShapeableImageView productImg = thumbView.findViewById(R.id.imgProductThumb);
            TextView qtyBadge = thumbView.findViewById(R.id.txtQtyBadge);

            // Load product image
            Glide.with(context)
                    .load(item.getProduct().getImageUrl())
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .into(productImg);

            // Show quantity badge only if more than one
            int qty = item.getQuantity();
            if (qty > 1) {
                qtyBadge.setVisibility(View.VISIBLE);
                qtyBadge.setText("x" + qty);
            } else {
                qtyBadge.setVisibility(View.GONE);
            }

            // Add to parent layout
            holder.thumbnails.addView(thumbView);
        }

        // ➕ Add "Add more" button at end
        ImageButton addButton = new ImageButton(context);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(100, 100);
        btnParams.setMargins(8, 0, 8, 0);
        addButton.setLayoutParams(btnParams);
        addButton.setScaleType(ImageView.ScaleType.CENTER);
        addButton.setImageResource(R.drawable.positive_symbol);
        addButton.setBackgroundResource(R.drawable.circle_border_gray);
        holder.thumbnails.addView(addButton);

        // 🎯 Handle "View Cart" button click
        holder.btnViewCart.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShopCartDetailActivity.class);
            intent.putExtra("cartIndex", position); // identify which vendor's cart to show
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView shopName, shopAddress;
        ImageView shopLogo;
        LinearLayout thumbnails;
        Button btnViewCart;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.txtRestaurantName);
            shopAddress = itemView.findViewById(R.id.txtShopAddress);
            shopLogo = itemView.findViewById(R.id.imgRestaurantLogo);
            thumbnails = itemView.findViewById(R.id.layoutThumbnails);
            btnViewCart = itemView.findViewById(R.id.btnViewCart);
        }
    }
}