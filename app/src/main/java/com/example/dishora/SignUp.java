package com.example.dishora;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dishora.models.RegisterRequest;
import com.example.dishora.models.RegisterResponse;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {
    private TextView logInTV;
    private Button registerBtn;
    private EditText usernameET;
    private EditText emailET;
    private EditText passwordET;
    private ImageView togglePassVisibility;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        registerBtn = findViewById(R.id.button);
        usernameET = findViewById(R.id.usernameInput);
        emailET = findViewById(R.id.emailInput);
        passwordET = findViewById(R.id.passwordInput);
        togglePassVisibility = findViewById(R.id.togglePasswordVisibility);
        progressBar = findViewById(R.id.prgrssBar);

        registerBtn.setOnClickListener(v -> {
                String username = usernameET.getText().toString().trim();
                String email = emailET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String fullName =" Anonymous";
                RegisterRequest request = new RegisterRequest(fullName, username, email, password);
                ApiService apiService = ApiClient.getBackendClient().create(ApiService.class);

                progressBar.setVisibility(View.VISIBLE);    // Show loading

                apiService.register(request).enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                        progressBar.setVisibility(View.GONE);   // Hide loading

                        if (response.isSuccessful() && response.body() != null) {
                            RegisterResponse res = response.body();
                            Toast.makeText(SignUp.this, res.getMessage(), Toast.LENGTH_SHORT).show();

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
                        } else {
                            if (response.code() == 409) {
                                Toast.makeText(SignUp.this, "Username or email already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignUp.this, "Register failed: " + response.code(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignUp.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });

        togglePassVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordET.getTransformationMethod() == PasswordTransformationMethod.getInstance()){
                    passwordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    togglePassVisibility.setImageResource(R.drawable.pass_show);
                }
                else {
                    passwordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    togglePassVisibility.setImageResource(R.drawable.pass_hide);
                }
                passwordET.setSelection(passwordET.getText().length());
            }
        });

        logInTV = findViewById(R.id.logInLink);
        logInTV.setOnClickListener(v -> finish());  //closes the current activity
    }
}