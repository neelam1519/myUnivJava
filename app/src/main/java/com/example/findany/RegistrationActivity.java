package com.example.findany;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";
    private MaterialButton btnRegister, btnLinkToLogin;
    private TextInputLayout inputEmail, inputPassword;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        btnRegister = findViewById(R.id.register);
        btnLinkToLogin = findViewById(R.id.button_login);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = inputEmail.getEditText().getText().toString();
                String password = inputPassword.getEditText().getText().toString();

                // Check if email or password is empty
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    // Show a toast message
                    Toast.makeText(RegistrationActivity.this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Hide the progress bar
                } else if (password.length() < 8) {
                    // Check if the password length is less than 8 characters
                    Toast.makeText(RegistrationActivity.this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Hide the progress bar
                } else {
                    // Check if the email already exists before registering
                    checkEmailExistsAndRegister(email, password);
                }
            }
        });


        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerUserUsingEmailAndPassword(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null && !user.isEmailVerified()) {
                                progressBar.setVisibility(View.VISIBLE);
                                sendVerificationEmail(user);
                            } else if (user == null) {
                                showToast("Registration failed. Try again.");
                            } else {
                                navigateToHomeScreen();
                            }
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            progressBar.setVisibility(View.GONE);
                            showToast("Authentication failed. " + task.getException().getMessage());

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Registration Error: " + e.getMessage());
                        progressBar.setVisibility(View.GONE);
                        showToast("Registration Error: " + e.getMessage());

                    }
                });
    }

    private void checkEmailExistsAndRegister(String email, String password) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                                // Email already exists
                                showToast("Email address is already in use with a different sign-in method. Please use a different email.");
                                progressBar.setVisibility(View.GONE);
                            } else {
                                registerUserUsingEmailAndPassword(email, password);
                            }
                        } else {
                            // Error checking email existence
                            Log.e(TAG, "Error checking email existence: " + task.getException().getMessage());
                            showToast("Error checking email existence. Please try again.");
                        }
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            showToast("Email verification sent. Please check your email.");
                            navigateToLoginActivity();
                        } else {
                            // Failed to send verification email
                            Log.e(TAG, "Failed to send verification email", task.getException());
                            progressBar.setVisibility(View.GONE);
                            showToast("Failed to send verification email. " + task.getException().getMessage());
                        }
                    }
                });
    }


    private void navigateToHomeScreen() {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void navigateToLoginActivity(){
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
