package com.example.findany;

import static com.example.findany.utils.utils.isConnectedToInternet;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.findany.Firebase.Firestore;
import com.example.findany.callbacks.FirestoreCallback;
import com.example.findany.utils.ClearData;
import com.example.findany.utils.sharedprefs;
import com.example.findany.utils.utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SharedPreferences UserDetailsPrefs,settingsprefs;
    private SharedPreferences.Editor UserDetailseditor;
    Boolean timetableswitch;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    String documentname;
    NavigationView navigationView;
    ImageView navimageview;
    TextView navtextview;
    Boolean Detailsuplaoded=false;
    private FirebaseFirestore db;
    ImageView materials,studentprofiles,groupchat,soc;
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 2;
    Context context=this;
    ProgressBar progressBar;
    private int foregroundActivities = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeviews();
        navigation();
        ClickListners();
        requestNotificationPermission();
        Firestore.CurrentTimestamp();
        checkAndUpdateFCMToken();

        if(isConnectedToInternet(context)){
            Firestore.getAllDataFromDocument(this, "UserDetails", documentname, new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    Detailsuplaoded=areRequiredFieldsPresent(UserDetailsPrefs);
                    updateImageViewTransparency(Detailsuplaoded);
                    navImageAndUsername();
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG,"DetailsUploaded:   "+e.getMessage());
                    Detailsuplaoded=areRequiredFieldsPresent(UserDetailsPrefs);
                    updateImageViewTransparency(Detailsuplaoded);
                    progressBar.setVisibility(View.GONE);
                    navImageAndUsername();
                }
            });
        }else{
            Detailsuplaoded=areRequiredFieldsPresent(UserDetailsPrefs);
            updateImageViewTransparency(Detailsuplaoded);
            navImageAndUsername();
            progressBar.setVisibility(View.GONE);
        }



        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (timetableswitch) {
            Log.d("MainActivity", "onCreate: Sending broadcast"); // Add this log
            Intent intents = new Intent(this, NotificationReceiver.class);
            intents.setAction("com.neelam.findany.NOTIFY");
            sendBroadcast(intents);
        } else {
            // Initialize AlarmManager
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // Initialize your pending intent
            Intent intents = new Intent("com.neelam.findany.NOTIFY");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 143, intents, PendingIntent.FLAG_IMMUTABLE); // Added FLAG_IMMUTABLE

            // Cancel the alarm
            if (alarmManager != null) {
                Log.d("MainActivity", "onCreate: Canceling broadcast alarm"); // Add this log
                alarmManager.cancel(pendingIntent);
            }
        }

    }
    private void checkAndUpdateFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String newToken = task.getResult();

                        // Compare the new token with the stored token in SharedPreferences or elsewhere
                        String storedToken = getStoredTokenFromSharedPreferences();

                        if (storedToken == null || storedToken.isEmpty()) {
                            // If the stored token is null or empty, update Firestore with the new token
                            updateFCMTokenInFirestore(newToken);
                        } else if (!newToken.equals(storedToken)) {
                            // The token has changed, update Firestore
                            updateFCMTokenInFirestore(newToken);
                        }
                    } else {
                        // Handle the failure to retrieve the FCM token
                        Log.e(TAG, "Failed to get FCM token during onResume", task.getException());
                    }
                });
    }
    private String getStoredTokenFromSharedPreferences() {
        // Implement your logic to retrieve the stored token from SharedPreferences or elsewhere
        return UserDetailsPrefs.getString("token", "");
    }

    private void updateFCMTokenInFirestore(String newToken) {
        String documentName = UserDetailsPrefs.getString("RegNo", "");

        Log.d(TAG, "Document Name: " + documentName + "  New Token: " + newToken);

        // Update FCM token in Firestore
        Firestore.updateFieldInAllDocuments("TOKENS", documentName, newToken);

        // Store the new token for future comparisons
        storeTokenInSharedPreferences(newToken);
    }

    private void storeTokenInSharedPreferences(String newToken) {
        // Implement your logic to store the token in SharedPreferences or elsewhere

        UserDetailseditor.putString("FCMToken", newToken);
        UserDetailseditor.apply();
    }

    private void getFcmToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        sharedprefs.saveValueToPrefs(context,"UserDetails","token",token);
                        Map<String,String> data=new HashMap<>();
                        data.put(documentname,token);
                        Firestore.storeDataInFirestore("TOKENS","ALL",data);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean areRequiredFieldsPresent(SharedPreferences sharedPreferences) {
        // Check if all required fields are present and not empty
        return isFieldNotEmpty(sharedPreferences, "FullName")
                && isFieldNotEmpty(sharedPreferences, "MobileNumber")
                && isFieldNotEmpty(sharedPreferences, "RegNo")
                && isFieldNotEmpty(sharedPreferences, "Mail");
    }

    private boolean isFieldNotEmpty(SharedPreferences sharedPreferences, String key) {
        // Check if the field is present and not empty
        return sharedPreferences.contains(key) && !TextUtils.isEmpty(sharedPreferences.getString(key, ""));
    }


    private static final float FULL_OPACITY = 1f;
    private static final float PARTIAL_OPACITY = 0.4f;
    private void updateImageViewTransparency(boolean detailsUploaded) {
        float alpha = detailsUploaded ? FULL_OPACITY : PARTIAL_OPACITY;
        studentprofiles.setAlpha(alpha);
        groupchat.setAlpha(alpha);
        materials.setAlpha(alpha);
    }

    private void initializeviews(){

        UserDetailsPrefs =getSharedPreferences("UserDetails", MODE_PRIVATE);
        UserDetailseditor = UserDetailsPrefs.edit();
        documentname=UserDetailsPrefs.getString("RegNo","Document not found");

        settingsprefs=getSharedPreferences("settings",MODE_PRIVATE);
        timetableswitch=settingsprefs.getBoolean("timetableswitch",true);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        drawerLayout = findViewById(R.id.drawer_layout1);
        navigationView = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);

        View header = navigationView.getHeaderView(0);
        navimageview = header.findViewById(R.id.imageView);
        navtextview = header.findViewById(R.id.username);

        materials = findViewById(R.id.materials);
        studentprofiles = findViewById(R.id.studentprofiles);
        groupchat =findViewById(R.id.groupchat);
        progressBar=findViewById(R.id.loadingProgressBar);
        soc=findViewById(R.id.socimage);

        db = FirebaseFirestore.getInstance();

        Firestore.getFieldValueAndSaveToPrefs(context,"APIKEYS","APIKEYS","FCM_SERVER_KEY","APIKEYS","FCMKEY");

    }
    private void ClickListners() {
        studentprofiles.setOnClickListener(v -> {
            if (isConnectedToInternet(context)) {
                if (Detailsuplaoded) {
                    Intent intent = new Intent(this, StudentProfiles.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Upload all your details in your account to access this", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Connect to the internet and try again", Toast.LENGTH_SHORT).show();
            }
        });

        materials.setOnClickListener(v -> {
            if (isConnectedToInternet(context)) {
                if (Detailsuplaoded) {
                    Intent intent = new Intent(this, materials_years.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Upload all your details in your account to access this", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Connect to the internet and try again", Toast.LENGTH_SHORT).show();
            }

        });

        groupchat.setOnClickListener(v -> {
            if (isConnectedToInternet(context)) {
                if (Detailsuplaoded) {
                    Intent intent = new Intent(this, Group_Chat.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Upload all your details in your account to access this", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Connect to the internet and try again", Toast.LENGTH_SHORT).show();
            }
        });

        soc.setOnClickListener(v -> {
            if (isConnectedToInternet(context)) {
                if (Detailsuplaoded) {
                    Intent intent = new Intent(this, soc_main.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Upload all your details in your account to access this", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Connect to the internet and try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navImageAndUsername() {

        // Fetch and Set Username from Shared Preferences
        String username = UserDetailsPrefs.getString("username", UserDetailsPrefs.getString("FullName", "Name not found"));
        navtextview.setText(username);

        String imageUrl= UserDetailsPrefs.getString("ImageUrl", "");

        Glide.with(MainActivity.this)
                .load(imageUrl)
                .error(R.drawable.defaultprofile)
                .placeholder(R.drawable.defaultprofile)
                .into(navimageview);
    }

    private void navigation(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.nav_youraccount) {
                    Intent useraccount=new Intent(MainActivity.this,UserAccount.class);
                    startActivity(useraccount);
                } else if (id == R.id.nav_review) {
                    Intent review=new Intent(MainActivity.this, suggestions.class);
                    startActivity(review);
                }else if(id == R.id.nav_logout){
                    utils.signOutAndRedirectToLogin(context);
                } else if (id==R.id.nav_settings) {
                    Intent review=new Intent(MainActivity.this,Settings.class);
                    startActivity(review);
                }
                DrawerLayout drawer = findViewById(R.id.drawer_layout1);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, REQUEST_CODE_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Notification permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClearData.clearCache(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ClearData.clearCache(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(isConnectedToInternet(this)) {
            getFcmToken();
            Firestore.getAllDataFromCollection(context, "BRANCHES", "BRANCHES", "OTHERDETAILS", "Changes", "detailsUpdatedTimestamp", "FirebaseData");
            Firestore.getAllDataFromCollection(context, "SECTIONS", "", "OTHERDETAILS", "Changes", "detailsUpdatedTimestamp", "FirebaseData");
            Firestore.getsubvalues(context, "ACADEMICDETAILS", "SLOTS", "CSE", "2", "OTHERDETAILS", "SLOTS_CSE_","FirebaseData");
            Firestore.getsubvalues(context, "ACADEMICDETAILS", "SLOTS", "CSE", "3", "OTHERDETAILS", "SLOTS_CSE_","FirebaseData");
            Firestore.getFileFromStorage(context, "SQLITE_FILE", "TIMETABLE.db", "FirebaseData");
        }

        ClearData.clearCache(this);
    }
}
