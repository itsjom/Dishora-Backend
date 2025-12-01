package com.example.dishora.defaultUI.profileTab.startSellingOption;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.BusinessInformation;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.Confirmation;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.Requirements;

public class StartSellFragment extends Fragment implements BusinessInformation.OnStepContinueListener, Requirements.OnStepContinueListener, Requirements.OnStepBackListener, Confirmation.OnStepBackListener {

    private EditText openingHoursET;
    private int currentStep = 1;

    public StartSellFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_sell, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BusinessInformation step1 = new BusinessInformation();
        step1.setStepContinueListener(this);
        showStep(step1);

        // Load step 1
        highlightStep(1);
    }

    private void showStep(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.stepContainer, fragment)
                .commit();
    }

    private void highlightStep(int step) {
        TextView step1 = getView().findViewById(R.id.step1Circle);
        TextView step2 = getView().findViewById(R.id.step2Circle);
        TextView step3 = getView().findViewById(R.id.step3Circle);
        View c12 = getView().findViewById(R.id.connector1);
        View c23 = getView().findViewById(R.id.connector2);

        int accent = ContextCompat.getColor(requireContext(), R.color.orange);
        int gray = ContextCompat.getColor(requireContext(), R.color.gray);

        // reset everything to default inactive look
        step1.setBackgroundResource(R.drawable.step_inactive_circle);
        step2.setBackgroundResource(R.drawable.step_inactive_circle);
        step3.setBackgroundResource(R.drawable.step_inactive_circle);
        c12.setBackgroundColor(gray);
        c23.setBackgroundColor(gray);

        // now "light up" completed steps and connectors, then active one
        if (step >= 1) {
            step1.setBackgroundResource(step == 1 ? R.drawable.step_active_circle : R.drawable.step_completed_circle);
        }

        if (step >= 2) {
            c12.setBackgroundColor(accent);
            step2.setBackgroundResource(step == 2 ? R.drawable.step_active_circle : R.drawable.step_completed_circle);
        }

        if (step == 3) {
            c23.setBackgroundColor(accent);
            step3.setBackgroundResource(R.drawable.step_active_circle);
        }
    }

    @Override
    public void onContinueToNextStep() {
        if (currentStep == 1) {
            Requirements step2 = new Requirements();
            step2.setStepContinueListener(this);
            step2.setStepBackListener(this);
            showStep(step2);
            highlightStep(2);
            currentStep = 2;
        } else if (currentStep == 2) {
            Confirmation step3 = new Confirmation();
            //step3.setStepContinueListener(this);
            step3.setStepBackListener(this);
            showStep(step3);
            highlightStep(3);
            currentStep = 3;
        }
    }

    @Override
    public void onBackToPreviousStep() {
        if (currentStep == 2) {
            BusinessInformation step1 = new BusinessInformation();
            step1.setStepContinueListener(this);
            showStep(step1);
            highlightStep(1);
            currentStep = 1;
        } else if (currentStep == 3) {
            Requirements step2 = new Requirements();
            step2.setStepContinueListener(this);
            step2.setStepBackListener(this);
            showStep(step2);
            highlightStep(2);
            currentStep = 2;
        }
    }
}