package com.example.findany;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.findany.Firebase.Firestore;
import com.example.findany.Firebase.Storage;
import com.example.findany.callbacks.BooleanCallBack;
import com.example.findany.callbacks.ListReceived;
import com.example.findany.utils.internalstorage;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class soc_main extends AppCompatActivity {
    String TAG="soc_main";
    MaterialToolbar toolbar;
    SharedPreferences socprefs,userDetailsPrefs;
    String documentname;
    String Admin_Names="SOC_ADMINS";
    ImageView addproject;
    LinearLayout projectnames;
    ProgressBar progressBar;
    SharedPreferences.Editor soceditor,userDetailsEditor;
    List<String> allDocumentnames;
    Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soc_main);

        initializeViews();
        getAdmin();
        getSavedDetails();
        clickListeners();
    }

    private void getSavedDetails() {
        Firestore.getAllDocumentIds("SOC_FILES", new ListReceived() {
            @Override
            public void onListSuccess(List<String> documentIds) {
                progressBar.setVisibility(View.GONE);
                for (String documentId : documentIds) {
                   createDynamicButton(documentId);
                }
            }

            @Override
            public void onListFailure(Exception e) {
                // Handle failure, log error, or display an error message
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error getting document IDs: " + e.getMessage());
            }
        });
    }

    private void createDynamicButton(String name) {
        // Create a horizontal LinearLayout to hold the project button and delete button
        LinearLayout horizontalLayout = new LinearLayout(soc_main.this);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create a Button for the project
        Button projectButton = new Button(soc_main.this);
        projectButton.setText(name);
        projectButton.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        // Create a Button for delete
        Button deleteButton = new Button(soc_main.this);
        deleteButton.setText("Delete");
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Set a click listener for the project button
        projectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click (e.g., open document, perform an action)
                Intent intent = new Intent(soc_main.this, soc.class);
                intent.putExtra("projectname", name);
                startActivity(intent);
            }
        });

        // Set a click listener for the delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                Storage.deleteFilesInSubfolder("SOC_FILES/"+name+"/");
                for(int i=1;i<=11;i++){
                    Firestore.deleteDocument("SOC_FILES",name, String.valueOf(i));
                }

                internalstorage.deleteSubdirectory(context,name);

                progressBar.setVisibility(View.GONE);
                projectnames.removeView(horizontalLayout);
            }
        });

        // Add the project button and delete button to the horizontal layout
        horizontalLayout.addView(projectButton);
        horizontalLayout.addView(deleteButton);

        // Add the horizontal layout to the vertical layout
        projectnames.addView(horizontalLayout);
    }



    private void clickListeners() {
        addproject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProjectNameDialog();
            }
        });
    }

    private void showProjectNameDialog() {
        // Inflate the layout for the dialog
        View popupView = getLayoutInflater().inflate(R.layout.popup_window, null);

        // Create a PopupWindow with the inflated layout
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Set focusable to true to receive touch events outside the PopupWindow
        popupWindow.setFocusable(true);

        // Show the PopupWindow at the center of the screen
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // Find views in the popup layout
        EditText projectNameEditText = popupView.findViewById(R.id.editTextProjectName);
        Button saveButton = popupView.findViewById(R.id.buttonSave);

        // Set a click listener for the save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                // Retrieve the project name from the EditText
                String projectName = projectNameEditText.getText().toString().toUpperCase();

                Firestore.checkDocumentExistsAndCreateIfNotExists("SOC_FILES", projectName, new BooleanCallBack() {
                    @Override
                    public void onExists(boolean exists) {
                        if(exists){
                            Toast.makeText(soc_main.this, "Already you have a project with the same name", Toast.LENGTH_SHORT).show();
                            internalstorage.deleteSubdirectory(context,projectName);
                        }else{
                            createDynamicButton(projectName);

                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG,"save button error: "+e.getMessage());
                    }
                });

                // Dismiss the PopupWindow
                popupWindow.dismiss();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void getAdmin() {
        Log.d(TAG,documentname);
        Firestore.isAdmin(Admin_Names, documentname)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isAdmin = task.getResult();
                        if (isAdmin) {
                            Log.d(TAG,"YOU ARE ADMIN");
                            toolbar.setTitle("SOC ADMIN PANEL");
                        } else {
                            Log.d(TAG,"YOU ARE NOT ADMIN");
                            toolbar.setTitle("SOC STUDENT PANEL");
                        }
                    } else {
                        Log.d(TAG,"AN ERROR OCCURED");                    }
                });
    }
    private void initializeViews() {
        toolbar=findViewById(R.id.toolbar);
        addproject=findViewById(R.id.add);
        projectnames=findViewById(R.id.projectnamedisplay);
        progressBar=findViewById(R.id.centerProgressBar);

        socprefs=getSharedPreferences("SOC",MODE_PRIVATE);
        soceditor=socprefs.edit();

        userDetailsPrefs=getSharedPreferences("UserDetails",MODE_PRIVATE);
        userDetailsEditor=userDetailsPrefs.edit();

        documentname=userDetailsPrefs.getString("RegNo","");
    }
}