package com.example.findany;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class materials_years extends AppCompatActivity {

    LinearLayout linearLayout;
    SharedPreferences sharedPreferences;
    String branch,specialization;
    Spinner spinner, spinner1;
    ArrayAdapter<CharSequence> branchAdapter, specializationAdapter;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.materials_years);

        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();

        initializeViews();

        // Setup branch spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                branch = spinner.getSelectedItem().toString();
                editor.putString("branch", branch).apply();
                loadStoredSpecializationNames();
                if (isConnectedToInternet()) {
                    updateSpecializationSpinner(branch);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup specialization spinner
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                specialization = spinner1.getSelectedItem().toString();
                editor.putString("specialization", specialization).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Create year buttons
        String[] years = {"YEAR 1", "YEAR 2", "YEAR 3", "YEAR 4"};
        for (String year : years) {
            Button button = new Button(this);
            button.setText(year);
            button.setTextSize(20);
            button.setEnabled(false);
            linearLayout.addView(button);
        }
    }


    private void updateSpecializationSpinner(String branch) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("SPECIALIZATIONS")
                .document(branch)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> specializationData = documentSnapshot.getData();
                        if (specializationData == null) return;

                        List<String> specializationList = new ArrayList<>();
                        for (Object value : specializationData.values()) {
                            specializationList.add(value.toString());
                        }

                        ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, specializationList);

                        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner1.setAdapter(specializationAdapter);

                        Set<String> storedSpecializations = new HashSet<>(specializationList);
                        editor.putStringSet(branch + "list", storedSpecializations).apply();

                        specialization = sharedPreferences.getString("specialization", specialization);
                        int specializationPosition = specializationAdapter.getPosition(specialization);
                        spinner1.setSelection(specializationPosition);

                        enableYearButtons();
                    } else {
                        Toast.makeText(this, "Specialization data not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadStoredSpecializationNames() {
        Set<String> storedSpecializations = sharedPreferences.getStringSet(branch + "list", new HashSet<>());
        Log.d("StoredNames", branch + "list" + "-" + storedSpecializations);

        List<String> adapterData;
        int selectionPosition;

        if (!storedSpecializations.isEmpty()) {
            adapterData = new ArrayList<>(storedSpecializations);
            specialization = sharedPreferences.getString("specialization", specialization);
            selectionPosition = adapterData.indexOf(specialization);
            enableYearButtons();
        } else {
            adapterData = Arrays.asList("NOT_LOADED", "CONNECT TO THE INTERNET");
            selectionPosition = 0;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(materials_years.this,
                android.R.layout.simple_spinner_item, adapterData);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setSelection(selectionPosition);
    }

    private void enableYearButtons() {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;
                button.setEnabled(true);
                button.setOnClickListener(v -> handleButtonClick((Button) v));
            }
        }
    }

    private void handleButtonClick(Button button) {
        final String year = button.getText().toString();

        Intent intent = new Intent(materials_years.this, materials_subjects.class);
        intent.putExtra("branch", branch);
        intent.putExtra("year", year);
        intent.putExtra("specialization", specialization);
        startActivity(intent);
    }


    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void initializeViews(){
        linearLayout = findViewById(R.id.linearlayout);

        // Spinner setup
        spinner = findViewById(R.id.spinner);
        spinner1 = findViewById(R.id.spinner1);

        // Initialize SharedPreferences and Editor
        sharedPreferences = getSharedPreferences("yearstudy", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Retrieve branch and specialization values from SharedPreferences
        branch = sharedPreferences.getString("branch", "CSE");
        specialization = sharedPreferences.getString("specialization", "CYBER SECURITY");


        // Set up branch of study spinner
        branchAdapter = ArrayAdapter.createFromResource(this, R.array.BRANCH, android.R.layout.simple_spinner_item);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(branchAdapter);
        int branchPosition = branchAdapter.getPosition(branch);
        spinner.setSelection(branchPosition);

        // Set up specialization spinner
        specializationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(specializationAdapter);
    }

}
