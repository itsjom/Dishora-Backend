package com.example.dishora;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    View view;
    RecyclerView recyclerView;
    ArrayList<OrderItem> itemList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<StoreOrderGroup> groupList = new ArrayList<>();

        List<OrderItem> vendorA = new ArrayList<>();
        vendorA.add(new OrderItem("Buttered Chicken","Zone 6 Maguikay Ave. Naga City", "₱100.00", R.drawable.buttered_chicken));
        vendorA.add(new OrderItem("Roast Chicken","Zone 6 Maguikay Ave. Naga City", "₱100.00", R.drawable.roast_chicken));

        List<OrderItem> vendorB = new ArrayList<>();
        vendorB.add(new OrderItem("Chicken Inasal","Zone 6 Maguikay Ave. Naga City", "₱100.00", R.drawable.chicken_inasal));

        groupList.add(new StoreOrderGroup("Vendor A", vendorA));
        groupList.add(new StoreOrderGroup("Vendor B", vendorB));


        recyclerView.setAdapter(new StoreGroupAdapter(getContext(), groupList));
        return view;
    }
}