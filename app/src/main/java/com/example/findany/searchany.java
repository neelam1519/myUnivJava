package com.example.findany;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class searchany extends AppCompatActivity {
    private static final String TAG = "SearchAnyActivity";
    private ImageView searchIcon;
    private EditText searchEditText;
    private boolean isSearchVisible = false;
    private List<SearchResult> searchResults;
    private RecyclerView recyclerView;
    private SqliteHelper sqliteHelper;
    private String searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchany);

        initializeViews();
        dbFileDownload();
    }

    private void dbFileDownload() {
        final SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        final long lastUpdatedTime = preferences.getLong("LastUpdatedTimeAllBlocks", 0);

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference dbRef = mStorageRef.child("SQLITE_FILE/ALLBLOCKS.db");

        // Step 1: Fetch the metadata
        dbRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            long updatedTimeMillis = storageMetadata.getUpdatedTimeMillis();

            // Step 2: Compare with locally stored timestamp
            if (updatedTimeMillis > lastUpdatedTime) {
                File localFile = new File(getFilesDir().getParent() + "/databases/ALLBLOCKS.db");

                // Step 3: Download if newer
                dbRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    // Store the new timestamp
                    preferences.edit().putLong("LastUpdatedTime", updatedTimeMillis).apply();

                    // File downloaded and replaced successfully
                    Log.d(TAG, "DB file downloaded successfully");
                    taskFinished("dbFileDownload");

                }).addOnFailureListener(exception -> {
                    // Handle errors in downloading
                    Log.d(TAG, "DB file not downloaded successfully: " + exception.getMessage());
                    taskFinished("dbFileDownload");

                });
            } else {
                // No need to download, local db is up to date
                Log.d(TAG, "Local DB is up to date. Skipping download.");
                taskFinished("dbFileDownload");
            }
        }).addOnFailureListener(e -> {
            // Handle the error of fetching metadata
            Log.d(TAG, "Failed to fetch DB metadata: " + e.getMessage());
            taskFinished("dbFileDownload");
        });
    }

    private void taskFinished(String taskName) {
        // Implement your logic after the background task is finished
    }

    public void initializeViews() {
        searchIcon = findViewById(R.id.searchbtn);
        searchEditText = findViewById(R.id.search);
        searchEditText.setVisibility(View.GONE);

        sqliteHelper = new SqliteHelper(this);
        sqliteHelper.createDatabase();
    }
}
