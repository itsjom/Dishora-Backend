package com.example.dishora.defaultUI.homeTab.search;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.search.adapter.SearchResultAdapter;
import com.example.dishora.defaultUI.homeTab.search.filter.FilterFragment;
import com.example.dishora.defaultUI.homeTab.search.model.SearchResultItem;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView recyclerView;
    private SearchResultAdapter adapter;
    private List<SearchResultItem> searchList;
    private String currentQuery = ""; // store current query

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString("currentQuery", "");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // EditText setup inside CardView
        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.setBackgroundColor(Color.TRANSPARENT);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.GRAY);

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recyclerViewSearchResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchList = new ArrayList<>();
        adapter = new SearchResultAdapter(requireContext(), searchList);
        recyclerView.setAdapter(adapter);

        // Handle passed query from HomeFragment
        if (getArguments() != null) {
            currentQuery = getArguments().getString("search_query", "");
            if (!currentQuery.isEmpty()) {
                searchEditText.setText(currentQuery);
                performSearch(currentQuery);
            }
        }

        // Handle "Enter" key in EditText
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    currentQuery = query;
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        // Filter button
        ImageButton filterButton = view.findViewById(R.id.btnFilter);
        filterButton.setOnClickListener(v -> {
            FilterFragment filterFragment = new FilterFragment();

            // Send current query
            Bundle args = new Bundle();
            args.putString("query", currentQuery);
            filterFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, filterFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Restore query if needed
        if (!currentQuery.isEmpty()) {
            searchEditText.setText(currentQuery);
            performSearch(currentQuery);
        }

        // Listen for filter results
        getParentFragmentManager().setFragmentResultListener("filter_result", this, (requestKey, bundle) -> {
            ArrayList<String> filters = bundle.getStringArrayList("selected_filters");
            // Apply filters to your search logic
        });

        return view;
    }

    private void performSearch(String query) {
        searchList.clear();
        // Example search logic
        if (query.toLowerCase().contains("rice")) {
            searchList.add(new SearchResultItem("Roonie Fried Rice", "Roonies Lutong Ulam", 4.6, "804", "15 min", "330 Kois", R.drawable.chicken_inasal));
            searchList.add(new SearchResultItem("Chicken Adobo", "Mama’s Kitchen", 4.8, "1.2k", "20 min", "250 Kois", R.drawable.chicken_inasal));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentQuery", currentQuery);
    }
}
