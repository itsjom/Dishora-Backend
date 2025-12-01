package com.example.dishora.vendorUI.menuTab.api.responseModel;

public class CategoryResponse {
    private long product_category_id;
    private String category_name;

    public long getProduct_category_id() {
        return product_category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    @Override
    public String toString() {
        return category_name; // important for Spinner display
    }
}
