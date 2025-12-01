package com.example.dishora.models;

public class SettingItem {
    private String label;
    private int iconResId;

    public SettingItem(String label, int iconResId) {
        this.label = label;
        this.iconResId = iconResId;
    }

    public String getLabel() {
        return label;
    }

    public int getIconResId() {
        return iconResId;
    }
}
