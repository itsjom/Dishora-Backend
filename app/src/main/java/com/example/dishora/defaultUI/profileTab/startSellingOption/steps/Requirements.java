package com.example.dishora.defaultUI.profileTab.startSellingOption.steps;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.dishora.R;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.BusinessModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.viewModel.VendorRegistrationViewModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Requirements extends Fragment {

    private EditText editTextTIN;
    private LinearLayout noBirLayout;
    private TextView noBirTV;
    private ImageView noBirIcon;

    private String businessDuration = null; // NEW field for "no BIR" path

    // Track files independently
    private Uri birUri = null;
    private Uri businessPermitUri = null;
    private Uri frontIdUri = null;
    private Uri backIdUri = null;
    // private Uri mayorPermitUri = null;

    private Button nextBtn;

    private int currentViewSwitcherId = -1; // Track which section triggered file picker

    private OnStepContinueListener stepContinueListener;
    private OnStepBackListener stepBackListener;


    public interface OnStepContinueListener {
        void onContinueToNextStep();
    }

    public void setStepContinueListener(OnStepContinueListener listener) {
        this.stepContinueListener = listener;
    }

    public interface OnStepBackListener {
        void onBackToPreviousStep();
    }

    public void setStepBackListener(OnStepBackListener listener) {
        this.stepBackListener = listener;
    }

    // ActivityResultLauncher (replaces deprecated onActivityResult)
    private ActivityResultLauncher<Intent> filePickerLauncher;

    public Requirements() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requirements, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up TIN EditText
        editTextTIN = view.findViewById(R.id.editTextTIN);

        // Set up default text
        Spinner spinner = view.findViewById(R.id.spinnerIdType);

        noBirLayout = view.findViewById(R.id.noBIRLayout);
        noBirTV = view.findViewById(R.id.noBirTV);
        noBirIcon = view.findViewById(R.id.noBIRIcon);

        noBirLayout.setOnClickListener(v -> openNoBirDialog());

        // Load items from array
        String[] idOptions = getResources().getStringArray(R.array.valid_id_options);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, idOptions) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item ("Select ID Type")
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Hint text color
                    tv.setTextColor(Color.GRAY);
                } else {
                    // Normal text color
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Ensure default selection is HINT
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (position > 0) { // user picked a real ID type, not the hint
                    // ✅ Show front/back uploaders
                    LinearLayout frontContainer = requireView().findViewById(R.id.frontIdContainer);
                    LinearLayout backContainer = requireView().findViewById(R.id.backIdContainer);

                    frontContainer.setVisibility(View.VISIBLE);
                    backContainer.setVisibility(View.VISIBLE);
                } else {
                    // ✅ Hide again if they reset to "Select ID Type"
                    LinearLayout frontContainer = requireView().findViewById(R.id.frontIdContainer);
                    LinearLayout backContainer = requireView().findViewById(R.id.backIdContainer);

                    frontContainer.setVisibility(View.GONE);
                    backContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup ActivityResultLauncher
        setupFilePickerLauncher();

        // Setup upload sections
        setupFileUpload(view, R.id.viewSwitcher);                 // BIR
        setupFileUpload(view, R.id.businessPermitViewSwitcher);   // Business Permit
        setupFileUpload(view, R.id.frontIdViewSwitcher);          // Valid ID Front
        setupFileUpload(view, R.id.backIdViewSwitcher);           // Valid ID Back

        // Next button (go to next step)
        nextBtn = view.findViewById(R.id.nextButton);
        nextBtn.setOnClickListener(v -> {
            if (stepContinueListener != null) {
                boolean hasBIR = (birUri != null);
                boolean hasNoBIR = (businessDuration != null && !businessDuration.isEmpty());

                // Example: check if all required files are uploaded
                if (!(hasBIR || hasNoBIR) || businessPermitUri == null || frontIdUri == null || backIdUri == null) {
                    Toast.makeText(getContext(), "Please complete all required documents", Toast.LENGTH_SHORT).show();
                } else {
                    // ✅ Update BusinessModel
                    VendorRegistrationViewModel viewModel =
                            new ViewModelProvider(requireActivity()).get(VendorRegistrationViewModel.class);
                    BusinessModel business = viewModel.getBusiness();

                    if (business != null) {
                        business.setBirRegNo(editTextTIN.getText().toString().trim());
                        business.setBirRegFile(birUri != null ? birUri.toString() : null);
                        business.setBusinessPermitFile(businessPermitUri != null ? businessPermitUri.toString() : null);
                        business.setValidIdType(spinner.getSelectedItem().toString());
                        business.setValidIdNo("ID-123"); // or parse from another field
                        business.setValidIdFile(frontIdUri != null ? frontIdUri.toString() : null);
                        //business.setMayorPermitFile(mayorPermitUri != null ? mayorPermitUri.toString() : null); // or uri if you have
                        if (businessDuration != null) business.setBusinessDuration(businessDuration);

                        viewModel.setBusiness(business);
                    }
                    stepContinueListener.onContinueToNextStep();
                }
            }
        });

        Button backBtn = view.findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> {
            if (stepBackListener != null) {
                stepBackListener.onBackToPreviousStep();
            }
        });
    }

    /** Sets up launcher for picking files **/
    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        String fileName = getFileName(fileUri);

                        if (currentViewSwitcherId != -1) {
                            ViewSwitcher switcher = requireView().findViewById(currentViewSwitcherId);
                            View previewLayout = switcher.getChildAt(1);

                            TextView tvFileName = previewLayout.findViewById(R.id.tvPreviewFileName);
                            ImageView imgThumbnail = previewLayout.findViewById(R.id.imgThumbnail);
                            ImageButton btnView = previewLayout.findViewById(R.id.btnView);

                            // Show File Name
                            tvFileName.setText(fileName != null ? fileName : "Selected File");

                            String mimeType = requireContext().getContentResolver().getType(fileUri);

                            if (mimeType != null) {
                                if (mimeType.startsWith("image/")) {
                                    imgThumbnail.setImageURI(fileUri);
                                    btnView.setVisibility(View.VISIBLE);
                                } else if (mimeType.equals("application/pdf")) {
                                    imgThumbnail.setImageResource(R.drawable.ic_pdf);
                                    btnView.setVisibility(View.VISIBLE);
                                } else {
                                    imgThumbnail.setImageResource(R.drawable.ic_file);
                                    btnView.setVisibility(View.GONE);
                                }
                            }

                            // View Button
                            btnView.setOnClickListener(v -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(fileUri, "*/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                try {
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "No app can open this file", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Switch to preview display
                            switcher.setDisplayedChild(1);

                            // 🔑 Save fileUri to the right variable
                            if (currentViewSwitcherId == R.id.viewSwitcher) {
                                birUri = fileUri;
                                runOcrAndExtract(birUri); // OCR for BIR only
                            } else if (currentViewSwitcherId == R.id.businessPermitViewSwitcher) {
                                businessPermitUri = fileUri;
                            } else if (currentViewSwitcherId == R.id.frontIdViewSwitcher) {
                                frontIdUri = fileUri;
                            } else if (currentViewSwitcherId == R.id.backIdViewSwitcher) {
                                backIdUri = fileUri;
                            }
                        }
                    }
                }
        );
    }

    /** Helper method: attach upload/preview logic to a ViewSwitcher */
    private void setupFileUpload(View rootView, int viewSwitcherId) {
        ViewSwitcher switcher = rootView.findViewById(viewSwitcherId);

        // Upload Layout (first child)
        View uploadLayout = switcher.getChildAt(0);
        Button uploadBtn = uploadLayout.findViewById(R.id.uploadBtn);
        TextView fileNameUpload = uploadLayout.findViewById(R.id.tvUploadFileName);

        // Preview Layout (second child)
        View previewLayout = switcher.getChildAt(1);
        TextView fileNamePreview = previewLayout.findViewById(R.id.tvPreviewFileName);
        ImageView imgThumbnail = previewLayout.findViewById(R.id.imgThumbnail);
        ImageButton btnDelete = previewLayout.findViewById(R.id.btnDelete);
        ImageButton btnDownload = previewLayout.findViewById(R.id.btnDownload);

        // UPLOAD
        uploadBtn.setOnClickListener(v -> {
            currentViewSwitcherId = viewSwitcherId;
            openFilePicker();
        });

        // DELETE → back to Upload state
        btnDelete.setOnClickListener(v -> {
            switcher.setDisplayedChild(0); // Always go back to upload

            if (viewSwitcherId == R.id.viewSwitcher) {
                birUri = null;
                editTextTIN.setText("");
                editTextTIN.setVisibility(View.GONE);
            } else if (viewSwitcherId == R.id.businessPermitViewSwitcher) {
                businessPermitUri = null;
            } else if (viewSwitcherId == R.id.frontIdViewSwitcher) {
                frontIdUri = null;
            } else if (viewSwitcherId == R.id.backIdViewSwitcher) {
                backIdUri = null;
            }
        });

        // DOWNLOAD demo
        btnDownload.setOnClickListener(v ->
                Toast.makeText(getContext(), "Downloading " + fileNamePreview.getText(), Toast.LENGTH_SHORT).show()
        );
    }

    /** Run OCR Extraction for TIN */
    private void runOcrAndExtract(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(requireContext(), uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String fullText = visionText.getText();

                        // Regex for TIN
                        Pattern tinPattern = Pattern.compile("\\d{3}-\\d{3}-\\d{3}(-\\d{3})?");
                        Matcher tinMatcher = tinPattern.matcher(fullText);
                        boolean tinFound = tinMatcher.find();

                        if (tinFound) {
                            String tinValue = tinMatcher.group();
                            editTextTIN.setText(tinValue);
                            editTextTIN.setVisibility(View.VISIBLE);
                        } else {
                            editTextTIN.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "No valid TIN found in file", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Text extraction failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Launch system file picker */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }

    /** Utility: get actual filename from Uri */
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment(); // fallback
        }
        return result;
    }

    /** Launch no BIR dialog */
    private void openNoBirDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_no_bir, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText etBusinessDuration = dialogView.findViewById(R.id.etBusinessDuration);
        Button btnContinue = dialogView.findViewById(R.id.btnContinue);
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnContinue.setOnClickListener(v -> {
            String duration = etBusinessDuration.getText().toString().trim();
            if (duration.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a value", Toast.LENGTH_SHORT).show();
            } else {
                businessDuration = duration;

                // 1. Disable the ViewSwitcher + Upload button
                ViewSwitcher birSwitcher = requireView().findViewById(R.id.viewSwitcher);
                birSwitcher.setAlpha(0.4f);

                View uploadLayout = birSwitcher.getChildAt(0);
                Button uploadBtn = uploadLayout.findViewById(R.id.uploadBtn);
                uploadBtn.setEnabled(false);

                editTextTIN.setVisibility(View.GONE);
                birUri = null;

                // 2. Update UI
                noBirTV.setText("Business running: " + duration + " (no BIR)");
                noBirTV.setTextColor(Color.BLACK);
                noBirIcon.setImageResource(android.R.drawable.ic_dialog_info);

                dialog.dismiss();
            }
        });
    }
}