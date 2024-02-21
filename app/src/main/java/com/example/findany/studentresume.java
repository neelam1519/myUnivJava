package com.example.findany;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findany.Firebase.Firestore;
import com.example.findany.callbacks.FirestoreCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class studentresume extends AppCompatActivity {

    String fullname, email, year, branch, section, documentName, imageUrl;
    EditText skills, projects, description, contactdetails, proofofwork;
    Button update,delete;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    DocumentReference docRef;
    Boolean hasDetails;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studentresume);

        initalizeviews();

        readFromFirebase(documentName);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                updatedata();
            }
        });

        setDeleteVisibility(hasDetails);
    }
    private void updatedata(){
        String userSkills = skills.getText().toString().toUpperCase();
        String userProjects = projects.getText().toString();
        String userDescription = description.getText().toString().toUpperCase();
        String contactdetail = contactdetails.getText().toString();
        String proofwork = proofofwork.getText().toString();

        if(!userSkills.isEmpty() && !userDescription.isEmpty() && !contactdetail.isEmpty()) {
            if(userDescription.length()>=30) {

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                writeOrUpdateFirebaseDocument(userSkills, userProjects, userDescription, contactdetail, proofwork,true);
                            } else {
                                writeOrUpdateFirebaseDocument(userSkills, userProjects, userDescription, contactdetail, proofwork,false);
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(studentresume.this, "Upload your details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(studentresume.this, "Description must be at least 30 words", Toast.LENGTH_SHORT).show();
            }
        }else{
            progressBar.setVisibility(View.GONE);
            Toast.makeText(studentresume.this, "The required fields are empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeOrUpdateFirebaseDocument(String skills, String projects, String description, String contactdetail, String proofwork, boolean isUpdate) {
        Toast.makeText(this, "Updating details....", Toast.LENGTH_SHORT).show();

        Map<String, Object> data = new HashMap<>();
        data.put("FullName", fullname);
        data.put("Mail", email);
        data.put("year", year);
        data.put("branch", branch);
        data.put("skills", skills);
        data.put("projects", projects);
        data.put("description", description);
        data.put("RegNo", documentName);
        data.put("ImageUrl", imageUrl);
        data.put("section", section);
        data.put("ContactDetails", contactdetail);
        data.put("ProofOfWork", proofwork);

        Firestore.uploadData(data, "StudentResumeDetails", documentName, new FirestoreCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(studentresume.this, "Details are uploaded", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                Intent intent =new Intent(studentresume.this,StudentProfiles.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(studentresume.this, "There is some error in details uploading", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void setDeleteVisibility(boolean hasDetails) {
        int visibility = hasDetails ? View.VISIBLE : View.GONE;
        delete.setVisibility(visibility);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasDetails) {
                    Firestore.deleteDocument("StudentResumeDetails",documentName);
                }
            }
        });

    }

    public void readFromFirebase(String documentName) {

        // Get a reference to the document
        docRef = db.collection("StudentResumeDetails").document(documentName);

        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Document with the given documentName exists, retrieve and display data
                            String rskills = document.getString("skills");
                            String rprojects = document.getString("projects");
                            String rdescription = document.getString("description");
                            String rcontactdetails = document.getString("ContactDetails");
                            String rproofs = document.getString("ProofOfWork");

                            skills.setText(rskills);
                            projects.setText(rprojects);
                            description.setText(rdescription);
                            contactdetails.setText(rcontactdetails);
                            proofofwork.setText(rproofs);
                        } else {
                            // Document with the given documentName does not exist, show toast
                            Toast.makeText(studentresume.this, "Create your profile", Toast.LENGTH_SHORT).show();
                            // You can also set default values or clear fields if needed
                            skills.setText("");
                            projects.setText("");
                            description.setText("");
                            contactdetails.setText("");
                            proofofwork.setText("");
                        }
                    } else {
                        // Error occurred while retrieving data, show toast
                        Toast.makeText(studentresume.this, "Task failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Failure occurred, show toast
                    Toast.makeText(studentresume.this, "Error", Toast.LENGTH_SHORT).show();
                });
    }

    private void initalizeviews(){
        skills = findViewById(R.id.editTextSkills);
        projects = findViewById(R.id.editTextProjects);
        description = findViewById(R.id.editTextDescription);
        update = findViewById(R.id.buttonUpload);
        delete=findViewById(R.id.delete);
        contactdetails=findViewById(R.id.editTextContactDetails);
        proofofwork=findViewById(R.id.editTextProofs);
        progressBar=findViewById(R.id.progressBar);
        hasDetails=getIntent().getBooleanExtra("hadDetails",false);

        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        documentName = sharedPreferences.getString("RegNo", "");
        email = sharedPreferences.getString("Mail", "");
        fullname=sharedPreferences.getString("FullName","");
        year=sharedPreferences.getString("year","");
        branch=sharedPreferences.getString("branch","");
        imageUrl=sharedPreferences.getString("ImageUrl","");
        section=sharedPreferences.getString("section","");

        docRef = db.collection("StudentResumeDetails").document(documentName);
    }
}