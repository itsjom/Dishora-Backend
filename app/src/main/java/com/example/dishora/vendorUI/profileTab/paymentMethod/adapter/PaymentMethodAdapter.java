package com.example.dishora.vendorUI.profileTab.paymentMethod.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.vendorUI.profileTab.paymentMethod.model.PaymentMethod;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {
    private List<PaymentMethod> methods;
    private final OnPaymentMethodClickListener clickListener;

    public PaymentMethodAdapter(List<PaymentMethod> methods, OnPaymentMethodClickListener clickListener) {
        this.methods = methods;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentMethod method = methods.get(position);

        holder.tvName.setText(method.getName());
        holder.tvDescription.setText(method.getDescription());
        holder.switchEnabled.setChecked(method.isEnabled());

        // ✅ SHOW SAVED ACCOUNT NUMBER
        if (method.getAccountNumber() != null && !method.getAccountNumber().isEmpty()) {
            holder.tvAccountNumber.setText("Account: " + method.getAccountNumber());
            holder.tvAccountNumber.setVisibility(View.VISIBLE);
        } else {
            holder.tvAccountNumber.setVisibility(View.GONE);
        }

        holder.switchEnabled.setOnCheckedChangeListener((btn, checked) -> {
            method.setEnabled(checked); // update local flag
        });

        // ✅ SET CLICK LISTENER ON THE WHOLE ITEM
        holder.itemView.setOnClickListener(v -> {
            clickListener.onMethodClick(method);
        });
    }

    public interface OnPaymentMethodClickListener {
        void onMethodClick(PaymentMethod method);
    }

    @Override
    public int getItemCount() { return methods.size(); }

    public List<PaymentMethod> getMethods() { return methods; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvAccountNumber;
        SwitchMaterial switchEnabled;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMethodName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAccountNumber = itemView.findViewById(R.id.tvAccountNumber);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
        }
    }
}