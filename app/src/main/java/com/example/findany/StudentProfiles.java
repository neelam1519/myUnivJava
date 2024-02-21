package com.example.findany;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findany.adapter.RecyclerviewAdapterStudent;
import com.example.findany.model.ModelClassStudent;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentProfiles extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    List<ModelClassStudent> userdetails;
    RecyclerviewAdapterStudent adapter;
    SharedPreferences sharedPreferences;
    String documentname;
    ImageView actionAddButton;
    EditText editTextSearch;
    boolean hasDetails;
    SharedPreferences.Editor editor;
    Dialog loadingdialog;
    private boolean isSearchBarVisible = false;
    ImageView actionSearchButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profiles);

        userdetails = new ArrayList<>();

        initializeviews();

        editTextSearch.setVisibility(View.GONE);

        initRecyclerView();
        setupActionAddButton();
        initData();

        // Add TextWatcher to the search input field
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Call the filter method of the adapter when the user types
                adapter.filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        actionSearchButton.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (isSearchBarVisible) {
                // If search bar is visible, hide it
                editTextSearch.setVisibility(View.GONE);
                editTextSearch.setText(""); // Clear the search query
                adapter.filter(""); // Reset the adapter with original data
                isSearchBarVisible = false;

                // Hide the keyboard
                if (imm != null) {
                    imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                }
            } else {
                // If search bar is not visible, show it
                editTextSearch.setVisibility(View.VISIBLE);
                editTextSearch.requestFocus();
                isSearchBarVisible = true;

                // Show the keyboard
                if (imm != null) {
                    imm.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

    }
    private void initData() {
        userdetails = new ArrayList<>();
        Log.d("Debug", "Initiating Data");
        DialogUtils.showLoadingDialog(loadingdialog);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference studentResumeRef = db.collection("StudentResumeDetails");

        Log.d("Debug", "Connected to Firestore");

        studentResumeRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                DialogUtils.hideLoadingDialog(loadingdialog);
                Log.e("Debug", "SnapshotListener error", error);
                Toast.makeText(StudentProfiles.this, "Unable to get the details", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && !snapshot.isEmpty()) {
                Log.d("Debug", "Snapshot received with " + snapshot.size() + " documents");
                userdetails.clear();
                for (QueryDocumentSnapshot document : snapshot) {
                    Log.d("Debug", "Processing document: " + document.getId());

                    String name = document.getString("FullName");
                    String year = document.getString("year");
                    String branch = document.getString("branch");
                    String imageUrl = document.getString("ImageUrl");
                    String Regno = document.getString("RegNo");
                    String projects = document.getString("projects");
                    String skills = document.getString("skills");

                    ModelClassStudent student = new ModelClassStudent(name, year, branch, imageUrl, Regno, skills, projects);
                    Log.d("Debug", "Student object created: " + student.getName());

                    userdetails.add(student);
                }

                adapter = new RecyclerviewAdapterStudent(userdetails);
                recyclerView.setAdapter(adapter);
            } else {
                Log.d("Debug", "Snapshot is empty");
                userdetails.clear();
                adapter = new RecyclerviewAdapterStudent(userdetails);
                recyclerView.setAdapter(adapter);
            }
            DialogUtils.hideLoadingDialog(loadingdialog);
        });

        studentResumeRef.document(documentname).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Log.d("Debug", "Document exists: " + document.getId());
                    hasDetails = true;
                } else {
                    Log.d("Debug", "Document does not exist");
                    hasDetails = false;
                }
                refreshActionAddButton();
            } else {
                Log.e("Debug", "Error getting document", task.getException());
                hasDetails = false;
                Toast.makeText(StudentProfiles.this, "Create your profile", Toast.LENGTH_SHORT).show();
            }

            editor.putBoolean("hasDetails", hasDetails);
            editor.apply();
        });
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        Log.d("DEBUG", "RecyclerView initialized");
    }

    private void setupActionAddButton() {
        if (actionAddButton != null) {
            actionAddButton.setOnClickListener(v -> {
                Log.d("DEBUG", "Add/Edit button clicked");
                Intent intent = new Intent(StudentProfiles.this, studentresume.class);
                intent.putExtra("hadDetails",hasDetails);
                startActivity(intent);
            });
        } else {
            Log.e("DEBUG", "No edit/add button found");
            Toast.makeText(StudentProfiles.this, "No edit/add button", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshActionAddButton() {
        if (hasDetails) {
            Log.d("DEBUG", "Updating Add/Edit button to Edit icon");
            actionAddButton.setImageResource(R.drawable.edit_foreground);
        } else {
            Log.d("DEBUG", "Updating Add/Edit button to Add icon");
            actionAddButton.setImageResource(R.drawable.add);
        }
    }

    private void initializeviews(){
        actionAddButton = findViewById(R.id.action_add);
        editTextSearch = findViewById(R.id.editTextSearch);
        actionAddButton.setImageResource(R.drawable.add);
        actionSearchButton = findViewById(R.id.action_search);
        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        documentname = sharedPreferences.getString("RegNo", "1");

        loadingdialog=DialogUtils.createLoadingDialog(this);

    }
}
