package com.example.dishora.vendorUI.orderTab.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dishora.R;
import com.example.dishora.vendorUI.orderTab.model.OrderItem;
import java.util.List;

/**
 * This is the simple adapter for the NESTED RecyclerView,
 * which only shows the products inside a customer card.
 */
public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ProductViewHolder> {

    private final List<OrderItem> items;

    public OrderProductAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.productName.setText(item.getProductName());
        holder.productQuantity.setText(item.getQuantity() + "x");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textProductName);
            productQuantity = itemView.findViewById(R.id.textProductQuantity);
        }
    }
}