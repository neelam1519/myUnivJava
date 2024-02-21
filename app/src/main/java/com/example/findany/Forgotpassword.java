package com.example.findany;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class Forgotpassword extends AppCompatActivity {
    String TAG = "ForgotPassword";
    TextInputLayout inputemail;
    MaterialButton resetlink;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        inputemail = findViewById(R.id.resetmail);
        resetlink = findViewById(R.id.btnVerify);

        mAuth = FirebaseAuth.getInstance();

        resetlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputemail.getEditText().getText().toString().trim();
                if (!email.isEmpty()) {
                    sendResetEmail(email);
                } else {
                    Toast.makeText(Forgotpassword.this, "Enter your email to reset your password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Forgotpassword.this, "Password reset email sent to your email address", Toast.LENGTH_SHORT).show();
                            // You can navigate to another screen or perform other actions here
                        } else {
                            Toast.makeText(Forgotpassword.this, "Failed to send reset email. Check your email address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error sending reset email: " + e.getMessage());
                        Toast.makeText(Forgotpassword.this, "Error sending reset email. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}