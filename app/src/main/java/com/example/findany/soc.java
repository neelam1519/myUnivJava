package com.example.findany;

import static com.example.findany.suggestions.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.findany.Firebase.Firestore;
import com.example.findany.Firebase.Storage;
import com.example.findany.callbacks.BooleanCallBack;
import com.example.findany.callbacks.FilesCallback;
import com.example.findany.callbacks.FirestoreCallback;
import com.example.findany.callbacks.MapCallback;
import com.example.findany.utils.internalstorage;
import com.example.findany.utils.sharedprefs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class soc extends AppCompatActivity {
    static String TAG = "SOC";
    Boolean isAdmin;
    SharedPreferences userDetailsPrefs;
    SharedPreferences.Editor userDetailsEditor;
    String RegNo;
    String Admin_Names = "SOC_ADMINS";
    MaterialToolbar toolbar;
    SharedPreferences socprefs;
    SharedPreferences.Editor soceditor;
    int PICK_FILE_REQUEST = 1234;
    Button firstplace, secondplace, thirdplace, listsubmit;
    MaterialButton submitfiles, sittingarrangment, saveclassrooms,showArrangment;
    LinearLayout displayfileslayout, classroomslayout,batchListLayout,fileslayout;
    MaterialTextView arrangmentCount;
    String color1 = "#FF0000";
    String color2 = "#AEFF00";
    String color3 = "#000CFF";

    // Convert color strings to integers using Color.parseColor()
    int intColor1 = Color.parseColor(color1);
    int intColor2 = Color.parseColor(color2);
    int intColor3 = Color.parseColor(color3);
    int buttoncolor = intColor1;
    String batchlisttext = "";
    int buttonsclicked = 0;
    TextView batchText;
    int i = 1;
    Context context = this;
    String batchnumber = "Batch_1";
    List<String> projectdocuments;
    public static String projectname;
    private Map<String, String> batches = new HashMap<>();
    private String intentsubkeyvalue;
    Map<String, String> Filenames = new HashMap<>();
    Map<String, Integer> numberOfPlaces = new HashMap<>();
    Map<String, File> FileMap = new HashMap<>();
    Map<String,String> documentdata=new HashMap<>();
    private List<String> classroomTextList = new ArrayList<>();
    String Random_File_Name = "GeneratedRandomFile";
    public static ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soc);

        initializeViews();
        submitfiles.setVisibility(View.GONE);
        sittingarrangment.setVisibility(View.GONE);
        arrangmentCount.setVisibility(View.GONE);
        saveclassrooms.setVisibility(View.GONE);
        showArrangment.setVisibility(View.GONE);

        Intent intent = getIntent();
        projectname = intent.getStringExtra("projectname");

        getkeyvalues();
        getAdmin();
        clickListners();

    }

    private void createDynamicListener(String hint) {


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        batchText = new TextView(this);
        batchText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        batchText.setHint(hint);
        batchText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        batchText.setText(batchlisttext);

        // Create a new Button
        Button button = new Button(this);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        button.setText("OK");

        linearLayout.addView(batchText);
        linearLayout.addView(button);

        batchListLayout.addView(linearLayout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batches.put(batchnumber,batchlisttext);
                button.setBackgroundColor(buttoncolor);
                if(i<=2 && buttonsclicked<=2) {
                    i++;
                    if(i==2){
                        buttoncolor=intColor2;
                    }else{
                        buttoncolor=intColor3;
                    }
                    batchnumber = "Batch_" + i + "\n"; // Append each value with a line break

                    batchText.setHint(batchnumber);

                    batchlisttext="";
                    if(!TextUtils.isEmpty(batchText.getText().toString())){
                        Log.d(TAG, batchText.getText().toString());
                        createDynamicListener(batchnumber);
                    }
                }
            }
        });
    }


    private void clickListners() {

        firstplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                firstplace.setBackgroundColor(buttoncolor);
                if (batchlisttext.isEmpty()) {
                    batchlisttext = "firstplace";
                } else {
                    batchlisttext = batchlisttext + "," + "firstplace";
                }
                firstplace.setEnabled(false);
                batchText.setText(batchlisttext);
                buttonsclicked++;
                batches.put(batchnumber,batchlisttext);
                progressBar.setVisibility(View.GONE);

            }
        });

        secondplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                secondplace.setBackgroundColor(buttoncolor);
                if (batchlisttext.isEmpty()) {
                    batchlisttext = "secondplace";
                } else {
                    batchlisttext = batchlisttext + "," + "secondplace";
                }

                secondplace.setEnabled(false);
                batchText.setText(batchlisttext);
                buttonsclicked++;
                batches.put(batchnumber,batchlisttext);
                progressBar.setVisibility(View.GONE);

            }
        });

        thirdplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                thirdplace.setBackgroundColor(buttoncolor);
                if (batchlisttext.isEmpty()) {
                    batchlisttext = "thirdplace";
                } else {
                    batchlisttext = batchlisttext + "," + "thirdplace";
                }
                thirdplace.setEnabled(false);
                batchText.setText(batchlisttext);
                buttonsclicked++;
                batches.put(batchnumber,batchlisttext);
                progressBar.setVisibility(View.GONE);
            }
        });

        listsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                batchListLayout.setEnabled(false);
                listsubmit.setEnabled(false);
                firstplace.setEnabled(false);
                secondplace.setEnabled(false);
                thirdplace.setEnabled(false);

                Map<String, String> data = new HashMap<>();
                data.put("firstplace_visibility", "false");
                data.put("secondplace_visibility", "false");
                data.put("thirdplace_visibility", "false");

                for (Map.Entry<String, String> batch : batches.entrySet()) {
                    data.put(batch.getKey(), batch.getValue());
                    batchlisttext = batch.getValue();
                    createDynamicFilesUpload(batch.getKey());
                }

                Firestore.storeDataInFirestore("SOC_FILES", projectname, data);

                numberOfPlaces=transformMapValues(batches);
                submitfiles.setVisibility(View.VISIBLE);

                progressBar.setVisibility(View.GONE);
            }
        });

        submitfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                submitfiles.setEnabled(false);
                fileslayout.setEnabled(false);

                Map<String,String> data=new HashMap<>();

                List<Task<Void>> uploadTasks = new ArrayList<>();

                for (Map.Entry<String, File> fileEntry : FileMap.entrySet()) {
                    String key = fileEntry.getKey();
                    File value = fileEntry.getValue();
                    // Store filename in Firestore
                    data.put("filename_" + key, value.getName());
                    // Upload file to Firebase Storage
                    Task<Void> uploadTask = Storage.uploadFile(context, value, "/SOC_FILES/" + projectname + "/");
                    uploadTasks.add(uploadTask);
                    // Display file in dynamic layout
                    DynamicFilesLayout(context, displayfileslayout, value.getName(), value);
                }
                // Store the data in the Firestore
                Firestore.storeDataInFirestore("SOC_FILES",projectname,data);

                // Combine all upload tasks into a single task
                Task<Void> combinedTask = Tasks.whenAll(uploadTasks);

                // Add a completion listener to the combined task
                combinedTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // All files were uploaded successfully
                        Log.d(TAG, "All files uploaded successfully");
                        Toast.makeText(context, "files uploaded successfully", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        sittingarrangment.setVisibility(View.VISIBLE);
                    } else {
                        // At least one file upload failed
                        Log.e(TAG, "Some files failed to upload");
                        Toast.makeText(context, "Some files failed to upload retry again", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        sittingarrangment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveclassrooms.setVisibility(View.VISIBLE);
                sittingarrangment.setEnabled(false);
                displayfileslayout.setEnabled(false);
                // Get the number of batches
                internalstorage.createEmptyFile(context,projectname,Random_File_Name);

                Log.d(TAG,"FileMap: "+FileMap+"  numberOfFiles: "+numberOfPlaces);

                processSeatingArrangement(FileMap,numberOfPlaces);
                createDynamicClassrooms();
            }
        });
        saveclassrooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sharedprefs.saveListToPrefs(classroomTextList,context,"SOC","classrooms");
                Log.d(TAG, String.valueOf(classroomTextList));
                saveclassrooms.setEnabled(false);
                classroomslayout.setEnabled(false);

                internalstorage.createEmptyFile(context, projectname, Random_File_Name);
                File file = new File(context.getFilesDir() + "/" + projectname, Random_File_Name);
                List<String> students=readCSV(file);

                arrangeInFirestore(classroomTextList,students);
            }
        });
        showArrangment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(soc.this, socSeatingArrangment.class);
                intent.putExtra("projectname",projectname);
                startActivity(intent);
            }
        });
    }

    private void arrangeInFirestore(List<String> classrooms, List<String> students) {
        if (classrooms != null && students != null) {

            int studentsPerBench = 3;
            int totalStudentsPerClass = 45;
            final int totalUploads = classrooms.size();
            final int[] uploadCount = {0};

            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Uploading Seating Arrangement");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(totalUploads); // Set the maximum value according to the total uploads
            progressDialog.show();

            FirestoreCallback firestoreCallback = new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    // Increment the file upload count
                    uploadCount[0]++;

                    // Update the progress bar
                    progressDialog.setProgress(uploadCount[0]);

                    // Check if all uploads are completed
                    if (uploadCount[0] == totalUploads) {
                        // Print or log the upload count after all uploads are completed
                        Log.d(TAG, "Total file uploads: " + uploadCount[0]);

                        // Hide the progress bar after completing the upload
                        progressDialog.dismiss();
                        showArrangment.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Handle failures
                    Log.e(TAG, "Firestore upload failed", e);

                    // You may consider hiding the progress bar here if needed
                    progressDialog.dismiss();
                }
            };
            int studentCounter = 0;

            for (String classroom : classrooms) {
                Log.d(TAG, "classroom: " + classroom);

                // Determine subcollection based on classroom length
                String subcollection;
                if (classroom.length() == 4) {
                    subcollection = classroom.substring(0, 1);
                } else if (classroom.length() == 5) {
                    subcollection = classroom.substring(0, 2);
                } else {
                    subcollection = classroom;
                }

                // Create a new data map for each classroom
                Map<String, String> classroomData = new HashMap<>();

                for (int j = 0; j < totalStudentsPerClass && studentCounter < students.size(); j++) {
                    int benchNumber = (j / studentsPerBench) + 1;
                    char seatLetter = (char) ('a' + (j % studentsPerBench));

                    String student = students.get(studentCounter);

                    // Remove double quotation marks from the student string
                    student = student.replaceAll("\"", "");

                    String studentAssignment = classroom + "(" + benchNumber + seatLetter + ")";

                    // Add data to the classroom map
                    classroomData.put(student, studentAssignment);

                    studentCounter++; // Increment the student counter for the next iteration
                }
                Log.d(TAG, "Each Classroom size: " + classroomData.size());
                Log.d(TAG, "Each Classroom Data: " + classroomData);
                // Upload accumulated data to Firestore
                Firestore.uploadFieldAndValueToSubDocument("SOC_FILES", projectname, subcollection, classroom, classroomData, firestoreCallback);
            }

        }
    }


    private void createDynamicClassrooms() {
        File file = new File(context.getFilesDir() + "/" + projectname, Random_File_Name);
        List<String> totalCount = readCSV(file);

        if (totalCount != null) {
            // Round up to the nearest whole number
            int totalClassrooms = (int) Math.ceil((double)totalCount.size()/45);
            Log.d(TAG,"Total classrooms required: "+totalClassrooms);

            // Get the screen width
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;

            // Calculate the width for each EditText based on the screen width divided by 3
            int editTextWidth = screenWidth / 3;

            // Create a flat list to store all the entered text
            classroomTextList = new ArrayList<>();

            // Retrieve the stored classroom numbers from SharedPreferences
            List<String> storedClassroomNumbers = sharedprefs.getListFromPrefs(context, "SOC", "classrooms");

            // Loop through the total classrooms
            for (int i = 0; i < totalClassrooms; i++) {
                // Create a new horizontal LinearLayout for each row
                LinearLayout horizontalLayout = new LinearLayout(context);
                horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                // Create 3 EditText views for each row or less for the last row
                int editTextsInRow = Math.min(3, totalClassrooms - i * 3);
                for (int j = 0; j < editTextsInRow; j++) {
                    final int finalI = i; // Declare a final variable for use in the inner class
                    EditText editText = new EditText(context);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER); // Set to accept only numbers
                    editText.setLayoutParams(new LinearLayout.LayoutParams(
                            editTextWidth,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    // Add the EditText to the horizontal LinearLayout
                    horizontalLayout.addView(editText);

                    // Add a TextWatcher to monitor changes in the EditText
                    final int currentJ = j;
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            // Calculate the index in the flat list
                            int index = finalI * 3 + currentJ;
                            // Ensure the size of allEditTextValues is at least index + 1
                            while (classroomTextList.size() <= index) {
                                classroomTextList.add(""); // Add empty strings or any default value
                            }
                            // Update the corresponding value in allEditTextValues when text changes
                            classroomTextList.set(index, charSequence.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });

                    // Set the retrieved classroom number to the EditText
                    int storedIndex = i * 3 + j;
                    if (storedClassroomNumbers.size() > storedIndex) {
                        editText.setText(storedClassroomNumbers.get(storedIndex));
                    }
                }

                // Add the horizontal LinearLayout to the parent vertical LinearLayout
                classroomslayout.addView(horizontalLayout);
            }

            // Now, you can access allEditTextValues to retrieve all entered text
        } else {
            Log.e(TAG, "Error reading total count from CSV file");
        }
    }


    private void processSeatingArrangement(Map<String, File> fileMap, Map<String, Integer> places) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> batch1 = new ArrayList<>();
        List<String> batch2 = new ArrayList<>();
        List<String> batch3 = new ArrayList<>();
        int i=1;

        for (Map.Entry<String, File> fileEntry : fileMap.entrySet()) {
            String key = fileEntry.getKey();
            File csvFile = fileEntry.getValue();

            if (places.containsKey(key)) {
                int place = places.get(key);
                Log.d(TAG, "numberOfPlaces: " + "key: " + key + "  places: " + place);

                List<String> students = readCSV(csvFile);

                // Log the total number of students
                Log.d(TAG, "Total number of students in file: " + students.size());

                if (place==1 && areAllPlacesOne(numberOfPlaces)) {
                    String batchKey = "batch" + i;
                    Log.d(TAG, "i: " + batchKey);

                    if (fileMap.size() == 2) {
                        // Logic for fileMap.size() == 2
                        if ("batch1".equals(batchKey)) {
                            batch1.addAll(students);
                            Log.d(TAG, "i: " + i + " added details to batch1");
                        } else if ("batch3".equals(batchKey)) {
                            batch3.addAll(students);
                            Log.d(TAG, "i: " + i + " added details to batch3");
                        }
                        i += 2;
                    } else if (fileMap.size() == 3) {
                        // Logic for fileMap.size() == 3
                        Log.d(TAG, "i: " + batchKey);
                        if ("batch1".equals(batchKey)) {
                            batch1.addAll(students);
                            Log.d(TAG, "i: " + i + " added details to batch1");
                        } else if ("batch2".equals(batchKey)) {
                            batch2.addAll(students);
                            Log.d(TAG, "i: " + i + " added details to batch2");
                        } else if ("batch3".equals(batchKey)) {
                            batch3.addAll(students);
                            Log.d(TAG, "i: " + i + " added details to batch3");
                        }
                        i++;
                    } else {
                        // Default case
                        batch2.addAll(students);
                    }
                } else if (place == 1) {
                    batch2.addAll(students);
                } else if (place == 2) {
                    split(students, batch1, batch3);
                } else if (place == 3) {
                    split(students, batch1, batch2, batch3);
                }

            } else {
                Log.w(TAG, "Key not found in places map: " + key);
            }
        }
        arrangmentCount.setVisibility(View.VISIBLE);

        arrangmentCount.setText("Batch1: "+batch1.size()+"\n"+
                                "Batch2: "+batch2.size()+"\n"+
                                "Batch3: "+batch3.size());

        // Log the final result after processing all files
        Log.d(TAG, "batch1: " + batch1.size());
        Log.d(TAG, "batch2: " + batch2.size());
        Log.d(TAG, "batch3: " + batch3.size());

        internalstorage.createEmptyFile(context, projectname, Random_File_Name);
        File file = new File(context.getFilesDir() + "/" + projectname, Random_File_Name);

        try {
            writeCSV(context, file, batch1, batch2, batch3);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private static boolean areAllPlacesOne(Map<String, Integer> places) {
        // If the places map is empty, return false
        if (places.isEmpty()) {
            return false;
        }

        // Get the first place value
        int firstPlace = places.values().iterator().next();

        // Check if all values are equal to the first place value
        return places.values().stream().allMatch(place -> place == firstPlace);
    }


    private void split(List<String > students,List<String> batch1,List<String> batch3){
        Log.d(TAG,"Entered Two Places");
        // Handle the case where there are two places
        int middleIndex = students.size() / 2;
        for (int i = 0; i < students.size(); i++) {
            if (i < middleIndex) {
                batch1.add(students.get(i));
            } else {
                batch3.add(students.get(i));
            }
        }
    }
    private void split(List<String> students, List<String> batch1, List<String> batch2, List<String> batch3) {
        Log.d(TAG,"Entered Three Places");
        int oneThirdIndex = students.size() / 3;
        int twoThirdIndex = 2 * (students.size() / 3);

        for (int i = 0; i < students.size(); i++) {
            if (i < oneThirdIndex) {
                batch1.add(students.get(i));
            } else if (i < twoThirdIndex) {
                batch2.add(students.get(i));
            } else {
                batch3.add(students.get(i));
            }
        }

        // Handle the case where the number of students is not divisible by 3
        int remainder = students.size() % 3;
        if (remainder == 1) {
            // Move one student from batch1 to batch2
            batch2.add(batch1.remove(batch1.size() - 1));
        } else if (remainder == 2) {
            // Move one student from batch1 and one from batch2 to batch3
            batch3.add(batch2.remove(batch2.size() - 1));
            batch3.add(batch1.remove(batch1.size() - 1));
        }
    }

    private static void writeCSV(Context context, File file, List<String> batch1, List<String> batch2, List<String> batch3) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            int maxSize = Math.max(Math.max(batch1.size(), batch2.size()), batch3.size());

            for (int i = 0; i < maxSize; i++) {
                List<String> row = new ArrayList<>();

                if (i < batch1.size()) {
                    row.add(batch1.get(i));
                } else {
                    row.add("0");
                }

                writer.writeNext(row.toArray(new String[0]));

                row.clear(); // Clear the row for the next set of data

                if (i < batch2.size()) {
                    row.add(batch2.get(i));
                } else {
                    row.add("0");
                }

                writer.writeNext(row.toArray(new String[0]));

                row.clear(); // Clear the row for the next set of data

                if (i < batch3.size()) {
                    row.add(batch3.get(i));
                } else {
                    row.add("0");
                }

                writer.writeNext(row.toArray(new String[0]));
            }

            // Log the total number of elements in the newly created CSV file
            List<String> totalCount = readCSV(file);
            Log.d(TAG, "Total elements in the newly created CSV file: " + totalCount.size());
            Storage.uploadFile(context, file, "/SOC_FILES/" + projectname + "/");
        }
    }

    public static List<String> readCSV(File file) {
        List<String> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                students.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }
    public static void DynamicFilesLayout(Context context, LinearLayout parentLayout, String text,File file) {
        Log.d(TAG,"Dynamic Files Layout started");
        // Create a new horizontal LinearLayout for each dynamic view
        LinearLayout horizontalLayout = new LinearLayout(context);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Create a TextView for the left side
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        textView.setText(text);

        // Create an ImageButton for the right side
        ImageButton imageButton = new ImageButton(context);
        imageButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        // Set your image resource for the ImageButton
        imageButton.setImageResource(R.drawable.random);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    internalstorage.randomizeAndWriteToOriginalFile(context,file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Add TextView and ImageButton to the horizontal layout
        horizontalLayout.addView(textView);
        horizontalLayout.addView(imageButton);

        // Add the horizontal layout to the parent layout
        parentLayout.addView(horizontalLayout);
    }

    private void createDynamicFilesUpload(String subkeyvalue) {
        if (subkeyvalue != null) {

            // Create a new horizontal LinearLayout
            LinearLayout horizontalLayout = new LinearLayout(this);
            horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Create a new TextView
            TextView fileUploadTextView = new TextView(this);
            fileUploadTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            fileUploadTextView.setText(subkeyvalue);

            // Create a new ImageButton
            ImageButton imageButton = new ImageButton(this);
            imageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            imageButton.setImageResource(R.drawable.file_upload); // Set your image resource

            // Add TextView and ImageButton to the horizontal layout
            horizontalLayout.addView(fileUploadTextView);
            horizontalLayout.addView(imageButton);

            // Add the horizontal layout to the vertical layout
            fileslayout.addView(horizontalLayout);

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open a file picker
                    selectFile(subkeyvalue);

                }
            });
        }
    }

    private void selectFile(String subkeyvalue) {
        String[] mediaPermissions = {
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkMediaPermissions(mediaPermissions)) {
                openFilePickerForCsv(subkeyvalue);
            } else {
                ActivityCompat.requestPermissions(this, mediaPermissions, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else { // API level 32 and below
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openFilePickerForCsv(subkeyvalue);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        }
    }

    private boolean checkMediaPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void openFilePickerForCsv(String subkeyvalue) {
        this.intentsubkeyvalue = subkeyvalue;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            progressBar.setVisibility(View.VISIBLE);
            // Retrieve the subkeyvalue directly from the class-level variable
            String subkeyvalue = this.intentsubkeyvalue;

            if (subkeyvalue != null) {
                Log.d(TAG, "Subkeyvalue retrieved successfully: " + subkeyvalue);
                Uri selectedFileUri = data.getData();
                String filePath = getFilePathFromUri(selectedFileUri);

                if (filePath != null && isCsvFile(filePath)) {
                    String filename=new File(filePath).getName();
                    updateFileUploadTextView(subkeyvalue, filename);
                    File file = new File(filePath);
                    FileMap.put(subkeyvalue,file);
                    Toast.makeText(this, "Selected CSV File: " + new File(filePath).getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please select a valid CSV file", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Subkeyvalue is null");
            }
        } else {
            Log.e(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);
        }
        progressBar.setVisibility(View.GONE);
    }
    private void updateFileUploadTextView(String subkeyvalue, String filename) {
        LinearLayout fileUploadTextViewLayout = findViewById(R.id.fileslayout);

        for (int i = 0; i < fileUploadTextViewLayout.getChildCount(); i++) {
            View childView = fileUploadTextViewLayout.getChildAt(i);
            if (childView instanceof LinearLayout) {
                LinearLayout horizontalLayout = (LinearLayout) childView;
                TextView fileUploadTextView = (TextView) horizontalLayout.getChildAt(0);

                if (fileUploadTextView.getText().toString().equals(subkeyvalue)) {
                    // Update the text of the TextView with the new filename
                    fileUploadTextView.setText(filename);
                    break;
                }
            }
        }
    }
    private boolean isCsvFile(String filePath) {
        // Check if the file has a CSV extension
        return filePath.toLowerCase().endsWith(".csv");
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                String fileName = cursor.getString(columnIndex);

                // Create a temporary file with the original file name in the "project1" folder
                File projectFolder = new File(getFilesDir(), projectname);
                if (!projectFolder.exists()) {
                    projectFolder.mkdirs();
                }

                File tempFile = new File(projectFolder, fileName);

                // Copy the content of the URI to the temporary file
                copyUriToFile(uri, tempFile);

                // Get the absolute path of the temporary file
                filePath = tempFile.getAbsolutePath();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return filePath;
    }

    private void copyUriToFile(Uri uri, File destinationFile) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void getAdmin() {
        Log.d(TAG,RegNo);
        Firestore.isAdmin(Admin_Names, RegNo)
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
        firstplace=findViewById(R.id.firstplace);
        secondplace=findViewById(R.id.secondplace);
        thirdplace=findViewById(R.id.thirdplace);
        listsubmit=findViewById(R.id.listsubmit);
        submitfiles=findViewById(R.id.submitfiles);
        displayfileslayout=findViewById(R.id.displayfileslayout);
        progressBar=findViewById(R.id.progressBar);
        sittingarrangment=findViewById(R.id.sittingarrangment);
        arrangmentCount=findViewById(R.id.arrangmentCount);
        batchListLayout=findViewById(R.id.listLinearLayout);
        fileslayout=findViewById(R.id.fileslayout);
        showArrangment=findViewById(R.id.showresult);

        classroomslayout=findViewById(R.id.classroomslayout);
        saveclassrooms=findViewById(R.id.saveclassrooms);

        socprefs=getSharedPreferences("SOC",MODE_PRIVATE);
        soceditor=socprefs.edit();

        userDetailsPrefs=getSharedPreferences("UserDetails",MODE_PRIVATE);
        userDetailsEditor=userDetailsPrefs.edit();

        RegNo=userDetailsPrefs.getString("RegNo","");
    }
    private void getkeyvalues(){
        Firestore.getFieldValueData("SOC_FILES",projectname, new MapCallback() {
            @Override
            public void onCallback(Map<String, Object> data) {
                if (data != null) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue().toString();
                        documentdata.put(key,value);
                        // Do something with the key and value
                        Log.d(TAG, "Key: " + key + ", Value: " + value);
                    }
                    retriveSetValues();
                } else {
                    Log.d(TAG, "Document does not exist or data is null");
                    createDynamicListener(batchnumber);
                    progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"getkeyvalue Exception: "+e.getMessage());
                createDynamicListener(batchnumber);
                progressBar.setVisibility(View.GONE);

            }
        });
    }
    private static Map<String, Integer> transformMapValues(Map<String, String> map) {
        Map<String, Integer> transformedMap = new HashMap<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String originalValue = entry.getValue();
            int transformedValue = transformString(originalValue);
            transformedMap.put(entry.getKey(), transformedValue);
        }

        Log.d(TAG, "Transformed Map: " + transformedMap);
        return transformedMap;
    }

    private static int transformString(String originalValue) {
        // Check if the original value contains a comma
        if (originalValue.contains(",")) {
            // If it contains a comma, split and get the count of strings
            String[] parts = originalValue.split(",");
            return parts.length;
        } else {
            // If it doesn't contain a comma, set as 1
            return 1;
        }
    }
    private void getFilesInSubFolder() {
        internalstorage.getFilesInSubfolder(context, projectname, new FilesCallback() {
            @Override
            public void onFilesReceived(File[] files) {
                Log.d(TAG, "Get Files In SubFolder is running : "+Filenames);

                // Check if files array is not null
                if (files != null) {
                    // Iterate over the batch data filenames
                    // Iterate over the batch data filenames
                    for (File file : files) {
                        Log.d(TAG,"File: "+file.getName());
                        for (Map.Entry<String, String> entry : Filenames.entrySet()) {
                            String batchNumber = entry.getKey();
                            String filevalue = entry.getValue();
                            String filename = file.getName();
                            Log.d(TAG, "filename: " + filename+"  Filenames: "+Filenames);
                            if (filename.equals(filevalue)) {
                                DynamicFilesLayout(context, displayfileslayout, filename, file);
                                FileMap.put(batchNumber, file);
                                break; // Break out of the loop once a matching filename is found
                            }
                        }
                    }
                    sittingarrangment.setVisibility(View.VISIBLE);
                    Log.d(TAG, "FileMap:  " + FileMap);
                    processSeatingArrangement(FileMap,numberOfPlaces);
                    createDynamicClassrooms();

                    // Use an array to hold a mutable boolean value
                    int i;

                    for ( i=1; i <= 11; i++) {
                        Firestore.checkSubcollections("SOC_FILES", projectname, String.valueOf(i), new BooleanCallBack() {
                            @Override
                            public void onExists(boolean exists) {
                                if (exists) {
                                    // Set the value in the array to true
                                    showArrangment.setVisibility(View.VISIBLE);
                                    saveclassrooms.setEnabled(false);
                                    Log.d(TAG, "VISIBLE");
                                } else {
                                    saveclassrooms.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                showArrangment.setVisibility(View.GONE);
                                Log.d(TAG, "NOT VISIBLE");
                            }
                        });

                    }

                } else {
                    Log.d(TAG, "Files array is null");
                }
            }
        });
    }

    private void retriveSetValues(){
        for(Map.Entry<String,String> data:documentdata.entrySet()){
            String key=data.getKey();
            String value=data.getValue();

            if(key.startsWith("Batch")){
                batchlisttext=value;
                createDynamicListener(key);
                batches.put(key,value);
            }else if(key.startsWith("filename_")) {
                String modifiedKey = key.substring("filename_".length());
                Filenames.put(modifiedKey, value);
            }else if ("firstplace_visibility".equals(key)) {
                if ("false".equals(batchlisttext)) {
                    firstplace.setEnabled(false);
                } else {
                    firstplace.setEnabled(true);
                }
            } else if ("secondplace_visibility".equals(key)) {
                if ("false".equals(batchlisttext)) {
                    secondplace.setEnabled(false);
                } else {
                    secondplace.setEnabled(true);
                }
            } else if ("thirdplace_visibility".equals(key)) {
                if ("false".equals(batchlisttext)) {
                    thirdplace.setEnabled(false);
                } else {
                    thirdplace.setEnabled(true);
                }
            } else {
                // Handle other keys if needed
            }
        }
        numberOfPlaces=transformMapValues(batches);
        List<Task<Void>> downloadTasks = new ArrayList<>();

        for (Map.Entry<String, String> filenames : Filenames.entrySet()) {
            if (Filenames != null) {
                String value = filenames.getValue();
                // Add each download task to the list
                downloadTasks.add(Storage.downloadStorageFile(context, "SOC_FILES", projectname, value));
            }
        }
        Tasks.whenAll(downloadTasks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // All download tasks are complete, you can now run getFilesInSubFolder
                        getFilesInSubFolder();
                    }
                });
        submitfiles.setEnabled(false);
        sittingarrangment.setEnabled(false);
        progressBar.setVisibility(View.GONE);
    }
}