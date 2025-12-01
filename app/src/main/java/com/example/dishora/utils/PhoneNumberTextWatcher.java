package com.example.dishora.utils;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

public class PhoneNumberTextWatcher implements TextWatcher {
    private boolean isFormatting;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (isFormatting) return;
        isFormatting = true;

        // Remove anything that isn't a digit
        String digits = s.toString().replaceAll("\\D", "");
        StringBuilder formatted = new StringBuilder();

        // Format as 0912-345-6789 (PH style)
        if (digits.length() >= 4) {
            formatted.append(digits.substring(0, 4));
            if (digits.length() >= 7) {
                formatted.append("-").append(digits.substring(4, 7));
                if (digits.length() > 7) {
                    formatted.append("-").append(digits.substring(7, Math.min(digits.length(), 11)));
                }
            } else {
                formatted.append("-").append(digits.substring(4));
            }
        } else {
            formatted.append(digits);
        }

        // Apply formatted string back
        s.replace(0, s.length(), formatted.toString());

        // Fix cursor so it stays at the end
        Selection.setSelection(s, s.length());

        isFormatting = false;
    }
}