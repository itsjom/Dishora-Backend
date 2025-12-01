package com.example.dishora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dishora.defaultUI.CustomerMainActivity;
import com.example.dishora.models.LoginRequest;
import com.example.dishora.models.LoginResponse;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.ApiService;
import com.example.dishora.utils.SessionManager;
import com.example.dishora.vendorUI.VendorMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private TextInputEditText emailET, passwordET;
    private ProgressBar progressBar;
    private FrameLayout loadingOverlay;
    private MaterialButton loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Check existing session
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isVendor() && "Approved".equalsIgnoreCase(sessionManager.getVendorStatus())) {
                startActivity(new Intent(Login.this, VendorMainActivity.class));
            } else {
                startActivity(new Intent(Login.this, CustomerMainActivity.class));
            }
            finish();
            return;
        }

        // Initialize UI
        emailET = findViewById(R.id.emailInput);
        passwordET = findViewById(R.id.passwordInput);
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        loginBtn = findViewById(R.id.logInBtn);
        CheckBox rememberCheck = findViewById(R.id.checkBox);
        rememberCheck.setChecked(true);

        // Navigation actions
        findViewById(R.id.signInLink)
                .setOnClickListener(v -> startActivity(new Intent(Login.this, SignUp.class)));
        findViewById(R.id.forgotPassTV)
                .setOnClickListener(v -> startActivity(new Intent(Login.this, ForgotPassword.class)));

        // Login button click
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailET.getText() != null ? emailET.getText().toString().trim() : "";
        String password = passwordET.getText() != null ? passwordET.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        ApiService api = ApiClient.getBackendClient().create(ApiService.class);
        api.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);
                Log.d("LOGIN_RESPONSE_CODE", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    Log.d("LOGIN_RESPONSE_BODY", "Response body: " + new Gson().toJson(res));

                    if (res.isSuccess()) {
                        LoginResponse.UserData data = res.getData();
                        SessionManager sessionManager = new SessionManager(getApplicationContext());
                        sessionManager.createLoginSession(data);

                        Toast.makeText(Login.this,
                                "Welcome, " + (data.getUsername() != null ? data.getUsername() : "User") + "!",
                                Toast.LENGTH_SHORT).show();

                        Intent nextScreen;
                        if (data.isVendor() && "Approved".equalsIgnoreCase(data.getVendorStatus())) {
                            nextScreen = new Intent(Login.this, VendorMainActivity.class);
                        } else {
                            nextScreen = new Intent(Login.this, CustomerMainActivity.class);
                        }

                        nextScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(nextScreen);
                        finish();
                    } else {
                        Toast.makeText(Login.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("LOGIN_ERROR_BODY", "Error body: " + errorBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(Login.this,
                            "Login failed. Server error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Log.e("LOGIN_FAILURE", "Network failure: " + t.getMessage(), t);
                Toast.makeText(Login.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        loginBtn.setEnabled(!show);
    }
}