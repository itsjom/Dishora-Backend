package com.example.dishora.defaultUI.profileTab.startSellingOption.steps;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dishora.R;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.dto.BusinessDto;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.dto.OpeningHourDto;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.dto.VendorDto;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.BusinessModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.OpeningHourModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.VendorModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.viewModel.VendorRegistrationViewModel;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.ApiService;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Confirmation extends Fragment {

    private VendorRegistrationViewModel viewModel;
    private BusinessModel business;
    private VendorModel vendor;

    private OnStepBackListener stepBackListener;

    public interface OnStepBackListener {
        void onBackToPreviousStep();
    }

    public void setStepBackListener(OnStepBackListener listener) {
        this.stepBackListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(VendorRegistrationViewModel.class);
        vendor = viewModel.getVendor();
        business = viewModel.getBusiness();

        if (vendor == null || business == null) {
            Toast.makeText(getContext(), "Error: Missing data for confirmation", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        // Map Vendor info
        ((TextView) view.findViewById(R.id.confirmFullName)).setText(vendor.getFullName());
        ((TextView) view.findViewById(R.id.confirmPhone)).setText(vendor.getPhoneNumber());

        // Map Business info
        ((TextView) view.findViewById(R.id.confirmBusinessName)).setText(business.getBusinessName());
        ((TextView) view.findViewById(R.id.confirmBusinessType)).setText(business.getType());
        ((TextView) view.findViewById(R.id.confirmDescription)).setText(business.getDescription());
        ((TextView) view.findViewById(R.id.confirmLocation)).setText(business.getLocation());
        ((TextView) view.findViewById(R.id.confirmCoordinates)).setText(business.getLatitude() + ", " + business.getLongitude());

        // Opening hours map
        StringBuilder hours = new StringBuilder();
        if (business.getOpeningHours() != null) {
            for (OpeningHourModel oh : business.getOpeningHours()) {
                hours.append(oh.getDayOfWeek()).append(": ")
                        .append(oh.isClosed() ? "Closed" : oh.getOpensAt() + " - " + oh.getClosesAt())
                        .append("\n");
            }
        }
        ((TextView) view.findViewById(R.id.confirmOpeningHours)).setText(hours.toString());

        // Requirements info
        ((TextView) view.findViewById(R.id.confirmBIR)).setText(business.getBirRegNo());
        ((TextView) view.findViewById(R.id.confirmPermit)).setText(business.getBusinessPermitNo());
        ((TextView) view.findViewById(R.id.confirmValidId)).setText(
                business.getValidIdType() + " - " + business.getValidIdNo()
        );

        // Back button
        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            if (stepBackListener != null) {
                stepBackListener.onBackToPreviousStep();
            }
        });

        // Submit button → directly call backend
        view.findViewById(R.id.submitButton).setOnClickListener(v -> submitRegistration(vendor, business));
    }

    private void submitRegistration(VendorModel vendor, BusinessModel businessModel) {
        ApiService api = ApiClient.getBackendClient(requireContext()).create(ApiService.class);
        Gson gson = new Gson();

        // Vendor JSON
        VendorDto vendorDto = new VendorDto();
        vendorDto.setFullName(vendor.getFullName());
        vendorDto.setPhoneNumber(vendor.getPhoneNumber());

        // Business JSON
        BusinessDto businessDto = new BusinessDto();
        businessDto.setBusinessName(businessModel.getBusinessName());
        businessDto.setDescription(businessModel.getDescription());
        businessDto.setType(businessModel.getType());
        businessDto.setLocation(businessModel.getLocation());
        businessDto.setLatitude(businessModel.getLatitude());
        businessDto.setLongitude(businessModel.getLongitude());
        businessDto.setBirRegNo(businessModel.getBirRegNo());
        businessDto.setBusinessPermitNo(businessModel.getBusinessPermitNo());
        businessDto.setValidIdType(businessModel.getValidIdType());
        businessDto.setValidIdNo(businessModel.getValidIdNo());
        businessDto.setBusinessDuration(businessModel.getBusinessDuration());

        // Opening hours list
        List<OpeningHourDto> openingHours = new ArrayList<>();
        if (businessModel.getOpeningHours() != null) {
            for (OpeningHourModel oh : businessModel.getOpeningHours()) {
                OpeningHourDto dto = new OpeningHourDto();
                dto.setDayOfWeek(oh.getDayOfWeek());
                dto.setOpensAt(oh.getOpensAt());
                dto.setClosesAt(oh.getClosesAt());
                dto.setClosed(oh.isClosed());
                openingHours.add(dto);
            }
        }

        // JSONs → plain string RequestBody (important for ASP.NET [FromForm])
        RequestBody vendorJson = RequestBody.create(
                gson.toJson(vendorDto), MediaType.parse("text/plain"));
        RequestBody businessJson = RequestBody.create(
                gson.toJson(businessDto), MediaType.parse("text/plain"));
        RequestBody hoursJson = RequestBody.create(
                gson.toJson(openingHours), MediaType.parse("text/plain"));

        // File parts — send only if available
        MultipartBody.Part businessImage = businessModel.getBusinessImage() != null
                ? prepareFilePart("BusinessImage", Uri.parse(businessModel.getBusinessImage()))
                : null;

        MultipartBody.Part birRegFile = businessModel.getBirRegFile() != null
                ? prepareFilePart("BirRegFile", Uri.parse(businessModel.getBirRegFile()))
                : null;

        MultipartBody.Part businessPermitFile = businessModel.getBusinessPermitFile() != null
                ? prepareFilePart("BusinessPermitFile", Uri.parse(businessModel.getBusinessPermitFile()))
                : null;

        MultipartBody.Part validIdFile = businessModel.getValidIdFile() != null
                ? prepareFilePart("ValidIdFile", Uri.parse(businessModel.getValidIdFile()))
                : null;

        MultipartBody.Part mayorPermitFile = businessModel.getMayorPermitFile() != null
                ? prepareFilePart("MayorPermitFile", Uri.parse(businessModel.getMayorPermitFile()))
                : null;

        // API call
        api.registerVendor(vendorJson, businessJson, hoursJson,
                businessImage, birRegFile, businessPermitFile, validIdFile, mayorPermitFile
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                if (res.isSuccessful()) {
                    Toast.makeText(getContext(), "Submitted successfully! Returning to profile...",
                            Toast.LENGTH_LONG).show();


                    // ✅ Save vendor state
                    requireActivity()
                            .getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("isVendor", true)
                            .putString("vendorStatus", "Pending")
                            .apply();

                    // ✅ Navigate back to Profile tab after short delay
                    requireView().postDelayed(() -> {
                        // Example if using BottomNavigationView in MainActivity:
                        // ((MainActivity) requireActivity()).switchToProfileTab();

                        // Simplest generic fallback: clear back stack to ProfileFragment
                        requireActivity().getSupportFragmentManager()
                                .popBackStack(null,
                                        requireActivity().getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
                    }, 1500);

                } else {
                    Toast.makeText(getContext(), "Submission failed: " + res.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Converts Uri → File → Multipart form-data part
    private MultipartBody.Part prepareFilePart(String name, Uri uri) {
        if (uri == null) return null;

        String mimeType = requireContext().getContentResolver().getType(uri);
        File file = new File(getRealPathFromUri(uri));

        MediaType mediaType = (mimeType != null)
                ? MediaType.parse(mimeType)
                : MediaType.parse("application/octet-stream");

        // ✅ Non-deprecated API
        RequestBody body = RequestBody.create(file, mediaType);

        return MultipartBody.Part.createFormData(name, file.getName(), body);
    }

    private String getRealPathFromUri(Uri uri) {
        File file = new File(requireContext().getCacheDir(), "upload_" + System.currentTimeMillis());
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}