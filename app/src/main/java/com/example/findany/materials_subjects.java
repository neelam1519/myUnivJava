package com.example.findany;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class materials_subjects extends AppCompatActivity {
    String branch, year, toolbartitle, specialization, storingname;
    androidx.appcompat.widget.Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.materials_subjects);

        Intent intent = getIntent();
        year = intent.getStringExtra("year"); //year
        branch = intent.getStringExtra("branch"); //branch
        specialization = intent.getStringExtra("specialization"); // specialization

        storingname=branch+specialization+year;

        if(isConnectedToInternet()){
            loadStoredSubjectNames();
            getSubjectNamesFromFirebase(year,branch,specialization);
        }else{
            loadStoredSubjectNames();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbartitle = year.replaceAll("\\s", "")+ "/" + branch.toUpperCase()+"/"+specialization.replaceAll("\\s", "");

        getSupportActionBar().setTitle(toolbartitle);

    }
    private void createDynamicButtons(String[] subjects) {
        LinearLayout linearLayout = findViewById(R.id.linearlayout);
        linearLayout.removeAllViews();

        SharedPreferences preferences = getSharedPreferences("yearstudy", Context.MODE_PRIVATE);
        Set<String> tickedSubjects = preferences.getStringSet("tickedSubjects", new HashSet<>());
        SharedPreferences.Editor editor = preferences.edit();

        for (String subject : subjects) {
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonLayout.setLayoutParams(layoutParams);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(tickedSubjects.contains(subject));
            buttonLayout.addView(checkBox);

            Button button = new Button(this);
            button.setText(subject);
            button.setOnClickListener(v -> {
                Intent intent = new Intent(materials_subjects.this, materials_units.class);
                intent.putExtra("year", year);
                intent.putExtra("branch", branch);
                intent.putExtra("specialization", specialization);
                intent.putExtra("subject", subject);
                startActivity(intent);
            });
            buttonLayout.addView(button);

            if (tickedSubjects.contains(subject)) {
                linearLayout.addView(buttonLayout, 0);
            } else {
                linearLayout.addView(buttonLayout);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tickedSubjects.add(subject);
                    if (buttonLayout.getParent() != null) {
                        ((ViewGroup) buttonLayout.getParent()).removeView(buttonLayout);
                    }
                    linearLayout.addView(buttonLayout, 0);
                } else {
                    tickedSubjects.remove(subject);
                    if (buttonLayout.getParent() != null) {
                        ((ViewGroup) buttonLayout.getParent()).removeView(buttonLayout);
                    }
                    linearLayout.addView(buttonLayout);
                }
                editor.putStringSet("tickedSubjects", tickedSubjects).apply();
            });
        }
    }


    private void getSubjectNamesFromFirebase(String year, String branch, String specialization) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String formattedYear = year.replaceAll("\\s", "");
        String formattedBranch = branch.replaceAll("\\s", "");
        String formattedSpecialization = specialization.replaceAll("\\s", "");

        String commonPath = String.format("/SUBJECTS/%s/%s/COMMON", formattedYear, formattedBranch);
        String specializationPath = String.format("/SUBJECTS/%s/%s/%s", formattedYear, formattedBranch, formattedSpecialization);

        List<String> combinedSubjectNames = new ArrayList<>();

        // Fetch common data
        fetchFromFirebase(db, commonPath, commonDocument -> {
            combinedSubjectNames.addAll(fetchSubjectsFromDocument(commonDocument));
            // Fetch specialization data
            fetchFromFirebase(db, specializationPath, specializationDocument -> {
                combinedSubjectNames.addAll(fetchSubjectsFromDocument(specializationDocument));
                handleCombinedSubjectNames(combinedSubjectNames);
            });
        });
    }

    private void fetchFromFirebase(FirebaseFirestore db, String path, Consumer<DocumentSnapshot> onSuccess) {
        db.document(path).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                onSuccess.accept(task.getResult());
            } else {
                showToast();
            }
        });
    }

    private void showToast() {
        Toast.makeText(this, "NO DATA IS AVAILABLE IF YOU KNOW PLEASE LET US KNOW IN REVIEW SECTION OR SEND US THROUGH MAIL", Toast.LENGTH_SHORT).show();
    }

    private void handleCombinedSubjectNames(List<String> combinedSubjectNames) {
        if (!combinedSubjectNames.isEmpty()) {
            SharedPreferences preferences = getSharedPreferences("yearstudy", Context.MODE_PRIVATE);
            Set<String> storedSubjects = preferences.getStringSet(storingname, new HashSet<>());

            boolean same = storedSubjects.equals(new HashSet<>(combinedSubjectNames));

            preferences.edit().putStringSet(storingname, new HashSet<>(combinedSubjectNames)).apply();
            Log.d("combinedsubjects", String.valueOf(combinedSubjectNames));

            if (!same) {
                createDynamicButtons(combinedSubjectNames.toArray(new String[0]));
            }
        } else {
            showToast();
        }
    }

    private List<String> fetchSubjectsFromDocument(DocumentSnapshot document) {
        List<String> subjectNames = new ArrayList<>();
        if (document != null && document.getData() != null) {
            Map<String, Object> data = document.getData();
            for (String field : data.keySet()) {
                Object value = data.get(field);
                if (value instanceof String) {
                    subjectNames.add((String) value);
                }
            }
        } else {
            // Log an error or show a toast if the document or its data is null.
            showToast();
        }
        return subjectNames;
    }

    private void loadStoredSubjectNames() {
        SharedPreferences preferences = getSharedPreferences("yearstudy", Context.MODE_PRIVATE);
        Set<String> storedSubjects = preferences.getStringSet(storingname, new HashSet<>());
        Log.d("subjectnames", String.valueOf(storedSubjects));

        if (!storedSubjects.isEmpty()) {
            createDynamicButtons(storedSubjects.toArray(new String[0]));
        } else {
            handleNoStoredSubjects();
        }
    }

    private void handleNoStoredSubjects() {
        if (isConnectedToInternet()) {
            Toast.makeText(this, "Loading..", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Connect to internet to load", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

}
