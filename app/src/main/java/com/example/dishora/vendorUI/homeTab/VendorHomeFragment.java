package com.example.dishora.vendorUI.homeTab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dishora.R;
import com.example.dishora.databinding.FragmentVendorHomeBinding;
import com.example.dishora.vendorUI.homeTab.profile.VendorProfileActivity;
import com.example.dishora.vendorUI.homeTab.schedule.PreOrderScheduleActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class VendorHomeFragment extends Fragment {

    private FragmentVendorHomeBinding binding;
    private TextView toolbarTitle;

    public VendorHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVendorHomeBinding.inflate(inflater, container, false);

        // Toolbar
        toolbarTitle = binding.getRoot().findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Welcome!!");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ----- ✅ Profile button from the custom toolbar -----
        ImageView profileButton = view.findViewById(R.id.ivProfile);
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), VendorProfileActivity.class);
                startActivity(intent);
            });
        }

        // ---- Active Orders ----
        binding.cardActiveOrders.tvTitle.setText("Active Orders");
        binding.cardActiveOrders.tvValue.setText("30");
        binding.cardActiveOrders.ivIcon.setImageResource(R.drawable.ic_users);

        // ---- Pending Orders ----
        binding.cardPendingOrders.tvTitle.setText("Pending Orders");
        binding.cardPendingOrders.tvValue.setText("15");
        binding.cardPendingOrders.ivIcon.setImageResource(R.drawable.ic_package);

        // ---- Rating ----
        binding.cardRating.tvTitle.setText("Rating");
        binding.cardRating.tvValue.setText("★★★★☆"); // Later replace with RatingBar if needed
        binding.cardRating.ivIcon.setImageResource(R.drawable.ic_chart);

        // ---- Monthly Orders ----
        binding.cardMonthlyOrders.tvTitle.setText("Monthly Orders");
        binding.cardMonthlyOrders.tvValue.setText("250");
        binding.cardMonthlyOrders.ivIcon.setImageResource(R.drawable.ic_record);

        if (binding.cardPreOrderSchedule != null) {
            // Set content for the new card using the included layout IDs
            binding.cardPreOrderSchedule.tvTitle.setText("Pre-Order Schedule");
            binding.cardPreOrderSchedule.tvValue.setText("Manage Dates"); // Action-oriented text for a navigation card
            // NOTE: You must have an icon named 'ic_calendar' or similar in your drawable resources
            binding.cardPreOrderSchedule.ivIcon.setImageResource(R.drawable.ic_calendar);

            // Set the click listener to navigate to the schedule management screen
            binding.cardPreOrderSchedule.getRoot().setOnClickListener(v -> {
                // Replace with your actual Activity/Fragment for managing the schedule
                Intent intent = new Intent(requireContext(), PreOrderScheduleActivity.class);
                startActivity(intent);
            });
        }

        // ---- Sales Track Chart ----
        binding.cardSalesTrack.tvChartTitle.setText("Sales Track");
        setupSalesTrackChart(binding.cardSalesTrack.lineChart);

        // ---- Revenue Chart ----
        binding.cardRevenue.tvChartTitle.setText("Revenue");
        setupRevenueChart(binding.cardRevenue.lineChart);
    }

    private void setupSalesTrackChart(LineChart chart) {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 5000));
        entries.add(new Entry(2, 10000));
        entries.add(new Entry(3, 15000));
        entries.add(new Entry(4, 20000));
        entries.add(new Entry(5, 18000));

        LineDataSet dataSet = new LineDataSet(entries, "Sales");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(80);
        dataSet.setFillColor(Color.parseColor("#80A8D8FF"));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private void setupRevenueChart(LineChart chart) {
        ArrayList<Entry> sales = new ArrayList<>();
        sales.add(new Entry(1, 20));
        sales.add(new Entry(2, 50));
        sales.add(new Entry(3, 30));
        sales.add(new Entry(4, 70));
        sales.add(new Entry(5, 90));

        ArrayList<Entry> profit = new ArrayList<>();
        profit.add(new Entry(1, 15));
        profit.add(new Entry(2, 30));
        profit.add(new Entry(3, 40));
        profit.add(new Entry(4, 60));
        profit.add(new Entry(5, 80));

        LineDataSet salesSet = new LineDataSet(sales, "Sales");
        salesSet.setColor(Color.RED);
        salesSet.setCircleColor(Color.RED);
        salesSet.setDrawFilled(true);
        salesSet.setFillColor(Color.parseColor("#40FF0000"));

        LineDataSet profitSet = new LineDataSet(profit, "Profit");
        profitSet.setColor(Color.MAGENTA);
        profitSet.setCircleColor(Color.MAGENTA);
        profitSet.setDrawFilled(true);
        profitSet.setFillColor(Color.parseColor("#409933CC"));

        LineData data = new LineData(salesSet, profitSet);
        chart.setData(data);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
