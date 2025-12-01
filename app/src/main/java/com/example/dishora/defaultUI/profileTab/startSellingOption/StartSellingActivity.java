package com.example.dishora.defaultUI.profileTab.startSellingOption;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dishora.R;

public class StartSellingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_start_selling);

        // Toolbar
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnSearch = findViewById(R.id.btnSearch);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Start Selling");
        btnSearch.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new StartSellFragment())
                    .commit();
        }
    }
}