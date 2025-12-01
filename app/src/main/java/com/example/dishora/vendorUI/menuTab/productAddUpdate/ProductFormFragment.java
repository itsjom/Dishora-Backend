package com.example.dishora.vendorUI.menuTab.productAddUpdate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.dishora.R;
import com.example.dishora.network.ApiClient;
import com.example.dishora.vendorUI.menuTab.VendorMenuFragment;
import com.example.dishora.vendorUI.menuTab.api.CategoryApiService;
import com.example.dishora.vendorUI.menuTab.api.DietarySpecApiService;
import com.example.dishora.vendorUI.menuTab.api.ProductApiService;
import com.example.dishora.vendorUI.menuTab.api.responseModel.CategoryResponse;
import com.example.dishora.vendorUI.menuTab.api.responseModel.DietarySpecResponse;
import com.example.dishora.vendorUI.menuTab.api.responseModel.UploadResponse;
import com.example.dishora.vendorUI.menuTab.model.Product;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class ProductFormFragment extends Fragment {

    private static final String ARG_MODE = "form_mode";
    private static final String ARG_PRODUCT = "product";
    private boolean isEditMode = false;
    private Product existingProduct;

    private ImageView imgUpload;
    private LinearLayout overlayUpload;
    private EditText edtName, edtPrice, edtDescription, edtTimeCutOff, edtAdvancePrice;
    private AutoCompleteTextView autoPreOrder, autoCategory, autoDietary;
    private Button btnSubmit, btnDiscard;
    private Uri imageUri;

    // --- NEW: Views for highlighting ---
    private MaterialCardView cardUpload;
    private TextInputLayout layoutName, layoutPrice, layoutDescription, layoutPreOrder, layoutCategory, layoutAdvancePrice, layoutTimeCutOff, layoutDietary;
    private ColorStateList defaultStrokeColor; // To store the default border color

    private List<CategoryResponse> categoryList = new ArrayList<>();
    private List<DietarySpecResponse> dietaryList = new ArrayList<>();
    private boolean[] selectedSpecs;
    private final List<Long> selectedSpecIds = new ArrayList<>();

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        overlayUpload.setVisibility(View.GONE);
                        imgUpload.setImageURI(imageUri);
                        // Reset highlight if user selects an image
                        if (cardUpload != null) {
                            cardUpload.setStrokeColor(defaultStrokeColor);
                        }
                    }
                }
            });

    public static ProductFormFragment newAddInstance() {
        ProductFormFragment f = new ProductFormFragment();
        Bundle b = new Bundle();
        b.putString(ARG_MODE, "add");
        f.setArguments(b);
        return f;
    }

    public static ProductFormFragment newEditInstance(Product product) {
        ProductFormFragment f = new ProductFormFragment();
        Bundle b = new Bundle();
        b.putString(ARG_MODE, "edit");
        b.putSerializable(ARG_PRODUCT, product);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isEditMode = "edit".equalsIgnoreCase(getArguments().getString(ARG_MODE));
            if (isEditMode)
                existingProduct = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_product_form, container, false);

        ImageView btnBack = v.findViewById(R.id.btnBack);
        TextView title = v.findViewById(R.id.toolbarTitle);
        title.setText(isEditMode ? "Edit Item" : "Add Item");
        btnBack.setOnClickListener(view -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, new VendorMenuFragment())
                .commit());

        // Bind standard views
        imgUpload = v.findViewById(R.id.imgUpload);
        overlayUpload = v.findViewById(R.id.overlayUpload);
        edtName = v.findViewById(R.id.edtName);
        edtPrice = v.findViewById(R.id.edtPrice);
        edtAdvancePrice = v.findViewById(R.id.edtAdvancePrice);
        edtDescription = v.findViewById(R.id.edtDescription);
        edtTimeCutOff = v.findViewById(R.id.edtTimeCutOff);
        autoPreOrder = v.findViewById(R.id.autoPreOrder);
        autoCategory = v.findViewById(R.id.autoCategory);
        autoDietary = v.findViewById(R.id.autoDietary);
        btnSubmit = v.findViewById(R.id.btnAdd);
        btnDiscard = v.findViewById(R.id.btnDiscard);

        // --- NEW: Bind views for highlighting ---
        cardUpload = v.findViewById(R.id.cardUpload);
        layoutName = v.findViewById(R.id.layoutName);
        layoutPrice = v.findViewById(R.id.layoutPrice);
        layoutAdvancePrice = v.findViewById(R.id.layoutAdvancePrice);
        layoutDescription = v.findViewById(R.id.layoutDescription);
        layoutPreOrder = v.findViewById(R.id.layoutPreOrder);
        layoutCategory = v.findViewById(R.id.layoutCategory);
        layoutTimeCutOff = v.findViewById(R.id.layoutTimeCutOff);
        layoutDietary = v.findViewById(R.id.layoutDietary);

        // Store the default color to reset it later
        defaultStrokeColor = cardUpload.getStrokeColorStateList();

        btnSubmit.setText(isEditMode ? "Update" : "Add Item");

        // Make both the image & card clickable
        imgUpload.setOnClickListener(x -> openImagePicker());
        overlayUpload.setOnClickListener(x -> openImagePicker());

        btnSubmit.setOnClickListener(x -> handleSubmit());
        btnDiscard.setOnClickListener(x -> requireActivity().onBackPressed());

        edtPrice.setOnFocusChangeListener((v1, hasFocus) -> {
            if (!hasFocus) {
                String text = edtPrice.getText() != null ? edtPrice.getText().toString().trim() : "";
                if (!text.isEmpty()) {
                    try {
                        double val = Double.parseDouble(text);
                        edtPrice.setText(String.format(Locale.US, "%.2f", val));
                    } catch (NumberFormatException e) {
                        edtPrice.setText("");
                    }
                }
            }
        });

        edtAdvancePrice.setOnFocusChangeListener((v1, hasFocus) -> {
            if (!hasFocus) {
                String text = edtAdvancePrice.getText() != null ? edtAdvancePrice.getText().toString().trim() : "";
                if (!text.isEmpty()) {
                    try {
                        double val = Double.parseDouble(text);
                        edtAdvancePrice.setText(String.format(Locale.US, "%.2f", val));
                    } catch (NumberFormatException e) {
                        edtAdvancePrice.setText("");
                    }
                }
            }
        });

        setupPreOrderDropdown();
        setupTimePicker();
        loadCategories();
        loadDietarySpecs();

        if (isEditMode && existingProduct != null) preloadFields(existingProduct);

        return v;
    }

    private void preloadFields(Product p) {
        edtName.setText(p.getItem_name());
        edtPrice.setText(String.format(Locale.US, "%.2f", p.getPrice()));

        // ADDED: TODO for preloading advance price
        // TODO: Preload advance price if it exists in your Product model
        // e.g., if (p.getAdvancePrice() > 0) {
        //    edtAdvancePrice.setText(String.format(Locale.US, "%.2f", p.getAdvancePrice()));
        // }

        edtDescription.setText(p.getDescription());
        autoPreOrder.setText(p.isPreorder() ? "Yes" : "No", false);
        Glide.with(requireContext())
                .load(p.getImage_url())
                .placeholder(R.drawable.ic_food_placeholder)
                .into(imgUpload);

        if (p.getImage_url() != null && !p.getImage_url().isEmpty()) {
            overlayUpload.setVisibility(View.GONE);
        }
    }

    private void setupPreOrderDropdown() {
        String[] options = {"Yes", "No"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, options);
        autoPreOrder.setAdapter(adapter);
    }

    private void setupTimePicker() {
        edtTimeCutOff.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int h = c.get(Calendar.HOUR_OF_DAY), m = c.get(Calendar.MINUTE);
            new TimePickerDialog(requireContext(),
                    (picker, hh, mm) ->
                            edtTimeCutOff.setText(String.format(Locale.US, "%02d:%02d", hh, mm)),
                    h, m, true).show();
        });
    }

    private void openImagePicker() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        pickImageLauncher.launch(i);
    }

    private void loadCategories() {
        CategoryApiService api = ApiClient.getBackendClient().create(CategoryApiService.class);
        api.getCategories().enqueue(new retrofit2.Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    categoryList = res.body();
                    List<String> names = new ArrayList<>();
                    for (CategoryResponse c : categoryList)
                        names.add(c.getCategory_name());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    autoCategory.setAdapter(adapter);

                    if (isEditMode && existingProduct != null) {
                        for (int i = 0; i < categoryList.size(); i++) {
                            if (categoryList.get(i).getProduct_category_id() ==
                                    existingProduct.getProduct_category_id()) {
                                autoCategory.setText(names.get(i), false);
                                break;
                            }
                        }
                    }
                }
            }

            @Override public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDietarySpecs() {
        DietarySpecApiService api = ApiClient.getBackendClient().create(DietarySpecApiService.class);
        api.getDietarySpecs().enqueue(new retrofit2.Callback<List<DietarySpecResponse>>() {
            @Override
            public void onResponse(Call<List<DietarySpecResponse>> call, Response<List<DietarySpecResponse>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    dietaryList = res.body();
                    selectedSpecs = new boolean[dietaryList.size()];
                    setupDietaryDropdown();
                }
            }

            @Override public void onFailure(Call<List<DietarySpecResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDietaryDropdown() {
        List<String> names = new ArrayList<>();
        for (DietarySpecResponse d : dietaryList)
            names.add(d.getDietary_spec_name());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, names);
        autoDietary.setAdapter(adapter);
        autoDietary.setOnClickListener(v -> showDietaryDialog());
    }

    private void showDietaryDialog() {
        if (dietaryList.isEmpty()) return;
        String[] names = new String[dietaryList.size()];
        for (int i = 0; i < dietaryList.size(); i++)
            names[i] = dietaryList.get(i).getDietary_spec_name();
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Dietary Specifications")
                .setMultiChoiceItems(names, selectedSpecs, (d, idx, checked) -> {
                    long id = dietaryList.get(idx).getDietary_specification_id();
                    if (checked) selectedSpecIds.add(id);
                    else selectedSpecIds.remove(id);
                })
                .setPositiveButton("OK", (dialog, which) -> autoDietary.setText(
                        selectedSpecIds.isEmpty() ? "" : buildSelectedDietaryNames(names)))
                .show();
    }

    private String buildSelectedDietaryNames(String[] allNames) {
        List<String> chosen = new ArrayList<>();
        for (int i = 0; i < allNames.length; i++) {
            if (selectedSpecs[i]) chosen.add(allNames[i]);
        }
        return String.join(", ", chosen);
    }

    /**
     * NEW: Validates all user input fields and highlights them if invalid.
     * @return true if all fields are valid, false otherwise.
     */
    private boolean validateInput() {
        boolean isValid = true;

        // --- 1. Reset all previous error highlights ---
        layoutName.setError(null);
        layoutPrice.setError(null);
        layoutAdvancePrice.setError(null);
        layoutDescription.setError(null);
        layoutCategory.setError(null);
        layoutPreOrder.setError(null);
        layoutTimeCutOff.setError(null);
        layoutDietary.setError(null);
        cardUpload.setStrokeColor(defaultStrokeColor); // Reset image border

        // --- 2. Perform validation and highlight if invalid ---
        if (edtName.getText().toString().trim().isEmpty()) {
            layoutName.setError(" "); // Set error with a space to activate highlight without text
            isValid = false;
        }

        String price = edtPrice.getText().toString().trim();
        double parsedTotalPrice = 0.0;
        if (price.isEmpty()) {
            layoutPrice.setError(" ");
            isValid = false;
        } else {
            try {
                parsedTotalPrice = Double.parseDouble(price);
            } catch (NumberFormatException e) {
                layoutPrice.setError(" "); // Invalid format
                isValid = false;
            }
        }

        String advancePrice = edtAdvancePrice.getText().toString().trim();
        if (!advancePrice.isEmpty()) { // Only validate if not empty
            try {
                double parsedAdvancePrice = Double.parseDouble(advancePrice);

                // Rule 1: Can't have advance price if item price is invalid or 0
                if (parsedTotalPrice <= 0) {
                    // *** THIS IS THE FIX ***
                    layoutAdvancePrice.setError("Set item price first");
                    isValid = false;
                }
                // Rule 2: Advance price cannot exceed item price
                else if (parsedAdvancePrice > parsedTotalPrice) {
                    // *** THIS IS THE FIX ***
                    layoutAdvancePrice.setError("Cannot exceed item price");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutAdvancePrice.setError(" "); // Invalid format
                isValid = false;
            }
        }

        if (edtDescription.getText().toString().trim().isEmpty()) {
            layoutDescription.setError(" ");
            isValid = false;
        }

        if (autoCategory.getText().toString().trim().isEmpty()) {
            layoutCategory.setError(" ");
            isValid = false;
        }

        if (autoPreOrder.getText().toString().trim().isEmpty()) {
            layoutPreOrder.setError(" ");
            isValid = false;
        }

        if (edtTimeCutOff.getText().toString().trim().isEmpty()) {
            layoutTimeCutOff.setError(" ");
            isValid = false;
        }

        if (selectedSpecIds.isEmpty()) {
            layoutDietary.setError(" ");
            isValid = false;
        }

        // Validate Image (only for new products)
        if (!isEditMode && imageUri == null) {
            cardUpload.setStrokeColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void handleSubmit() {
        if (validateInput()) {
            processUpload(isEditMode);
        }
    }

    private void processUpload(boolean updating) {
        String name = edtName.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String advancePrice = edtAdvancePrice.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String preOrderVal = autoPreOrder.getText().toString().trim();
        String categoryVal = autoCategory.getText().toString().trim();
        String cutOffTxt = edtTimeCutOff.getText().toString().trim();

        double parsedPrice = Double.parseDouble(price);

        double parsedAdvancePrice = 0.0;
        if (!advancePrice.isEmpty()) {
            try {
                parsedAdvancePrice = Double.parseDouble(advancePrice);
            } catch (NumberFormatException ignored) {}
        }

        Integer cutoffMinutes = null;
        if (!cutOffTxt.isEmpty()) {
            try {
                String[] p = cutOffTxt.split(":");
                cutoffMinutes = Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
            } catch (Exception ignored) {}
        }

        long businessId = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                .getLong("business_id", -1);
        long catId = getCategoryIdFromName(categoryVal);

        try {
            MultipartBody.Part imagePart = null;
            if (imageUri != null) {
                String mime = requireContext().getContentResolver().getType(imageUri);
                if (mime == null) mime = "image/jpeg";
                File f = new File(requireContext().getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
                try (InputStream in = requireContext().getContentResolver().openInputStream(imageUri);
                     FileOutputStream out = new FileOutputStream(f)) {
                    byte[] buf = new byte[1024]; int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                }
                imagePart = MultipartBody.Part.createFormData("image", f.getName(), RequestBody.create(MediaType.parse(mime), f));
            }

            RequestBody itemName = RequestBody.create(name, MultipartBody.FORM);
            RequestBody priceBody = RequestBody.create(String.format(Locale.US, "%.2f", parsedPrice), MultipartBody.FORM);
            RequestBody advancePriceBody = RequestBody.create(String.format(Locale.US, "%.2f", parsedAdvancePrice), MultipartBody.FORM);
            RequestBody descBody = RequestBody.create(desc, MultipartBody.FORM);
            RequestBody catBody = RequestBody.create(String.valueOf(catId), MultipartBody.FORM);
            RequestBody preOrderBody = RequestBody.create(preOrderVal.equals("Yes") ? "true" : "false", MultipartBody.FORM);
            RequestBody availBody = RequestBody.create("true", MultipartBody.FORM);
            RequestBody cutOffBody = RequestBody.create(cutoffMinutes == null ? "" : String.valueOf(cutoffMinutes), MultipartBody.FORM);

            List<MultipartBody.Part> dietaryParts = new ArrayList<>();
            for (Long id : selectedSpecIds)
                dietaryParts.add(MultipartBody.Part.createFormData("dietary_specification_ids", String.valueOf(id)));

            ProductApiService api = ApiClient.getBackendClient().create(ProductApiService.class);
            Call<UploadResponse> call;

            if (!updating) {
                RequestBody businessBody = RequestBody.create(String.valueOf(businessId), MultipartBody.FORM);
                // ADDED: TODO to remind you to update your API interface
                // TODO: Add 'advancePriceBody' to your 'uploadProduct' API call signature
                // e.g., call = api.uploadProduct(imagePart, itemName, priceBody, advancePriceBody, ...);
                call = api.uploadProduct(imagePart, itemName, priceBody, advancePriceBody, businessBody, catBody,
                        preOrderBody, availBody, descBody, cutOffBody, dietaryParts);
            } else {
                // ADDED: TODO to remind you to update your API interface
                // TODO: Add 'advancePriceBody' to your 'updateProductWithImage' API call signature
                // e.g., call = api.updateProductWithImage(existingProduct.getProduct_id(), imagePart, itemName, priceBody, advancePriceBody, ...);
                call = api.updateProductWithImage(
                        existingProduct.getProduct_id(),
                        imagePart, // Will be null if no new image is selected, which is correct
                        itemName, priceBody, advancePriceBody, catBody, preOrderBody,
                        availBody, descBody, cutOffBody, dietaryParts);
            }

            call.enqueue(new retrofit2.Callback<UploadResponse>() {
                @Override public void onResponse(Call<UploadResponse> call, Response<UploadResponse> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        Toast.makeText(requireContext(),
                                updating ? "Product updated!" : "Product added!",
                                Toast.LENGTH_SHORT).show();
                        // Go back to the menu list
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main_fragment_container, new VendorMenuFragment())
                                .commit();
                    } else {
                        Toast.makeText(requireContext(), "Failed: " + res.message(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private long getCategoryIdFromName(String name) {
        for (CategoryResponse c : categoryList)
            if (c.getCategory_name().equals(name))
                return c.getProduct_category_id();
        return -1;
    }
}