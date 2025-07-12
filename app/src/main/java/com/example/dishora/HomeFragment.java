package com.example.dishora;

import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;


public class HomeFragment extends Fragment {

    private SearchView searchV;

    private LinearLayout cLayout, buttonContainer, btnContainer;
    private CircleIndicator3 indicator3;
    private ViewPager2 viewPager;
    private ImageAdapter imgAdapter;
    private List<String> imageList;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int currentPage = 0;

    //    Category List
    private String[] categories = {"All", "Gluten-free", "Vegetarian", "Vegan", "Buffet", "Drinks", "Desserts", "Specials"};
    private TextView selectedButton = null;

    //    Featured List
    private  String[] featureCtgry = {"All", "Platter", "Combo", "Family", "Solo"};
    private TextView selectedBtn = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        searchV = view.findViewById(R.id.searchView);
        searchV.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (searchV.isIconified()) {
                    searchV.setIconified(false);
                }
            }
            return false;
        });
        int closeBtnId = androidx.appcompat.R.id.search_close_btn;
        ImageView closeBtn = searchV.findViewById(closeBtnId);

        if (closeBtn != null) {
            // remove any background/ripple drawable
            closeBtn.setBackground(null);
            // on API21+ clear the foreground ripple (if set)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                closeBtn.setForeground(null);
            }
            // optional: make sure it's still clickable
            closeBtn.setClickable(true);
        }

        viewPager = view.findViewById(R.id.viewPager1);
        indicator3 = view.findViewById(R.id.cardIndicator);

        // Bind the indicator to the ViewPager2
        indicator3.setViewPager(viewPager);

        buttonContainer = view.findViewById(R.id.categoryBtnContainer);
        btnContainer = view.findViewById(R.id.featuredBtnContainer);

        setupCarousel();
        setupCategoryButtons();
        setupFeatureButtons();

        return view;
    }

    private void setupCarousel() {
        imageList = new ArrayList<>();
        imageList.add("https://static.wikia.nocookie.net/naruto/images/d/de/Boruto_Infobox.png/revision/latest/scale-to-width-down/1200?cb=20220130144217");
        imageList.add("https://i.pinimg.com/736x/77/1a/bc/771abcafead609892a05be89a38122b2.jpg");
        imageList.add("https://w0.peakpx.com/wallpaper/289/711/HD-wallpaper-boruto-naruto-next-generation-anime-naruto-shippuden-boruto-thumbnail.jpg");

        imgAdapter = new ImageAdapter(requireContext(), imageList);
        viewPager.setAdapter(imgAdapter);               // Functions for the Carousel
        indicator3.setViewPager(viewPager);             // Bind the indicator to the ViewPager2
        autoScroll();
    }

    private void setupCategoryButtons() {
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            TextView button = createStyledButton(category);

            // Select first category by default
            if (i == 0) {
                selectCategory(button, category);
            }

            button.setOnClickListener(v -> selectCategory(button, category));
            buttonContainer.addView(button);
        }
    }

    private void setupFeatureButtons() {
        for (int i = 0; i < featureCtgry.length; i++) {
            String category = featureCtgry[i];
            TextView button = createStyledButton(category);

            if (i == 0) selectFeatureCategory(button, category);

            button.setOnClickListener(v -> selectFeatureCategory(button,category));
            btnContainer.addView(button);
        }
    }

    private TextView createStyledButton(String text) {
        TextView button = new TextView(requireContext());
        button.setText(text);
        button.setTextSize(14);
        button.setPadding(40, 20, 40, 20);
        button.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_button));
        button.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 12, 12, 12);
        button.setLayoutParams(params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setElevation(6f);
            button.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 60f);
                }
            });
            button.setClipToOutline(true);
        }

        return button;
    }

    private void selectCategory(TextView newSelected, String category) {
        if (selectedButton != null) {
            selectedButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_button));
        }

        newSelected.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.selected_category_background));
        selectedButton = newSelected;

        // Replace fragment based on category
        CategoryFrag fragment = CategoryFrag.newInstance(category);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.categoryFrgmentContainer, fragment)
                .commit();
    }

    private void selectFeatureCategory(TextView newSelected, String category) {
        if (selectedBtn != null) {
            selectedBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_button));
        }

        newSelected.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.selected_category_background));
        selectedBtn = newSelected;

        // Replace fragment based on category
        FeatureCategoryFrag fragment = FeatureCategoryFrag.newInstance(category);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.featuredFrgmentContainer, fragment)
                .commit();
    }

    private void autoScroll() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage >= imageList.size()) currentPage = 0;
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}