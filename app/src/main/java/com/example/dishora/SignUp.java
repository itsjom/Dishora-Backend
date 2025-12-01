package com.example.dishora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dishora.models.RegisterRequest;
import com.example.dishora.models.RegisterResponse;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {

    private TextInputEditText fullNameET, usernameET, emailET, passwordET;
    private MaterialButton registerBtn;
    private FrameLayout loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // View setup
        fullNameET = findViewById(R.id.fullNameInput);
        usernameET = findViewById(R.id.usernameInput);
        emailET = findViewById(R.id.emailInput);
        passwordET = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.button);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        registerBtn.setOnClickListener(v -> registerUser());

        findViewById(R.id.logInLink).setOnClickListener(v -> {
            startActivity(new Intent(SignUp.this, Login.class));
            finish();
        });
    }

    private void registerUser() {
        String fullName = fullNameET.getText() != null ? fullNameET.getText().toString().trim() : "";
        String username = usernameET.getText() != null ? usernameET.getText().toString().trim() : "";
        String email = emailET.getText() != null ? emailET.getText().toString().trim() : "";
        String password = passwordET.getText() != null ? passwordET.getText().toString().trim() : "";

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        RegisterRequest request = new RegisterRequest(fullName, username, email, password);
        ApiService apiService = ApiClient.getBackendClient().create(ApiService.class);

        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse res = response.body();
                    // Toast.makeText(SignUp.this, res.getMessage(), Toast.LENGTH_SHORT).show();

                    if (res.isSuccess()) {
                        new AlertDialog.Builder(SignUp.this)
                                .setTitle("Almost there!")
                                .setMessage("We’ve sent a verification link to your email.\nPlease verify your account before logging in.")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    startActivity(new Intent(SignUp.this, Login.class));
                                    finish();
                                })
                                .show();
                    }
                } else if (response.code() == 409) {
                    Toast.makeText(SignUp.this,
                            "Username or email already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUp.this,
                            "Register failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SignUp.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        registerBtn.setEnabled(!show);
    }
}