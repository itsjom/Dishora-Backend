package com.example.dishora.vendorUI.orderTab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar; // Import this
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.vendorUI.orderTab.adapter.VendorOrdersAdapter;
import com.example.dishora.vendorUI.orderTab.model.GroupedOrder;
import com.example.dishora.vendorUI.orderTab.model.OrderViewModel;

import java.util.List;

public class VendorsOrderListFragment extends Fragment {

    private static final String ARG_STATUS = "status";
    private String statusFilter;
    private VendorOrdersAdapter adapter;
    private OrderViewModel viewModel;

    private RecyclerView recyclerView;
    private TextView textNoOrdersMessage;
    private ProgressBar progressBar; // ✅ 1. Add this variable

    public VendorsOrderListFragment() { }

    public static VendorsOrderListFragment newInstance(String status) {
        VendorsOrderListFragment fragment = new VendorsOrderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            statusFilter = getArguments().getString(ARG_STATUS);
        }

        viewModel = new ViewModelProvider(requireParentFragment()).get(OrderViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vendors_order_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerOrders);
        textNoOrdersMessage = view.findViewById(R.id.textNoOrdersMessage);
        progressBar = view.findViewById(R.id.progressBar); // ✅ 2. Find the view

        setupRecyclerView();
        observeData();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VendorOrdersAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnStatusUpdateListener((group, newStatus) -> {
            viewModel.updateGroupStatus(group, newStatus, requireContext());
        });
    }

    private void observeData() {
        if (statusFilter == null) return;

        // ✅ 3. Show progress bar, hide others
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textNoOrdersMessage.setVisibility(View.GONE);

        Observer<List<GroupedOrder>> uiUpdater = orders -> {
            // ✅ 4. Hide progress bar once data arrives
            progressBar.setVisibility(View.GONE);

            if (orders == null || orders.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textNoOrdersMessage.setVisibility(View.VISIBLE);

                // ✅ 5. Add the text fix for the "All" tab
                if (statusFilter.equalsIgnoreCase("all")) {
                    textNoOrdersMessage.setText("No Orders");
                } else {
                    textNoOrdersMessage.setText("No " + statusFilter + " Orders");
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textNoOrdersMessage.setVisibility(View.GONE);
                adapter.setOrders(orders);
            }
        };

        switch (statusFilter.toLowerCase()) {
            case "all":
                viewModel.allOrders.observe(getViewLifecycleOwner(), uiUpdater);
                break;
            case "pending":
                viewModel.pendingOrders.observe(getViewLifecycleOwner(), uiUpdater);
                break;
            case "preparing":
                viewModel.preparingOrders.observe(getViewLifecycleOwner(), uiUpdater);
                break;
            case "for delivery":
                viewModel.forDeliveryOrders.observe(getViewLifecycleOwner(), uiUpdater);
                break;
            case "completed":
                viewModel.completedOrders.observe(getViewLifecycleOwner(), uiUpdater);
                break;
            case "cancelled":
                viewModel.cancelledOrders.observe(getViewLifecycleOwner(), uiUpdater);
                break;
        }
    }
}