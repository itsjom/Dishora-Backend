package com.example.dishora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.security.KeyStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    //
    private EditText emailET, passwrdET;
    private ImageView togglePassword;
    private TextView signInView, forgotPassView;
    private Button loginBtn;

    //SharedPreferences
//    private SharedPreferences sharedPreferences;
//    private static final String PREF_NAME = "MyPreferences";
//    private static final String KEY_USERNAME = "username";
//    private static final String KEY_PASSWORD = "password";
//    private static final String KEY_REMEMBER = "remember";

//
    private static final String KEYSTORE_ALIAS ="secret_key";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

//
    private KeyStore keyStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Already logged in
            Intent i = new Intent(Login.this, MainActivity.class);
            startActivity(i);
            finish(); // don't allow back to login
        }

        emailET = findViewById(R.id.emailInput);
        passwrdET = findViewById(R.id.passwordInput);
        togglePassword = findViewById(R.id.togglePasswordVisibility);

        CheckBox cb = findViewById(R.id.checkBox);
        cb.setChecked(true);

        togglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwrdET.getTransformationMethod() == PasswordTransformationMethod.getInstance()){
                    passwrdET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    togglePassword.setImageResource(R.drawable.pass_show);
                }
                else {
                    passwrdET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    togglePassword.setImageResource(R.drawable.pass_hide);
                }
                passwrdET.setSelection(passwrdET.getText().length());
            }
        });

        signInView = findViewById(R.id.signInLink);
        signInView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, SignUp.class);
                startActivity(i);
            }
        });
        
        forgotPassView = findViewById(R.id.forgotPassTV);
        forgotPassView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, ForgotPassword.class);
                startActivity(i);
            }
        });

        loginBtn = findViewById(R.id.logInBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString().trim();
                String password = passwrdET.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show loader and disable login button
                progressBar.setVisibility(View.VISIBLE);
                loginBtn.setEnabled(false);

                LoginRequest request = new LoginRequest(email, password);
                ApiService api = ApiClient.getClient().create(ApiService.class);

                api.login(request).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        // Hide loader and enable login button
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse res = response.body();
                            if (res.isSuccess()) {
                                Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                // Save login info
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("email", email);
                                editor.putBoolean("isLoggedIn", true); // mark as logged in
                                editor.apply();

                                // Redirect to Main Activity
                                Intent i = new Intent(Login.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(Login.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {

                            Toast.makeText(Login.this, "Login failed. Code" + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure (Call<LoginResponse> call, Throwable t) {
                        // Hide loader and enable login button
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);

                        Toast.makeText(Login.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}