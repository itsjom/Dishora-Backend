package com.example.dishora.defaultUI.homeTab.cart.shopCart.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.item.CartItem;
import java.util.List;

public class ShopItemsAdapter extends RecyclerView.Adapter<ShopItemsAdapter.ItemViewHolder> {

    private final List<CartItem> items;
    private final Context context;
    private final OnCartUpdateListener updateListener; // ✅ Renamed for clarity

    // ✅ ENHANCED INTERFACE
    public interface OnCartUpdateListener {
        void onCartUpdated();    // For quantity changes, recalculating totals
        void onCartEmpty();      // Specific callback for when the last item is removed
    }

    public ShopItemsAdapter(List<CartItem> items, Context context, OnCartUpdateListener listener) {
        this.items = items;
        this.context = context;
        this.updateListener = listener; // ✅ Updated constructor
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_shop_cart_product, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder h, int pos) {
        CartItem ci = items.get(pos);

        Glide.with(context)
                .load(ci.getProduct().getImageUrl())
                .placeholder(R.drawable.ic_food_placeholder)
                .into(h.img);

        h.name.setText(ci.getProduct().getItemName());
        h.price.setText(String.format("₱%.2f", ci.getProduct().getPrice()));
        h.qty.setText(String.valueOf(ci.getQuantity()));

        // --- Quantity: + / - ---
        h.plus.setOnClickListener(v -> {
            ci.setQuantity(ci.getQuantity() + 1);
            notifyItemChanged(h.getAdapterPosition());
            if (updateListener != null) updateListener.onCartUpdated(); // ✅ Call updated listener
        });

        h.minus.setOnClickListener(v -> {
            if (ci.getQuantity() > 1) {
                ci.setQuantity(ci.getQuantity() - 1);
                notifyItemChanged(h.getAdapterPosition());
                if (updateListener != null) updateListener.onCartUpdated(); // ✅ Call updated listener
            }
        });

        // --- 🗑 Remove button with confirmation ---
        h.removeBtn.setOnClickListener(v -> {
            int position = h.getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(context)
                    .setTitle("Remove item")
                    .setMessage("Are you sure you want to remove this item from your cart?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        // Remove the item from our list
                        items.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, items.size());

                        // ✅ --- THIS IS THE KEY LOGIC ---
                        if (updateListener != null) {
                            if (items.isEmpty()) {
                                // If the list is now empty, call the new onCartEmpty() method
                                updateListener.onCartEmpty();
                            } else {
                                // Otherwise, just signal a normal update
                                updateListener.onCartUpdated();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, price, qty;
        ImageButton plus, minus, removeBtn;

        ItemViewHolder(@NonNull View item) {
            super(item);
            img = item.findViewById(R.id.imgFood);
            name = item.findViewById(R.id.tvName);
            price = item.findViewById(R.id.tvPrice);
            qty = item.findViewById(R.id.tvQty);
            plus = item.findViewById(R.id.btnPlus);
            minus = item.findViewById(R.id.btnMinus);
            removeBtn = item.findViewById(R.id.btnRemove);
        }
    }
}