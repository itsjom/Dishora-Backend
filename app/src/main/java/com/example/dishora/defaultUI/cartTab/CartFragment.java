package com.example.dishora.defaultUI.cartTab;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dishora.R;
import com.example.dishora.defaultUI.cartTab.orderTab.adapter.OrderItemAdapter;
import com.example.dishora.defaultUI.cartTab.orderTab.adapter.StoreAdapter;
import com.example.dishora.defaultUI.cartTab.orderTab.checkOutPage.CheckoutFragment;
import com.example.dishora.defaultUI.cartTab.orderTab.model.OrderItem;
import com.example.dishora.defaultUI.cartTab.orderTab.model.StoreOrder;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements OrderItemAdapter.OnViewOrderClickListener {

    RecyclerView recyclerView;
//    private List<OrderItem> orderItemList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);

        // --- build storeOrders from your cart source (example dummy data) ---
        List<OrderItem> items1 = new ArrayList<>();
        items1.add(new OrderItem("Buttered Chicken", 1, 150.0, R.drawable.buttered_chicken));

        List<OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderItem("Roasted Chicken", 2, 180.0, R.drawable.roast_chicken));
        items2.add(new OrderItem("Inasal", 1, 120.0, R.drawable.chicken_inasal));

        List<StoreOrder> storeOrders = new ArrayList<>();
        storeOrders.add(new StoreOrder("Roonies Lutong Ulam",
                "Immaculate Heart of Mary Village", R.drawable.ronnies_profile_icon, items1));
        storeOrders.add(new StoreOrder("Zaffy's Kitchen",
                "Somewhere City", R.drawable.zaffys_profile_icon, items2));
        // --------------------------------------------------------------------

        StoreAdapter adapter = new StoreAdapter(requireContext(), storeOrders, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewOrderClick(OrderItem item) {
        // This method will be called when the "View" button in the OrderItem is clicked

        // Create an instance of CheckoutFragment
        CheckoutFragment checkoutFragment = new CheckoutFragment();

        // You can pass data to the CheckoutFragment if needed
        // For example, if you want to pass the item clicked:
        Bundle bundle = new Bundle();
        bundle.putString("item_name", item.getItemName());
        bundle.putDouble("item_price", item.getPrice());
        checkoutFragment.setArguments(bundle);

        // Replace the current fragment with CheckoutFragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.cartCheckoutFragment, checkoutFragment);  // Replace with the actual container ID
        transaction.addToBackStack(null);  // Optional: Adds the transaction to the back stack
        transaction.commit();  // Commit the transaction
    }
}
