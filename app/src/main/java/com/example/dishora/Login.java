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
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.security.KeyStore;

public class Login extends AppCompatActivity {

    //
    private EditText emailET, passwrdET;
    private ImageView togglePassword;
    private TextView signInView, forgotPassView;
    private Button loginBtn;

    //SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPreferences";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

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
                Intent i = new Intent(Login.this, MainActivity.class);
                startActivity(i);
            }
        });

    }
}