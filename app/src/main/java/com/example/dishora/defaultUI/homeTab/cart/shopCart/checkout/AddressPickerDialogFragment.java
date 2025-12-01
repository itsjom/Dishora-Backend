package com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dishora.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AddressPickerDialogFragment extends DialogFragment {

    // --- FIX 1: Update the listener interface ---
    public interface AddressListener {
        void onAddressConfirmed(
                String fullAddress, String street, String barangay,
                String city, String province, String postal, String region
        );
    }
    // --- End Fix 1 ---

    private AddressListener listener;
    private JSONObject addressData;
    private final Map<String, String> regionKeyMap = new HashMap<>();

    public void setListener(AddressListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(requireActivity())
                .inflate(R.layout.dialog_address_picker, null);

        AutoCompleteTextView regionEt = view.findViewById(R.id.actvRegionDialog);
        AutoCompleteTextView provinceEt = view.findViewById(R.id.actvProvinceDialog);
        AutoCompleteTextView cityEt = view.findViewById(R.id.actvCityDialog);
        AutoCompleteTextView barangayEt = view.findViewById(R.id.actvBarangayDialog);
        TextInputEditText streetEt = view.findViewById(R.id.etStreetDialog);
        TextInputEditText postalEt = view.findViewById(R.id.etPostalDialog);
        TextInputLayout provinceTil = view.findViewById(R.id.tilProvinceDialog);
        TextInputLayout cityTil = view.findViewById(R.id.tilCityDialog);
        TextInputLayout barangayTil = view.findViewById(R.id.tilBarangayDialog);

        loadAddressData();

        // --- Populate regions ---
        ArrayList<String> regionList = new ArrayList<>();
        if (addressData != null) {
            JSONArray regionCodes = addressData.names();
            if (regionCodes != null) {
                for (int i = 0; i < regionCodes.length(); i++) {
                    try {
                        String code = regionCodes.getString(i);
                        JSONObject regionObj = addressData.getJSONObject(code);
                        String regionName = regionObj.optString("region_name", code);
                        regionList.add(regionName);
                        regionKeyMap.put(regionName, code);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Collections.sort(regionList, String::compareToIgnoreCase);
        regionEt.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, regionList));

        // --- Region selected ---
        regionEt.setOnItemClickListener((parent, v, position, id) -> {
            String regionName = parent.getItemAtPosition(position).toString();
            String regionKey = regionKeyMap.get(regionName);

            provinceEt.setText("", false);
            cityEt.setText("", false);
            barangayEt.setText("", false);
            postalEt.setText("");
            cityTil.setEnabled(false);
            barangayTil.setEnabled(false);

            populateProvinces(provinceEt, regionKey);
            provinceTil.setEnabled(true);
        });

        // --- Province selected ---
        provinceEt.setOnItemClickListener((parent, v, position, id) -> {
            String regionKey = regionKeyMap.get(regionEt.getText().toString());
            String province = parent.getItemAtPosition(position).toString();

            cityEt.setText("", false);
            barangayEt.setText("", false);
            postalEt.setText("");
            barangayTil.setEnabled(false);

            populateCities(cityEt, regionKey, province);
            cityTil.setEnabled(true);
        });

        // --- City selected ---
        cityEt.setOnItemClickListener((parent, v, position, id) -> {
            String regionKey = regionKeyMap.get(regionEt.getText().toString());
            String province = provinceEt.getText().toString();
            String city = parent.getItemAtPosition(position).toString();

            barangayEt.setText("", false);
            postalEt.setText("");

            // Fill barangays and auto‑fill postal code
            populateBarangays(barangayEt, regionKey, province, city);
            autoFillPostalCode(regionKey, province, city, postalEt);
            barangayTil.setEnabled(true);
        });

        // --- Buttons ---
        view.findViewById(R.id.btnCancelDialog).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnConfirmDialog).setOnClickListener(v -> {
            String street = streetEt.getText() != null ? streetEt.getText().toString().trim() : "";
            String barangay = barangayEt.getText().toString().trim();
            String city = cityEt.getText().toString().trim();
            String province = provinceEt.getText().toString().trim();
            String postal = postalEt.getText() != null ? postalEt.getText().toString().trim() : "";
            // --- FIX 2: Get the region text ---
            String region = regionEt.getText().toString().trim();

            if (street.isEmpty() || barangay.isEmpty() || city.isEmpty()
                    || province.isEmpty() || postal.isEmpty() || region.isEmpty()) { // Added region check
                Toast.makeText(getContext(), "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String fullAddress = street + ", " + barangay + ", " + city + ", "
                    + province + " " + postal;

            // --- FIX 3: Pass all data to the new listener ---
            if (listener != null) {
                listener.onAddressConfirmed(fullAddress, street, barangay, city, province, postal, region);
            }
            // --- End Fix 3 ---

            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    private void loadAddressData() {
        try {
            InputStream is = getResources().openRawResource(R.raw.philippine_addresses);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            addressData = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            addressData = null;
        }
    }

    private void populateProvinces(AutoCompleteTextView actv, String regionKey) {
        ArrayList<String> list = new ArrayList<>();
        try {
            JSONArray names = addressData.getJSONObject(regionKey)
                    .getJSONObject("province_list").names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++)
                    list.add(names.getString(i));
            }
            Collections.sort(list, String::compareToIgnoreCase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, list));
    }

    private void populateCities(AutoCompleteTextView actv, String regionKey, String province) {
        ArrayList<String> list = new ArrayList<>();
        try {
            JSONArray names = addressData.getJSONObject(regionKey)
                    .getJSONObject("province_list")
                    .getJSONObject(province)
                    .getJSONObject("municipality_list").names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++)
                    list.add(names.getString(i));
            }
            Collections.sort(list, String::compareToIgnoreCase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, list));
    }

    private void populateBarangays(AutoCompleteTextView actv, String regionKey,
                                   String province, String city) {
        ArrayList<String> list = new ArrayList<>();
        try {
            JSONArray arr = addressData.getJSONObject(regionKey)
                    .getJSONObject("province_list")
                    .getJSONObject(province)
                    .getJSONObject("municipality_list")
                    .getJSONObject(city)
                    .getJSONArray("barangay_list");
            for (int i = 0; i < arr.length(); i++)
                list.add(arr.getString(i));
            Collections.sort(list, String::compareToIgnoreCase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, list));
    }

    private void autoFillPostalCode(String regionKey, String province, String city,
                                    TextInputEditText postalEt) {
        try {
            JSONObject municipalityObj = addressData.getJSONObject(regionKey)
                    .getJSONObject("province_list")
                    .getJSONObject(province)
                    .getJSONObject("municipality_list")
                    .getJSONObject(city);

            String postal = municipalityObj.optString("postal_code", "");
            postalEt.setText(postal);
        } catch (JSONException e) {
            e.printStackTrace();
            postalEt.setText("");
        }
    }
}