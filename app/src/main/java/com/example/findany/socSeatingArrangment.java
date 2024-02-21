package com.example.findany;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class socSeatingArrangment extends AppCompatActivity {
    String TAG="socSeatingArrangment";
    SearchView searchView;
    TextView textView;
    String projectname;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean fieldFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soc_seating_arrangment);

        initializeviews();
        Intent intent = getIntent();
        projectname = intent.getStringExtra("projectname");

        // Set up the SearchView
        //configureSearchView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission here if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle search query changes here
                // Update the TextView with the current search query
                fieldFound=false;
                if(!newText.isEmpty() || newText!=null){
                    getTheClassroom(newText);
                    return true;
                }
                return false;
            }
        });
    }

    private void getTheClassroom(String field) {
        Log.d(TAG, field);

        for (int subcollection = 1; subcollection <= 11 && !fieldFound; subcollection++) {
            getSubcollectionValue("SOC_FILES", projectname, String.valueOf(subcollection), field);
        }
    }

    private void getSubcollectionValue(String collectionName, String documentName, String subcollectionName, String fieldname) {
        DocumentReference parentDocRef = db.collection(collectionName).document(documentName);
        CollectionReference subCollectionRef = parentDocRef.collection(subcollectionName);

        subCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Log all fields and their values
                    Log.d(TAG, "Document ID: " + document.getId());
                    for (Map.Entry<String, Object> entry : document.getData().entrySet()) {
                        String field = entry.getKey();
                        Object value = entry.getValue();
                        Log.d(TAG, "Fieldname: " + fieldname);
                        Log.d(TAG, "field: " + field);

                        if (field.equals(fieldname)) {
                            textView.setText(value.toString());
                        } else {
                            Log.d(TAG,"The fields are different");
                        }
                        Log.d(TAG, "Field: " + field + ", Value: " + value);
                    }
                }
            } else {
                // Handle error
                Log.e(TAG, "Error getting documents: " + task.getException());
            }
        });
    }

    private void initializeviews() {
        searchView = findViewById(R.id.searchView);
        textView = findViewById(R.id.textView);
    }
}
