package com.example.findany;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findany.Firebase.Firestore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1;
    private static final String DOMAIN = "@klu.ac.in";
    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    MaterialButton login, register, forgotPassword;
    TextInputLayout inputEmail, inputPassword;
    ImageView googleSignIn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        forgotPassword = findViewById(R.id.forgotpassword);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        googleSignIn = findViewById(R.id.googlesignin);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        progressBar.setVisibility(View.GONE);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String email = inputEmail.getEditText().getText().toString();
                String password = inputPassword.getEditText().getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Hide the progress bar
                } else {
                    emailPasswordSignIn(email, password);
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Log.d(TAG, "Register button clicked. Starting RegistrationActivity.");
                    Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                    startActivity(intent);

            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, Forgotpassword.class);
                    startActivity(intent);

            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    signIn();
            }
        });
    }
    void emailPasswordSignIn(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                updateUI(user);
                            } else {
                                Toast.makeText(LoginActivity.this, "Email not verified. Please check your email.", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        } else {
                            handleSignInFailure(task);
                        }
                    }
                });
    }

    // Method to handle sign-in failure
    private void handleSignInFailure(Task<AuthResult> task) {
        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(LoginActivity.this, "No account found with this email. Redirecting to register.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
            progressBar.setVisibility(View.GONE);
        } else {
            Log.w(TAG, "signInWithEmail:failure", task.getException());
            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Method called after Google sign-in activity completes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.VISIBLE);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    // Method to authenticate with Firebase using Google credentials
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            handleSignInFailure(task);
                        }
                    }
                });
    }

    void updateUI(FirebaseUser user) {
        if (user != null) {
            String uid = user.getUid();
            // Save user details to SharedPreferences
            saveUserDetailsToSharedPreferences(user);
            // Check if the email is valid and start the appropriate activity
            startAppropriateActivity(user.getEmail());
        }
    }

    // Method to save user details to SharedPreferences
    private void saveUserDetailsToSharedPreferences(FirebaseUser user) {
        SharedPreferences preferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("FullName", extractName(user.getDisplayName()));
        editor.putString("Mail", user.getEmail());
        editor.putString("RegNo", removeDomainFromEmail(user.getEmail()));
        editor.putString("UID", user.getUid());
        editor.putString("MobileNumber", user.getPhoneNumber());
        editor.putString("ImageUrl", String.valueOf(user.getPhotoUrl()));
        editor.apply();
    }

    // Method to extract the name from the display name
    public static String extractName(String displayName) {
        if (displayName != null && !displayName.isEmpty()) {
            String modifiedName = displayName.replaceAll("CSEUG.*|20.*", "").trim();
            return modifiedName;
        } else {
            return null;
        }
    }

    // Method to remove the domain from the email
    private String removeDomainFromEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex != -1) {
            return email.substring(0, atIndex);
        } else {
            return email;
        }
    }

    // Method to check if the email is valid and start the appropriate activity
    private void startAppropriateActivity(String email) {
        if (isValidEmail(email)) {
            updateFCMToken(removeDomainFromEmail(email));
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            progressBar.setVisibility(View.GONE);
        } else {
            Intent intent = new Intent(LoginActivity.this, OtherLogins.class);
            startActivity(intent);
            updateFCMToken(removeDomainFromEmail(email));
            progressBar.setVisibility(View.GONE);
        }
    }

    // Method to check if the email is valid
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@klu\\.ac\\.in$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Method called when the activity starts
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Checking Firebase user");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onStart: User is not null");
            if (currentUser.isEmailVerified()) {
                Log.d(TAG, "onStart: User email is verified");
                openActivity(currentUser);
            } else {
                Log.d(TAG, "onStart: User email is not verified");
            }
        } else {
            Log.d(TAG, "onStart: User is null");
        }
        Log.d(TAG, "onStart: Finishing activity");
    }

    // Method to open the appropriate activity
    private void openActivity(FirebaseUser user) {
        if (user != null) {
            startAppropriateActivity(user.getEmail());
        }
    }

    private void updateFCMToken(String documentName) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String newToken = task.getResult();

                        Log.d(TAG, "Document Name: " + documentName + "  New Token: " + newToken);
                        Firestore.updateFieldInAllDocuments("TOKENS", documentName, newToken);

                    } else {
                        // Handle the failure to retrieve the FCM token
                        Log.e(TAG, "Failed to get FCM token during onResume", task.getException());
                    }
                });
    }
}
