package com.example.dishora.defaultUI.profileTab.startSellingOption.steps;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.AddressPickerDialogFragment;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.BusinessModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.OpeningHourModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.VendorModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.viewModel.VendorRegistrationViewModel;
import com.example.dishora.utils.PhoneNumberTextWatcher;
import com.example.dishora.utils.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BusinessInformation extends Fragment {

    private TextInputEditText openingHoursET;
    private EditText phoneET, fullNameET,
            businessNameET, businessTypeET, businessDescriptionET;
    private TextInputLayout tilBusinessLocation, layoutOpeningHours;
    private TextInputEditText etBusinessLocation;

    private ShapeableImageView businessLogoUpload;
    private Uri logoUri;
    private CardView mapPreviewContainer;

    private ActivityResultLauncher<Intent> pickLogoImageLauncher;

    private String savedAddress;
    private double savedLat, savedLon;
    private LatLng selectedLatLng;

    private final List<OpeningHourModel> openingHoursList = new ArrayList<>();
    private final List<String> DAYS_OF_WEEK = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

    private OnStepContinueListener stepContinueListener;

    public BusinessInformation() {}

    public interface OnStepContinueListener {
        void onContinueToNextStep();
    }

    public void setStepContinueListener(OnStepContinueListener listener) {
        this.stepContinueListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_business_information, container, false);

        // Initialize views
        businessLogoUpload = view.findViewById(R.id.businessLogoUpload);
        openingHoursET = view.findViewById(R.id.businessOpeningHoursET);
        layoutOpeningHours = view.findViewById(R.id.layoutOpeningHours);
        phoneET = view.findViewById(R.id.businessPhoneNumberET);
        fullNameET = view.findViewById(R.id.fullNameET);
        businessNameET = view.findViewById(R.id.businessNameET);
        businessTypeET = view.findViewById(R.id.businessTypeET);
        businessDescriptionET = view.findViewById(R.id.businessDescriptionET);
        tilBusinessLocation = view.findViewById(R.id.tilBusinessLocation);
        etBusinessLocation = view.findViewById(R.id.businessLocationET);
        mapPreviewContainer = view.findViewById(R.id.mapPreviewContainer);

        phoneET.addTextChangedListener(new PhoneNumberTextWatcher());
        setupLogoPickerLauncher();

        for (String day : DAYS_OF_WEEK) {
            openingHoursList.add(new OpeningHourModel(day, "09:00", "17:00", true)); // Default closed
        }

        // Logo click → gallery
        businessLogoUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickLogoImageLauncher.launch(Intent.createChooser(intent, "Select Business Logo"));
        });

        // Location click → open Address Picker popup
        etBusinessLocation.setOnClickListener(v -> {
            AddressPickerDialogFragment dialog = new AddressPickerDialogFragment();

            // Use the new, detailed listener
            dialog.setListener(new AddressPickerDialogFragment.AddressListener() {
                @Override
                public void onAddressConfirmed(String fullAddress, String street, String barangay,
                                               String city, String province, String postal, String region) {

                    // This is what you wanted:
                    // We only use the 'fullAddress' and ignore all the other details.
                    etBusinessLocation.setText(fullAddress);
                    savedAddress = fullAddress;
                    generateCoordinatesAndShowMap(fullAddress);
                }
            });
            dialog.show(getParentFragmentManager(), "address_picker");
        });

        // Opening hours picker
        openingHoursET.setOnClickListener(v -> showOpeningHoursDialog());

        // Next‑button logic
        Button nextBtn = view.findViewById(R.id.nextButton);
        nextBtn.setOnClickListener(v -> validateAndContinue());

        return view;
    }

    /** Generate coordinates from selected address → show map preview */
    private void generateCoordinatesAndShowMap(String fullAddress) {
        Log.d(TAG, "Generating coordinates for: " + fullAddress);
        mapPreviewContainer.setVisibility(View.VISIBLE);
        Geocoder geocoder = new Geocoder(requireContext());
        new Thread(() -> {
            try {
                List<Address> results = geocoder.getFromLocationName(fullAddress, 1);
                if (results != null && !results.isEmpty()) {
                    Address a = results.get(0);
                    LatLng coords = new LatLng(a.getLatitude(), a.getLongitude());
                    selectedLatLng = coords;
                    savedLat = a.getLatitude();
                    savedLon = a.getLongitude();
                    requireActivity().runOnUiThread(() -> updateMapPreview(coords));
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Could not find location.", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoder error", e);
            }
        }).start();
    }

    /** Display small static map with marker */
    private void updateMapPreview(LatLng location) {
        if (location == null) return;
        mapPreviewContainer.setVisibility(View.VISIBLE);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapPreview);
        if (mapFragment == null) return;
        mapFragment.getMapAsync(googleMap -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f));
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        });
    }

    /** Validation + ViewModel population */
    private void validateAndContinue() {

        String phoneText = phoneET.getText().toString().trim();
        if (!isValidPhilippinesNumber(phoneText)) {
            phoneET.setError("Enter a valid 11-digit PH number (e.g. 0912‑345‑6789)");
            phoneET.requestFocus();
            return;
        }
        if (fullNameET.getText().toString().trim().isEmpty()) {
            fullNameET.setError("Please enter your full name");
            return;
        }
        if (businessNameET.getText().toString().trim().isEmpty()) {
            businessNameET.setError("Please enter your business name");
            return;
        }
        if (businessTypeET.getText().toString().trim().isEmpty()) {
            businessTypeET.setError("Please specify a business type");
            return;
        }
        if (businessDescriptionET.getText().toString().trim().isEmpty()) {
            businessDescriptionET.setError("Please provide a description");
            return;
        }
        if (logoUri == null) {
            Toast.makeText(requireContext(),
                    "Please upload a business logo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidBusinessLocation()) {
            etBusinessLocation.setError("Please select a valid address");
            Toast.makeText(requireContext(),
                    "Select a complete address to save coordinates.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        boolean atLeastOneDayOpen = openingHoursList.stream().anyMatch(h -> !h.isClosed());
        if (!atLeastOneDayOpen) {
            layoutOpeningHours.setError(" "); // Set error on the layout
            Toast.makeText(requireContext(), "Please set opening hours for at least one day", Toast.LENGTH_SHORT).show();
            return;
        } else {
            layoutOpeningHours.setError(null); // Reset error
        }

        // ✅ All good → save into ViewModel
        if (stepContinueListener != null) {
            VendorRegistrationViewModel vm =
                    new ViewModelProvider(requireActivity()).get(VendorRegistrationViewModel.class);

            SessionManager session = new SessionManager(requireContext());
            long userId = session.getUserId();
            // long userId = 123; // TODO: replace with SessionManager userId

            VendorModel vendor = new VendorModel(
                    fullNameET.getText().toString().trim(),
                    phoneET.getText().toString().trim(),
                    userId
            );
            vm.setVendor(vendor);

            BusinessModel business = new BusinessModel();
            business.setBusinessName(businessNameET.getText().toString().trim());
            business.setDescription(businessDescriptionET.getText().toString().trim());
            business.setType(businessTypeET.getText().toString().trim());
            business.setLocation(savedAddress);
            business.setLatitude(savedLat);
            business.setLongitude(savedLon);
            business.setBusinessImage(logoUri.toString());

            business.setOpeningHours(openingHoursList);

            vm.setBusiness(business);
            stepContinueListener.onContinueToNextStep();
        }
    }

    /** Business‑location validity */
    private boolean isValidBusinessLocation() {
        return savedAddress != null && !savedAddress.trim().isEmpty()
                && savedLat != 0.0 && savedLon != 0.0;
    }

    private void setupLogoPickerLauncher() {
        pickLogoImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            logoUri = uri;
                            businessLogoUpload.setImageURI(logoUri);
                            // Set scaleType to centerCrop for user-selected image
                            businessLogoUpload.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    }
                });
    }

    /** PH‑number validation */
    private boolean isValidPhilippinesNumber(String phone) {
        String digits = phone.replaceAll("\\D", "");
        return digits.matches("^09\\d{9}$");
    }

    private void showOpeningHoursDialog() {
        // Clear any error
        layoutOpeningHours.setError(null);

        // Inflate the custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_opening_hours, null);

        // Find all 7 included layouts INSIDE the dialog view
        View hoursMon = dialogView.findViewById(R.id.hoursMon);
        View hoursTue = dialogView.findViewById(R.id.hoursTue);
        View hoursWed = dialogView.findViewById(R.id.hoursWed);
        View hoursThu = dialogView.findViewById(R.id.hoursThu);
        View hoursFri = dialogView.findViewById(R.id.hoursFri);
        View hoursSat = dialogView.findViewById(R.id.hoursSat);
        View hoursSun = dialogView.findViewById(R.id.hoursSun);

        List<View> dayViews = Arrays.asList(hoursMon, hoursTue, hoursWed, hoursThu, hoursFri, hoursSat, hoursSun);

        // Loop and bind listeners for each day, using the existing data in openingHoursList
        for (int i = 0; i < DAYS_OF_WEEK.size(); i++) {
            OpeningHourModel model = openingHoursList.get(i);
            View dayView = dayViews.get(i);
            setupDayRow(dayView, model);
        }

        // Create and show the dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Set Opening Hours")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    // When "OK" is clicked, build a summary string and set it
                    String summary = buildHoursSummary();
                    openingHoursET.setText(summary);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- NEW METHOD: Builds the summary string for the text field ---
    private String buildHoursSummary() {
        List<String> openDays = openingHoursList.stream()
                .filter(h -> !h.isClosed())
                .map(h -> h.getDayOfWeek() + ": " + h.getDisplayTime())
                .collect(Collectors.toList());

        if (openDays.isEmpty()) {
            return "Closed";
        }
        // Just show the first day's hours and "..."
        return openDays.get(0) + (openDays.size() > 1 ? ", ..." : "");
    }


    // --- NEW METHOD: To bind listeners for a single day row ---
    private void setupDayRow(View dayView, OpeningHourModel model) {
        SwitchMaterial daySwitch = dayView.findViewById(R.id.daySwitch);
        TextView dayHours = dayView.findViewById(R.id.dayHours);
        Button btnEditHours = dayView.findViewById(R.id.btnEditHours);

        daySwitch.setText(model.getDayOfWeek());
        daySwitch.setChecked(!model.isClosed());

        if (model.isClosed()) {
            dayHours.setText("Closed");
            dayHours.setTextColor(getResources().getColor(R.color.gray));
            btnEditHours.setVisibility(View.GONE);
        } else {
            dayHours.setText(model.getDisplayTime());
            dayHours.setTextColor(getResources().getColor(R.color.black));
            btnEditHours.setVisibility(View.VISIBLE);
        }

        daySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            model.setClosed(!isChecked);
            if (!isChecked) { // Is Closed
                dayHours.setText("Closed");
                dayHours.setTextColor(getResources().getColor(R.color.gray));
                btnEditHours.setVisibility(View.GONE);
            } else { // Is Open
                dayHours.setText(model.getDisplayTime());
                dayHours.setTextColor(getResources().getColor(R.color.black));
                btnEditHours.setVisibility(View.VISIBLE);
            }
        });

        btnEditHours.setOnClickListener(v -> {
            showTimePickersForDay(model, dayHours);
        });
    }

    // --- NEW METHOD: Replaces old time pickers ---
    private void showTimePickersForDay(OpeningHourModel model, TextView dayHours) {
        MaterialTimePicker startPicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(model.getOpensAtHour())
                .setMinute(model.getOpensAtMinute())
                .setTitleText("Select Opening Time for " + model.getDayOfWeek())
                .build();

        startPicker.show(getParentFragmentManager(), "start");

        startPicker.addOnPositiveButtonClickListener(v -> {
            int startHour = startPicker.getHour();
            int startMinute = startPicker.getMinute();

            MaterialTimePicker endPicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(model.getClosesAtHour())
                    .setMinute(model.getClosesAtMinute())
                    .setTitleText("Select Closing Time for " + model.getDayOfWeek())
                    .build();

            endPicker.show(getParentFragmentManager(), "end");

            endPicker.addOnPositiveButtonClickListener(v2 -> {
                int endHour = endPicker.getHour();
                int endMinute = endPicker.getMinute();

                // Save data to model
                model.setOpensAt(String.format(Locale.US, "%02d:%02d", startHour, startMinute));
                model.setClosesAt(String.format(Locale.US, "%02d:%02d", endHour, endMinute));

                // Update UI
                dayHours.setText(model.getDisplayTime());
            });
        });
    }

    /** Opening‑hour pickers (same as before) */
    /** private void showOpeningTimePicker() {
        MaterialTimePicker startPicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(9).setMinute(0)
                .setTitleText("Select Opening Time")
                .build();
        startPicker.show(getParentFragmentManager(), "start");
        startPicker.addOnPositiveButtonClickListener(v -> {
            opensAt24 = String.format(Locale.getDefault(),
                    "%02d:%02d", startPicker.getHour(), startPicker.getMinute());
            showClosingTimePicker(startPicker.getHour(), startPicker.getMinute());
        });
    }

    private void showClosingTimePicker(int startHour, int startMinute) {
        MaterialTimePicker endPicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(16).setMinute(0)
                .setTitleText("Select Closing Time")
                .build();
        endPicker.show(getParentFragmentManager(), "end");
        endPicker.addOnPositiveButtonClickListener(v -> {
            closesAt24 = String.format(Locale.getDefault(),
                    "%02d:%02d", endPicker.getHour(), endPicker.getMinute());
            String display = String.format(Locale.getDefault(),
                    "%02d:%02d %s - %02d:%02d %s",
                    to12Hour(startHour), startMinute, (startHour >= 12 ? "PM" : "AM"),
                    to12Hour(endPicker.getHour()), endPicker.getMinute(),
                    (endPicker.getHour() >= 12 ? "PM" : "AM"));
            showDaySelectionDialog(display);
        });
    }

    private int to12Hour(int hour) {
        return (hour == 0 || hour == 12) ? 12 : hour % 12;
    }

    private void showDaySelectionDialog(String displayTime) {
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        boolean[] checked = new boolean[days.length];
        List<String> selectedDays = new ArrayList<>();

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Operating Days")
                .setMultiChoiceItems(days, checked, (dialog, which, isChecked) -> {
                    if (isChecked) selectedDays.add(days[which]);
                    else selectedDays.remove(days[which]);
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    String result;
                    if (selectedDays.isEmpty()) result = "";
                    else if (selectedDays.size() == 1) result = selectedDays.get(0);
                    else result = selectedDays.get(0) + " - " +
                                selectedDays.get(selectedDays.size() - 1);
                    openingHoursET.setText(displayTime + ", " + result);
                })
                .setNegativeButton("Cancel", null)
                .show();
    } **/
}