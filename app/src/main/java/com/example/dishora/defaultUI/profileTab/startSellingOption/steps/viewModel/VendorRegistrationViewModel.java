package com.example.dishora.defaultUI.profileTab.startSellingOption.steps.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.BusinessModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.OpeningHourModel;
import com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models.VendorModel;

import java.util.List;

public class VendorRegistrationViewModel extends ViewModel {
    private final MutableLiveData<VendorModel> vendor = new MutableLiveData<>();
    private final MutableLiveData<BusinessModel> business = new MutableLiveData<>();
    private final MutableLiveData<List<OpeningHourModel>> openingHours = new MutableLiveData<>();

    public void setVendor(VendorModel model) { vendor.setValue(model); }
    public VendorModel getVendor() { return vendor.getValue(); }

    public void setBusiness(BusinessModel model) { business.setValue(model); }
    public BusinessModel getBusiness() { return business.getValue(); }

    public void setBusinessOpeningHours(List<OpeningHourModel> models) { openingHours.setValue(models); }
    public List<OpeningHourModel> getBusinessOpeningHours() { return openingHours.getValue(); }
}
