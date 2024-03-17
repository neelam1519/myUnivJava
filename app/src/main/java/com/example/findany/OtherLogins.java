package com.example.findany;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

//Other logins when the user logged in with the other emails other than klu mail id
public class OtherLogins extends AppCompatActivity {

    String TAG="OtherLogins";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    String email;
    String documentname;
    Button signout;
    private static final int REQUEST_MEDIA_PERMISSIONS = 1;
    private final String[] permissionsToRequest = new String[]{
            android.Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            "android.permission.POST_NOTIFICATIONS" // Permission string for POST_NOTIFICATIONS
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_logins);

        checkAndRequestPermissions();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        email = sharedPreferences.getString("email", "");

        signout=findViewById(R.id.osignout);

        // Check if the user is logged in
        if (TextUtils.isEmpty(email)) {
            Log.d("OtherLogin","Email is empty");
        } else {
            String sanitizedEmail = email.replaceAll("[.#$\\[\\]]", "");
            int atIndex = sanitizedEmail.indexOf('@');
            if (atIndex != -1) {
                documentname = sanitizedEmail.substring(0, atIndex);
                editor.putString("documentname", documentname); // Save the documentname in SharedPreferences
                editor.apply(); // Apply the changes to SharedPreferences
                getAndAddUserTokenToGroupChatAll();
            }
        }

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutAndRedirectToLogin();
            }
        });


        // Find the ImageView elements
        ImageView booksImageView = findViewById(R.id.books);
        ImageView groupChatImageView = findViewById(R.id.groupchat);
        ImageView suggestionsImageView = findViewById(R.id.suggestions);

        // Set OnClickListener for Books ImageView
        booksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent materials = new Intent(OtherLogins.this, materials_years.class);
                startActivity(materials);
            }
        });

        // Set OnClickListener for Group Chat ImageView
        groupChatImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupchat = new Intent(OtherLogins.this, Group_Chat.class);
                startActivity(groupchat);
            }
        });

        // Set OnClickListener for Suggestions ImageView
        suggestionsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sugesstions = new Intent(OtherLogins.this, suggestions.class);
                startActivity(sugesstions);
            }
        });
    }
    private void checkAndRequestPermissions() {
        boolean allPermissionsGranted = true;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            List<String> permissionsToAsk = new ArrayList<>();

            for (String permission : permissionsToRequest) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToAsk.add(permission);
                    allPermissionsGranted = false;
                }
            }

            if (!permissionsToAsk.isEmpty()) {
                String[] permissionsArray = permissionsToAsk.toArray(new String[0]);
                ActivityCompat.requestPermissions(this, permissionsArray, REQUEST_MEDIA_PERMISSIONS);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    "android.permission.POST_NOTIFICATIONS"
            }, REQUEST_MEDIA_PERMISSIONS);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // Close all activities in the task and exit the app
    }

    private void getAndAddUserTokenToGroupChatAll() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String userToken = task.getResult();

                        SharedPreferences tokenref=getSharedPreferences("token",MODE_PRIVATE);
                        SharedPreferences.Editor tokeneditor=tokenref.edit();
                        tokeneditor.putString("token",userToken);
                        tokeneditor.apply();

                        // Add the user token to the "groupchat/all" node in the Firebase Realtime Database
                        DatabaseReference groupChatAllRef = FirebaseDatabase.getInstance().getReference("GroupChatToken/ALL/"+documentname);
                        groupChatAllRef.child("token").setValue(userToken)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User token added to groupchat/all successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to add user token to groupchat/all: " + e.getMessage(), e);
                                });
                    } else {
                        Log.e(TAG, "Failed to get user token: " + task.getException());
                    }
                });
    }
    private void signOutAndRedirectToLogin() {
        GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // Delete UID from tokenref database reference
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference tokenref = FirebaseDatabase.getInstance().getReference("GroupChatToken/ALL/"+documentname);
            tokenref.child("token").removeValue();
        }

        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(OtherLogins.this, task -> {
            // Navigate back to LoginActivity
            Intent intent = new Intent(OtherLogins.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_MEDIA_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted
            } else {
                // Permissions not granted

            }
        }
    }

}


