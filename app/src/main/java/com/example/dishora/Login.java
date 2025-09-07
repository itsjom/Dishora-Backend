package com.example.dishora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.dishora.models.LoginResponse.UserData;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dishora.models.LoginRequest;
import com.example.dishora.models.LoginResponse;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private EditText emailET, passwrdET;
    private ImageView togglePassword;
    private ProgressBar progressBar;
    private Button loginBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);

        // Check login status
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
            return;
        }

        // Initialize UI
        emailET = findViewById(R.id.emailInput);
        passwrdET = findViewById(R.id.passwordInput);
        togglePassword = findViewById(R.id.togglePasswordVisibility);
        progressBar = findViewById(R.id.progressBar);
        loginBtn = findViewById(R.id.logInBtn);
        CheckBox cb = findViewById(R.id.checkBox);
        cb.setChecked(true); // Default checked

        // Toggle password visibility
        togglePassword.setOnClickListener(v -> {
            if (passwrdET.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                passwrdET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.pass_show);
            } else {
                passwrdET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.pass_hide);
            }
            passwrdET.setSelection(passwrdET.getText().length());
        });

        // Go to SignUp
        TextView signInView = findViewById(R.id.signInLink);
        signInView.setOnClickListener(v -> startActivity(new Intent(Login.this, SignUp.class)));

        // Go to ForgotPassword
        TextView forgotPassView = findViewById(R.id.forgotPassTV);
        forgotPassView.setOnClickListener(v -> startActivity(new Intent(Login.this, ForgotPassword.class)));

        // Handle login
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailET.getText().toString().trim();
        String password = passwrdET.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);

        LoginRequest request = new LoginRequest(email, password);
        ApiService api = ApiClient.getBackendClient().create(ApiService.class);

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                loginBtn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    if (res.isSuccess()) {
                        UserData data = res.getData();

                        // Save login status
                        sharedPreferences.edit()
                                .putString("email", email)
                                .putBoolean("isLoggedIn", true)
                                .putString("username", data.getUsername())
                                .putLong("user_id", data.getUserId())
                                .apply();

                        Toast.makeText(Login.this, "Welcome, " + data.getUsername() + "!", Toast.LENGTH_SHORT).show();

                        // Go to main
                        Intent i = new Intent(Login.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(Login.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login.this, "Login failed. Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                loginBtn.setEnabled(true);
                Toast.makeText(Login.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
