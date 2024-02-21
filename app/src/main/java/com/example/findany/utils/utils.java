package com.example.findany.utils;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.example.findany.LoginActivity;
import com.example.findany.MainActivity;
import com.example.findany.OtherLogins;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class utils {
    private static GoogleSignInClient googleSignInClient;
    private static FirebaseAuth mAuth;
    List<File> selectedFiles= new ArrayList<>();

    public static void signOutAndRedirectToLogin(Context context) {
        sharedprefs.clearSharedPreferences(context, "UserDetails");

        mAuth = FirebaseAuth.getInstance();

        mAuth.signOut();

        googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN);

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            navigateToLoginActivity(context);
        });
    }

    public static void navigateToLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void navigateToMainActivity(Context context, String email) {
        // Extract the domain from the email address
        String[] emailParts = email.split("@");

        if (emailParts.length == 2) {
            String domain = emailParts[1].toLowerCase();

            // Check the domain and redirect accordingly
            if (domain.equals("klu.ac.in")) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, OtherLogins.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        } else {
            // Handle invalid email address
            Log.e(TAG, "Invalid email address: " + email);
            // You can add additional handling as needed
        }
    }

    public static void print(String TAG,String message){
        Log.d(TAG,message);
    }

    public static Boolean ColleageEmail(String email,String domain){

        if (email != null || email.endsWith(domain)) {
            return true;
        }
        return false;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ""; // Return an empty string for null or empty file names
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        // Check if the dot is not the first or last character in the file name
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1 && lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return ""; // Return an empty string if no valid extension is found
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities activeNetwork = cm.getNetworkCapabilities(cm.getActiveNetwork());
            boolean isConnected = activeNetwork != null &&
                    (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            if (!isConnected) {
                Log.d(TAG, "No network connectivity");
            }
            return isConnected;
        } else {
            Log.e(TAG, "Could not get Connectivity Manager");
            return false;
        }
    }

    public static String removeFileExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex > 0) {
            return fileName.substring(0, extensionIndex);
        }
        return fileName;
    }

    public static void redirectToMainActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public static void redirectToOtherLogin(Context context) {
        Intent intent = new Intent(context, OtherLogins.class);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public static String getPath(Context context, Uri uri) {
        String[] projection = {OpenableColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String name = cursor.getString(nameIndex);
            cursor.close();
            return name;
        }
        return null;
    }
}
