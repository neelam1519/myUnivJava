package com.example.findany;

import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Resume extends AppCompatActivity {
    ImageView imageurl;
    TextView name,email,skills,projects,year,branch,regno,contactdetails,proofofwork,description;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume);

        initializeviews();

        String regno = getIntent().getStringExtra("RegNo");

        read2firebase(regno);
    }
    public void read2firebase(String documentname) {

        // Fetch the document
        db.collection("StudentResumeDetails").document(documentname).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Log.d("FirestoreDebug", "Document exists. Parsing data...");

                            // Safely get the document fields
                            String rname = safeGetString(document, "FullName");
                            String rmail = safeGetString(document, "Mail");
                            String ryear = safeGetString(document, "year");
                            String rbranch = safeGetString(document, "branch");
                            String rprojects = safeGetString(document, "projects");
                            String rskills = safeGetString(document, "skills");
                            String rimageurl = safeGetString(document, "ImageUrl");
                            String rdescription = safeGetString(document, "description");
                            String rregno = safeGetString(document, "RegNo");
                            String rcontactdetails = safeGetString(document, "ContactDetails");
                            String rproofofwork = safeGetString(document, "ProofOfWork");
                            populateUI(rname, rmail, ryear, rbranch, rprojects, rskills, rimageurl, rdescription, rregno, rcontactdetails, rproofofwork);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Log.e("FirestoreDebug", "Document does not exist");
                            Toast.makeText(Resume.this, "Data is not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("FirestoreDebug", "Read operation failed: " + task.getException());
                        Toast.makeText(Resume.this, "Unable to get details", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Exception occurred: " + e.getMessage());
                    Toast.makeText(Resume.this, "Error", Toast.LENGTH_SHORT).show();
                });
    }

    private String safeGetString(DocumentSnapshot document, String key) {
        String value = document.getString(key);
        Log.d("FirestoreDebug", key + ": " + value);
        return (value != null && !value.isEmpty()) ? value : "NA";
    }

    private void populateUI(String rname, String rmail, String ryear, String rbranch, String rprojects, String rskills, String rimageurl, String rdescription, String rregno, String rcontactdetails, String rproofofwork) {
        // Set the text fields
        name.setText(rname);
        email.setText(rmail);
        year.setText(ryear);
        branch.setText(rbranch);
        projects.setText(rprojects);
        skills.setText(rskills);
        description.setText(rdescription);
        regno.setText(rregno);
        contactdetails.setText(rcontactdetails);
        proofofwork.setText(rproofofwork);

        // Make URLs clickable
        Linkify.addLinks(proofofwork, Linkify.WEB_URLS);
        Linkify.addLinks(contactdetails, Linkify.WEB_URLS);

        // Load the image
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.defaultprofile)
                .error(R.drawable.defaultprofile)
                .apply(RequestOptions.circleCropTransform());

        Glide.with(Resume.this)
                .load(rimageurl)
                .apply(requestOptions)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageurl);
    }
    private void initializeviews(){
        imageurl=findViewById(R.id.profileImageView);
        name=findViewById(R.id.resumename);
        email=findViewById(R.id.resumeemail);
        skills=findViewById(R.id.skillsTextView);
        projects=findViewById(R.id.experienceTextView);
        year=findViewById(R.id.yearTextView);
        branch=findViewById(R.id.branchTextView);
        description=findViewById(R.id.discriptionTextView);
        regno=findViewById(R.id.regno);
        contactdetails=findViewById(R.id.contactTextView);
        proofofwork=findViewById(R.id.proofofworkTextView);
        progressBar=findViewById(R.id.progressBar);
    }
}