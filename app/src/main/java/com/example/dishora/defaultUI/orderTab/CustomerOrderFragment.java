package com.example.dishora.defaultUI.orderTab;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.CustomerCartActivity;
import com.example.dishora.defaultUI.orderTab.api.OrdersApi;
import com.example.dishora.defaultUI.orderTab.model.Order;
import com.example.dishora.defaultUI.orderTab.orderDetails.CustomerOrderDetailsActivity;
import com.example.dishora.defaultUI.orderTab.orderDetails.adapter.OrdersAdapter;
import com.example.dishora.network.ApiClient;
import com.example.dishora.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerOrderFragment extends Fragment {
    private RecyclerView rvOrders;
    private OrdersAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private SessionManager sessionManager;


    public CustomerOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_order, container, false);

        // Toolbar
        ImageView btnBack = view.findViewById(R.id.btnBack);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setVisibility(View.GONE);
        toolbarTitle.setText("Orders");

        // Back button
        btnBack.setOnClickListener(v -> {
            // Get the bottom nav from the Activity
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.customerBottomNavigationView);
            // Trigger selecting the Home tab
            bottomNav.setSelectedItemId(R.id.home);
        });

        rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrdersAdapter(requireContext(), orderList, this::onOrderClick);
        rvOrders.setAdapter(adapter);

        loadOrders(); // fetch from backend

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload orders when returning to this screen to see status changes (e.g., after cancelling)
        loadOrders();
    }

    private void loadOrders() {
        sessionManager = new SessionManager(requireContext());
        long userId = sessionManager.getUserId();

        OrdersApi api = ApiClient
                .getBackendClient(requireContext())
                .create(OrdersApi.class);

        api.getMyOrders().enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call,
                                   @NonNull Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList.clear();
                    orderList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (orderList.isEmpty()) {
                        Toast.makeText(requireContext(),"No orders found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(),"Failed to load orders (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onOrderClick(long orderId) {
        // navigate to CustomerOrderDetailsFragment
        CustomerOrderDetailsActivity.start(requireContext(), orderId);
    }
}