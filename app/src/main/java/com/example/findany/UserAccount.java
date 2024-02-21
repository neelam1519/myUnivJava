package com.example.findany;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UserAccount extends AppCompatActivity {
    static String TAG="UserAccount";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView profileimageView;
    ImageButton backpressedbtn;
    Button editprofilebtn, updatebtn;
    TextView fullnameTextView, regnoTextView, emailTextView;
    EditText usernameEditText, mobilenumberEditText;
    Spinner yearOfStudySpinner,branchofstudy,sectionspinner;
    ArrayAdapter<CharSequence> sectionAdapter, yearOfStudyAdapter, branchOfStudyAdapter;
    String spinnerbranch,spinneryear,spinnersection,documentname,fullname,email,slot,branch,section,year,mobileNumber,username;
    String imageUrl;
    private SqliteHelper dbHelper;
    HashMap<String, String> selectedTeacherSpinnerValues=new HashMap<>(),timetablevalues = new HashMap<>();
    private static final int REQUEST_CODE_OPEN_GALLERY = 1;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String previousBranch;
    private String previousYear;
    Context context=this;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        dbHelper = new SqliteHelper(this);

        initializeViews();
        adapterInitialization();

        ReadTheData();
        setUpOnClickListeners();

        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        branchofstudy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerbranch = branchofstudy.getSelectedItem().toString();

                if(!spinnerbranch.equals(previousBranch)) {
                    updateSectionSpinner(spinnerbranch, spinneryear);
                    previousBranch=spinnerbranch;
                    Log.e(TAG, "Branchspinner: " + spinnerbranch);
                }
                slot=getSlotValue(getApplicationContext(),spinnerbranch,spinneryear,spinnersection);
                dynamicspinners(spinnerbranch,spinneryear,spinnersection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        yearOfStudySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinneryear = yearOfStudySpinner.getSelectedItem().toString();
                if(!spinneryear.equals(previousYear)) {
                    updateSectionSpinner(spinnerbranch, spinneryear);
                    previousYear=spinneryear;
                    Log.e(TAG, "Yearspinner: " + spinneryear);
                }
                slot=getSlotValue(getApplicationContext(),spinnerbranch,spinneryear,spinnersection);
                dynamicspinners(spinnerbranch,spinneryear,spinnersection);
                Log.d(TAG,"SLOT"+slot);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sectionspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Perform a null check before calling toString()
                if (sectionspinner.getSelectedItem() != null) {
                    spinnersection = String.valueOf(sectionspinner.getSelectedItem());
                    Log.e(TAG, "SECTIONSPINNER: " + spinnersection);
                    slot = getSlotValue(getApplicationContext(), spinnerbranch, spinneryear, spinnersection);
                    dynamicspinners(spinnerbranch, spinneryear, spinnersection);
                    Log.d(TAG, "SLOT" + slot);
                } else {
                    // Handle the case where the selected item is null
                    Log.e(TAG, "Selected item in sectionspinner is null");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateSectionSpinner(String branch, String year) {
        // Parse the JSON string as a JSON object
        try {
            SharedPreferences otherDetails = getSharedPreferences("OTHERDETAILS", MODE_PRIVATE);
            String key = year + "_" + branch;

            // Retrieve the JSON string from SharedPreferences
            String jsonString = otherDetails.getString(key, "");

            Log.d(TAG, "Key: " + key);
            Log.d(TAG, "JSON String: " + jsonString);

            if (!jsonString.isEmpty()) {
                // Parse the JSON string as a JSON object
                JSONObject sectionJsonObject = new JSONObject(jsonString);

                // Create a new adapter
                ArrayAdapter<String> newSectionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
                newSectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Add each section from the JSON object to the new adapter
                Iterator<String> keys = sectionJsonObject.keys();
                while (keys.hasNext()) {
                    String sectionKey = keys.next();
                    String sectionName = sectionJsonObject.getString(sectionKey);
                    if (!sectionName.isEmpty()) {
                        newSectionAdapter.add(sectionName);
                    }
                }

                // Set the new adapter to the spinner
                sectionspinner.setAdapter(newSectionAdapter);
            }
        } catch (JSONException e) {
            Log.d(TAG,"updatesectionspinnererror occured");
            e.printStackTrace();
        }
    }
    private void ReadTheData() {
        Log.d(TAG, "Reading the data");
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);

        username = sharedPreferences.getString("username", "");
        mobileNumber = sharedPreferences.getString("MobileNumber", "");
        imageUrl = sharedPreferences.getString("ImageUrl", "");
        slot = sharedPreferences.getString("slot", "");
        year = sharedPreferences.getString("year", "");
        branch = sharedPreferences.getString("branch", "");
        section = sharedPreferences.getString("section", "");
        email=sharedPreferences.getString("Mail","");
        fullname=sharedPreferences.getString("FullName","");
        documentname=sharedPreferences.getString("RegNo","");

        previousYear=year;
        previousBranch=branch;

        fullnameTextView.setText(fullname);
        regnoTextView.setText(documentname);
        emailTextView.setText(email);
        mobilenumberEditText.setText(mobileNumber);
        Glide.with(this).load(imageUrl).error(R.drawable.defaultprofile).placeholder(R.drawable.defaultprofile).into(profileimageView);

        if (username == null || username.isEmpty()) {
            usernameEditText.setText(generateNickname(fullname));
        } else {
            usernameEditText.setText(username);
        }

        // Set year spinner value
        if (!TextUtils.isEmpty(year)) {
            int yearPosition = yearOfStudyAdapter.getPosition(year);
            if (yearPosition != -1) {
                yearOfStudySpinner.setSelection(yearPosition);
                Log.d(TAG, "Year Spinner Value Set: " + year);
            } else {
                Log.e(TAG, "Year Spinner Value Not Found: " + year);
            }
        }

        // Set year spinner value
        if (!TextUtils.isEmpty(branch)) {
            int branchPosition = branchOfStudyAdapter.getPosition(branch);
            if (branchPosition != -1) {
                branchofstudy.setSelection(branchPosition);
                Log.d(TAG, "Branch Spinner Value Set: " + branch);
            } else {
                Log.e(TAG, "Branch Spinner Value Not Found: " + branch);
            }
        }

        updateSectionSpinner(branch,year,section);

        progressBar.setVisibility(View.GONE);
    }
    private void updateSectionSpinner(String selectedBranch, String selectedYear, String selectedSection) {

        SharedPreferences otherDetails = getSharedPreferences("OTHERDETAILS", MODE_PRIVATE);
        String key = selectedYear + "_" + selectedBranch;
        String jsonString = otherDetails.getString(key, "");
        String branchData = otherDetails.getString("USERACCOUNTBRANCHES", "");

        // Parse the JSON string to get the sections
        List<CharSequence> sectionData = parseJsonSections(jsonString);
        List<CharSequence> branchDataList = parseJsonSections(branchData);

        // Null checks for data
        if (sectionData == null) {
            sectionData = new ArrayList<>();
        }

        if (branchDataList == null) {
            branchDataList = new ArrayList<>();
        }

        // Set adapters to null before creating new instances
        sectionAdapter = null;
        branchOfStudyAdapter = null;

        // Create a new ArrayAdapter with the list of sections
        sectionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sectionData);
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Create a new ArrayAdapter with the list of branches
        branchOfStudyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branchDataList);
        branchOfStudyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Update the spinner with the new data
        sectionspinner.setAdapter(sectionAdapter);
        branchofstudy.setAdapter(branchOfStudyAdapter);

        Log.d(TAG, "selectedBranch: " + selectedBranch);
        Log.d(TAG, "selectedYear: " + selectedYear);
        Log.d(TAG, "selectedSection: " + selectedSection);

        if (selectedSection != null && !selectedSection.isEmpty()) {
            int position = findPositionInAdapter(sectionAdapter, selectedSection);
            if (position != -1) {
                runOnUiThread(() -> {
                    sectionspinner.setSelection(position);
                    Log.d(TAG, "After setting section spinner: " + sectionspinner.getSelectedItem());
                });
            }
        }

        if (selectedBranch != null && !selectedBranch.isEmpty()) {
            int position = findPositionInAdapter(branchOfStudyAdapter, selectedBranch);
            if (position != -1) {
                runOnUiThread(() -> {
                    branchofstudy.setSelection(position);
                    Log.d(TAG, "After setting branch spinner: " + branchofstudy.getSelectedItem());
                });
            }
        }
    }


    private List<CharSequence> parseJsonSections(String jsonString) {
        List<CharSequence> sections = new ArrayList<>();

        try {
            JSONObject sectionJsonObject = new JSONObject(jsonString);

            Iterator<String> keys = sectionJsonObject.keys();
            while (keys.hasNext()) {
                String sectionKey = keys.next();
                String sectionName = sectionJsonObject.getString(sectionKey);
                if (!sectionName.isEmpty()) {
                    sections.add(sectionName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sections;
    }

    private int findPositionInAdapter(ArrayAdapter<CharSequence> adapter, CharSequence item) {
        if (item == null) {
            Log.d(TAG, "Provided item is null");
            return -1;
        }

        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            CharSequence adapterItem = adapter.getItem(i);
            if (adapterItem == null) {
                continue;
            }

            if (item.toString().trim().equals(adapterItem.toString().trim())) {
                Log.d(TAG, "Position of " + item + " in adapter: " + i);
                return i;
            }
        }
        Log.d(TAG, "Item not found in adapter: " + item);
        return -1;
    }
    private void adapterInitialization(){

        branchOfStudyAdapter = ArrayAdapter.createFromResource(this, R.array.BRANCH, android.R.layout.simple_spinner_item);
        branchOfStudyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branchofstudy.setAdapter(branchOfStudyAdapter);

        yearOfStudyAdapter = ArrayAdapter.createFromResource(this, R.array.year_of_study_options, android.R.layout.simple_spinner_item);
        yearOfStudyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearOfStudySpinner.setAdapter(yearOfStudyAdapter);

        sectionAdapter = ArrayAdapter.createFromResource(this, R.array.DEFAULT, android.R.layout.simple_spinner_item);
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionspinner.setAdapter(sectionAdapter);

    }

    private void setUpOnClickListeners() {

        editprofilebtn.setOnClickListener(v -> {
            if (isConnectedToInternet()) {
                openGallery();
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });

        updatebtn.setOnClickListener(v -> {
            if (isConnectedToInternet()) {
                handleUpdateClick();
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });

        backpressedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UserAccount.this,MainActivity.class);
                startActivity(intent);
            }
        });

        mobilenumberEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Check if the action is an "UP" event and if the touch is within the drawable bounds
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Drawable drawable = mobilenumberEditText.getCompoundDrawables()[2]; // 0: left, 1: top, 2: right, 3: bottom

                    // Check if the touch event is within the drawable bounds
                    if (drawable != null && event.getRawX() >= (mobilenumberEditText.getRight() - drawable.getBounds().width())) {
                        Intent intent=new Intent(UserAccount.this,LoginPhoneNumberActivity.class);
                        startActivity(intent);
                        // The right drawable was touched
                        // Implement your onClick functionality here
                        // For example, you can open a dialog or perform an action
                        // Remember to return true to consume the event
                        Toast.makeText(UserAccount.this, "Edit your Mobile Number", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void handleUpdateClick() {

        String username = usernameEditText.getText().toString();
        String mobilenumber=mobilenumberEditText.getText().toString();

        write2firebase(username, mobilenumber, spinneryear, spinnerbranch, spinnersection);

    }

    private void write2firebase(String username, String mobileNumber, String year, String branch, String section) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> newData = buildNewData(username, mobileNumber, year, branch, section, slot,selectedTeacherSpinnerValues);
        Log.d(TAG, String.valueOf(selectedTeacherSpinnerValues));

        db.collection("UserDetails")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(usernameQueryTask -> {
                    if (usernameQueryTask.isSuccessful() && isUsernameUsed(usernameQueryTask, documentname)) {
                        Toast.makeText(UserAccount.this, "Username is already used", Toast.LENGTH_SHORT).show();
                    } else {
                        updateOrCreateDocument(newData,username,mobileNumber,year,branch,section,slot,selectedTeacherSpinnerValues);
                    }
                });
    }
    private boolean isUsernameUsed(Task<QuerySnapshot> usernameQueryTask, String documentname) {
        for (QueryDocumentSnapshot document : usernameQueryTask.getResult()) {
            String regNo = document.getString("RegNo");
            if (regNo != null && !regNo.equalsIgnoreCase(documentname)) {
                return true;
            }
        }
        return false;
    }
    public void saveStudentDetails(String username, String mobileNumber, String year, String branch, String section, String slotvalue, HashMap<String, String> selectedValues, Boolean detailsupdated) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("Mail", email);
        editor.putString("FullName", fullname);
        editor.putString("RegNo", documentname);
        editor.putString("username", username);
        editor.putString("MobileNumber", mobileNumber);
        editor.putString("year", year);
        editor.putString("branch", branch);
        editor.putString("section", section);
        editor.putString("slot", slotvalue);
        editor.putBoolean("DetailsUpdated", detailsupdated);
        long currentTimestamp = System.currentTimeMillis();
        editor.putLong("userUploadedTimestamp", currentTimestamp);

        Gson gson = new Gson();
        String selectedValuesJson = gson.toJson(selectedValues);
        editor.putString("selectedValues", selectedValuesJson);

        editor.apply();
    }


    private Map<String, Object> buildNewData(String username, String mobileNumber, String year, String branch, String section, String slotvalue, HashMap<String, String> selectedValues) {
        Map<String, Object> newData = new HashMap<>();
        newData.put("username", username);
        newData.put("Mail", email);
        newData.put("FullName", fullname);
        newData.put("RegNo", documentname);
        newData.put("MobileNumber", mobileNumber);
        newData.put("year", year);
        newData.put("section", section);
        newData.put("branch", branch);
        newData.put("slot", slotvalue);
        newData.put("selectedValues", selectedValues);
        long currentTimestamp = System.currentTimeMillis();
        newData.put("userUploadedTimestamp", currentTimestamp);

        return newData;
    }
    private void updateOrCreateDocument(Map<String, Object> newData,String username, String mobileNumber, String year, String branch, String section, String slot, HashMap<String, String> selectedValues) {
        DocumentReference docRef = db.collection("UserDetails").document(documentname);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    docRef.set(newData, SetOptions.merge())
                            .addOnCompleteListener(setTask -> {
                                if (setTask.isSuccessful()) {
                                    Toast.makeText(UserAccount.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                                    saveStudentDetails(username,mobileNumber,year,branch,section,slot,selectedValues,true);
                                } else {
                                    // Handle the update error
                                    Log.e(TAG, "Error updating document: " + setTask.getException());
                                }
                                redirectToMainActivity();
                                progressBar.setVisibility(View.GONE);
                            });
                } else {
                    docRef.set(newData)
                            .addOnCompleteListener(setTask -> {
                                if (setTask.isSuccessful()) {
                                    Toast.makeText(UserAccount.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                                    saveStudentDetails(username,mobileNumber,year,branch,section,slot,selectedValues,true);
                                } else {
                                    // Handle the create error
                                    Log.e(TAG, "Error creating document: " + setTask.getException());
                                }
                                redirectToMainActivity();
                                progressBar.setVisibility(View.GONE);
                            });
                }
            } else {
                Log.e(TAG, "Error getting document: " + task.getException());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, REQUEST_CODE_OPEN_GALLERY);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else { // API level 32 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, REQUEST_CODE_OPEN_GALLERY);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .start(this);

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri croppedImageUri = result.getUri();
                uploadImage(croppedImageUri);
                Glide.with(this)
                        .load(croppedImageUri)
                        .into(profileimageView);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error in imagecroping", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to upload the image to Firebase Storage
    private void uploadImage(Uri imageUri) {
        Toast.makeText(this, "Updating profile...", Toast.LENGTH_LONG).show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + documentname + ".jpg");

        storageRef.delete().addOnCompleteListener(deleteTask -> {
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                imageUrl = uri.toString();

                                // Update Firestore with new image URL
                                updateImageInFirestore("UserDetails", imageUrl);
                                updateImageInFirestore("StudentResumeDetails", imageUrl);
                            }).addOnFailureListener(e -> handleError("Failed to get image URL", e)))
                    .addOnFailureListener(e -> handleError("Failed to upload image", e));
        });
    }

    private void updateImageInFirestore(String collectionPath, String imageUrl) {
        DocumentReference docRef = db.collection(collectionPath).document(documentname);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, update the image URL
                    docRef.update("ImageUrl", imageUrl).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            // Successfully updated image URL
                            SharedPreferences FirebaseRead = getSharedPreferences("UserDetails", MODE_PRIVATE);
                            SharedPreferences.Editor FirebaseEdit = FirebaseRead.edit();
                            FirebaseEdit.putString("ImageUrl", imageUrl);
                            FirebaseEdit.apply();
                            Log.d(TAG, "ImageUrl: " + imageUrl);
                            Toast.makeText(this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to update image URL
                            handleError("Failed to update image URL", updateTask.getException());
                        }
                    });
                } else {
                    // Document does not exist, create a new document
                    Map<String, Object> data = new HashMap<>();
                    data.put("ImageUrl", imageUrl);

                    docRef.set(data).addOnCompleteListener(createTask -> {
                        if (createTask.isSuccessful()) {
                            // Successfully created new document and set image URL
                            SharedPreferences FirebaseRead = getSharedPreferences("UserDetails", MODE_PRIVATE);
                            SharedPreferences.Editor FirebaseEdit = FirebaseRead.edit();
                            FirebaseEdit.putString("ImageUrl", imageUrl);
                            FirebaseEdit.apply();
                            Log.d(TAG, "ImageUrl: " + imageUrl);
                            Toast.makeText(this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to create new document
                            handleError("Failed to create new document", createTask.getException());
                        }
                    });
                }
            } else {
                // Failed to query document existence
                handleError("Failed to query document existence", task.getException());
            }
        });
    }

    private void handleError(String message, Exception e) {
        Log.e(TAG, message, e);
        Toast.makeText(this, "Failed to upload the profile photo", Toast.LENGTH_SHORT).show();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(UserAccount.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
    public String generateNickname(String name) {
        // Check if name is null, empty, or just spaces
        if (name == null || name.trim().isEmpty()) {
            return "Invalid name";
        }

        // Split the name into individual words using space as the delimiter
        String[] words = name.trim().split("\\s+"); // Using "\\s+" to handle multiple spaces between words

        Random random = new Random();

        // Select a random word from the name
        String randomWord = words[random.nextInt(words.length)];

        // Generate the nickname with some letters in uppercase and some in lowercase
        StringBuilder nicknameBuilder = new StringBuilder();
        for (char letter : randomWord.toCharArray()) {
            // Randomly decide to convert the letter to uppercase or lowercase
            nicknameBuilder.append(random.nextBoolean() ? Character.toUpperCase(letter) : Character.toLowerCase(letter));
        }

        return nicknameBuilder.toString();
    }

    public static String getSlotValue(Context context, String branch, String year, String section) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("OTHERDETAILS", Context.MODE_PRIVATE);

        String slotsValue = sharedPreferences.getString("SLOTS_" + branch + "_" + year, "");

        Log.d(TAG, "Retrieved slotsValue: " + slotsValue);

        String[] slotsArray = slotsValue.replaceAll("[{}]", "").split(", "); // Remove curly braces
        for (String slotInfo : slotsArray) {
            // Log the slot info for debugging
            Log.d(TAG, "Slot info: " + slotInfo);

            // Separate slot number and values
            String[] slotData = slotInfo.split("=");

            // Log the slot data for debugging
            Log.d(TAG, "Slot data: " + Arrays.toString(slotData));

            if (slotData.length == 2) {
                String[] slotValues = slotData[1].split(",");

                // Log the slot values for debugging
                Log.d(TAG, "Slot values: " + Arrays.toString(slotValues));

                for (String slotValue : slotValues) {
                    // Log the section value and the current slot value for debugging
                    Log.d(TAG, "Comparing section: " + section + " with slot value: " + slotValue.trim());

                    if (slotValue.trim().equals(section)) {
                        String result = slotData[0];

                        // Log the successful result
                        Log.d(TAG, "Found slot number for " + section + ": " + result);

                        return result;
                    }
                }
            } else {
                // Log if there are not enough elements in the slotData array
                Log.e(TAG, "Not enough elements in slotData array");
            }
        }
        // Log if the input is not found in any slot
        Log.d(TAG, "Input " + section + " not found in any slot");

        // Return -1 if the input is not found in any slot
        return "-1";
    }

    private void dynamicspinners(String branch,String year,String section){
        if (slot != null && branch != null && year != null && section != null && !slot.isEmpty() && !branch.isEmpty() && !year.isEmpty() && !section.isEmpty()) {
            timetablevalues.put("slot",slot);
            Log.d(TAG, "slotvaluespinner: " + slot);
            generateLecturerNames(branch, year, slot);
        } else {
            StringBuilder nullValues = new StringBuilder();

            if (slot == null || slot.isEmpty()) {
                nullValues.append("slotvalue ");
            }
            if (branch == null || branch.isEmpty()) {
                nullValues.append("branch ");
            }
            if (year == null || year.isEmpty()) {
                nullValues.append("selectedYearOfStudy ");
            }
            if (section == null || section.isEmpty()) {
                nullValues.append("sectionSelected ");
            }

            Log.d(TAG, "There is a null value in: " + nullValues.toString().trim());

            // If any of the criteria are null or empty, remove any existing dynamic spinners
            LinearLayout dynamicSpinnersLayout = findViewById(R.id.dynamic_spinners_layout);
            dynamicSpinnersLayout.removeAllViews();
        }
    }

    private void generateLecturerNames(String branch, String year,String slotvalue) {
        String lecturersNames = String.format("YEAR%s_%s_SLOT%s_LECTURERS", year, branch, slotvalue);
        Log.d(TAG, lecturersNames);

        List<String> items = dbHelper.getColumnNames(lecturersNames);
        for (String item : items) {
            Log.d(TAG, item);
        }
        createDynamicSpinners(items, lecturersNames);
    }

    private void createDynamicSpinners(List<String> subjects, String tablename) {
        // Initialize the map to store the selected values
        Log.d(TAG,"createDynamicSpinners started");

        LinearLayout dynamicSpinnersLayout = findViewById(R.id.dynamic_spinners_layout);
        dynamicSpinnersLayout.removeAllViews();

        selectedTeacherSpinnerValues.clear();

        for (String subject : subjects) {
            // Create a TextView as a label for the spinner
            TextView label = new TextView(this);
            label.setText(subject);
            label.setTextColor(getResources().getColor(R.color.teal_700));
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            label.setTypeface(label.getTypeface(), Typeface.BOLD);

            label.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            dynamicSpinnersLayout.addView(label);

            // Create a new spinner for the subject
            Spinner dynamicSpinner = new Spinner(this);
            dynamicSpinner.setId(subject.hashCode());
            dynamicSpinner.setTag(subject);

            int spinnerHeightInPx = dpToPx(40);
            dynamicSpinner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    spinnerHeightInPx
            ));

            dynamicSpinner.setBackgroundResource(R.drawable.spinner_background);

            List<String> spinnerOptions = dbHelper.getValuesFromSpecificColumn(tablename, subject);

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    spinnerOptions
            ) {
                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView textView = (TextView) view;

                    // Set the text color to white
                    textView.setTextColor(Color.WHITE);

                    return view;
                }
            };

            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dynamicSpinner.setAdapter(spinnerAdapter);

            String savedValue = getValueFromSharedPreferencesSpinners(subject);

            if (!savedValue.isEmpty()) {
                int spinnerPosition = spinnerAdapter.getPosition(savedValue);

                if (spinnerPosition != -1) {
                    dynamicSpinner.setSelection(spinnerPosition);
                }
            }

            dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String spinnerSubject = (String) parent.getTag();
                    String selectedValue = parent.getItemAtPosition(position).toString();
                    selectedTeacherSpinnerValues.put(spinnerSubject, selectedValue);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

            dynamicSpinnersLayout.addView(dynamicSpinner);
        }
    }
    private String getValueFromSharedPreferencesSpinners(String subject) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        String serializedMap = sharedPreferences.getString("selectedValues", "");

        // Parse the serialized map
        if (!serializedMap.isEmpty()) {
            // Remove curly braces and split by comma
            String[] entries = serializedMap.substring(1, serializedMap.length() - 1).split(", ");
            for (String entry : entries) {
                // Split by equals sign to separate key and value
                String[] keyValuePair = entry.split("=");
                if (keyValuePair.length == 2 && keyValuePair[0].equals(subject)) {
                    return keyValuePair[1];
                }
            }
        }
        return "";
    }
    public int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    private void initializeViews() {
        // Initialize TextViews
        fullnameTextView = findViewById(R.id.fullname);
        regnoTextView = findViewById(R.id.regno);
        emailTextView = findViewById(R.id.email);

        // Initialize EditTexts
        usernameEditText = findViewById(R.id.username);
        mobilenumberEditText = findViewById(R.id.phonenumber);

        // Initialize Buttons
        editprofilebtn = findViewById(R.id.editprofile);
        updatebtn = findViewById(R.id.update);
        backpressedbtn=findViewById(R.id.back_button);

        // Initialize ImageView
        profileimageView = findViewById(R.id.profileimage);

        // Initialize Spinners
        yearOfStudySpinner = findViewById(R.id.yearspinner);
        branchofstudy = findViewById(R.id.branchspinner);
        sectionspinner = findViewById(R.id.sectionspinner);

        // Initialize ProgressBar
        progressBar=findViewById(R.id.loadingProgressBar);

    }
}