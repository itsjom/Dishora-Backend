package com.example.dishora;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPassword extends AppCompatActivity {

    private View backBtnView;
    private Button sendEmailBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        backBtnView = findViewById(R.id.backButtonView);
        backBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();                 //closes the current activity
            }
        });
    }
}