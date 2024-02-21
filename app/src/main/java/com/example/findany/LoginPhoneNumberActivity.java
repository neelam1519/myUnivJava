package com.example.findany;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPhoneNumberActivity extends AppCompatActivity {
    EditText phoneInput;
    Button sendOtpBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_number);

        phoneInput = findViewById(R.id.login_mobile_number);
        sendOtpBtn = findViewById(R.id.send_otp_btn);
        progressBar = findViewById(R.id.login_progress_bar);

        progressBar.setVisibility(View.GONE);

        sendOtpBtn.setOnClickListener((v) -> {

            // Get the phone number
            String phoneNumber = phoneInput.getEditableText().toString();
            Toast.makeText(this,"Sending OTP", Toast.LENGTH_SHORT).show();

            // Generate OTP (you can use a library or generate it manually)
            String otp = generateOTP();

            // Send OTP via SMS
            sendOtp(phoneNumber, otp);

            // Wait for a short delay before starting the OTP verification activity
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                // Start the OTP verification activity
                Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOtpActivity.class);
                intent.putExtra("phone", phoneNumber);
                intent.putExtra("otp", otp);  // Pass the OTP to the OTP verification activity
                startActivity(intent);
                finish();

            }, 2000);
        });
    }

    private void sendOtp(String phoneNumber, String otp) {
        sendsms sendSmsTask = new sendsms(phoneNumber, "Your OTP is: " + otp);
        sendSmsTask.execute();
    }

    private String generateOTP() {

        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }
}
