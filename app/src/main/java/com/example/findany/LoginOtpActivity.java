package com.example.findany;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginOtpActivity extends AppCompatActivity {

    String phoneNumber, otp;
    EditText otpInput;
    Button nextBtn;
    ProgressBar progressBar;
    TextView resendOtpTextView;

    private CountDownTimer countDownTimer;
    private final long startTimeMillis = 20000; // 30 seconds
    private final long intervalMillis = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp);

        otpInput = findViewById(R.id.login_otp);
        nextBtn = findViewById(R.id.login_next_btn);
        progressBar = findViewById(R.id.login_progress_bar);
        resendOtpTextView = findViewById(R.id.resend_otp_textview);

        phoneNumber = getIntent().getExtras().getString("phone");
        otp = getIntent().getExtras().getString("otp");

        startTimer();

        nextBtn.setOnClickListener(v -> {
            String enteredOtp = otpInput.getText().toString();

            if (!enteredOtp.isEmpty()) {
                if (enteredOtp.equals(otp)) {
                    Toast.makeText(this, "OTP verified successfully", Toast.LENGTH_SHORT).show();

                    SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("MobileNumber", phoneNumber);
                    editor.apply();

                    // Cancel the timer when OTP is verified
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                    Intent intent = new Intent(LoginOtpActivity.this, UserAccount.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Incorrect OTP. Please try again", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Empty OTP
                Toast.makeText(this, "Enter the OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(startTimeMillis, intervalMillis) {
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                resendOtpTextView.setText("Resend OTP in " + secondsRemaining + " sec");
            }

            public void onFinish() {
                resendOtpTextView.setText("Resend OTP");
                resendOtpTextView.setOnClickListener(v -> {

                    Intent intent=new Intent(LoginOtpActivity.this,LoginPhoneNumberActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the timer to avoid memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
