package com.example.dishora.reusable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.example.dishora.R;

public class ReusableAddressCard extends LinearLayout {

    private static final String TAG = "ReusableAddressCard";

    public enum Mode {
        PRE_ORDER,
        ADDRESS_LIST
    }

    private TextView name, phone, address;
    private ImageView iconChevron, iconLocation;
    private RadioButton radioButton;
    private Button editButton;

    private FragmentManager fragmentManager;

    public ReusableAddressCard(Context context) {
        super(context);
        init(context);
    }

    public ReusableAddressCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReusableAddressCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_address_card, this, true);

        setClickable(true);
        setFocusable(true);

        name = findViewById(R.id.txt_name);
        phone = findViewById(R.id.txt_phone);
        address = findViewById(R.id.txt_address);

        setOnClickListener(v -> Log.d(TAG, "ReusableAddressCard clicked (from inside class)"));

    }

    public void setData(String fullName, String phoneNumber, String addressText) {
        Log.d(TAG, "setData() called: " + fullName + " | " + phoneNumber + " | " + addressText);
        if (name != null) name.setText(fullName);
        if (phone != null) phone.setText(phoneNumber);
        if (address != null) address.setText(addressText);
    }
}
