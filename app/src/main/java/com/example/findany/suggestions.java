package com.example.findany;

import static com.example.findany.utils.utils.isConnectedToInternet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.findany.callbacks.EmailSendingCallback;
import com.example.findany.model.EmailData;
import com.example.findany.utils.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class suggestions extends AppCompatActivity {
    String TAG="suggestions";
    private static final int PICK_FILE_REQUEST = 1;
    public static  final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE=999;
    private EditText editSuggestion;
    private Button uploadFileButton;
    private Button submitButton;
    private ImageButton backPressedBtn;
    private List<File> selectedFiles;
    private SharedPreferences sharedPreferences;
    private String email,displayname,documentName;
    Context context=this;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        initialize();
        setButtonListeners();
    }

    private void setButtonListeners() {
        backPressedBtn.setOnClickListener(v -> onBackPressed());

        uploadFileButton.setOnClickListener(v -> {
            if (isConnectedToInternet(this)) {
                selectedFiles.clear();
                selectFile();
            } else {
                Toast.makeText(this, "Connect to the internet", Toast.LENGTH_SHORT).show();
            }
        });

        submitButton.setOnClickListener(v -> {
            if (isConnectedToInternet(this)) {
                progressBar.setVisibility(View.VISIBLE);
                String suggestion = editSuggestion.getText().toString().trim();
                if (suggestion.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Please enter a suggestion", Toast.LENGTH_SHORT).show();
                }else{
                    sendEmail(suggestion);
                }
            } else {
                Toast.makeText(this, "Connect to the internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmail(String suggestion) {
        // Determine the appropriate subject and message based on the selected mode
        String message = "Name: " + displayname + "\n" +
                "RegNo: " + documentName + "\n" +
                "Suggestion: " + suggestion;
        String mailsubject = "Suggestion";

        // Check if no files are selected and add a default image
        if (selectedFiles.isEmpty()) {
            // Load the default image from drawable
            Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);

            // Convert Bitmap to Base64
            String base64EncodedImage = bitmapToBase64(defaultBitmap);

            // Create a default attachment
            File defaultImageFile = createTempFile(base64EncodedImage, "default_image.png");
            selectedFiles.add(defaultImageFile);
        }

        EmailData emailData = new EmailData(selectedFiles.toArray(new File[0]), "neelammsr@gmail.com", mailsubject, message);
        // Create an instance of EmailSender
        EmailSender emailSender = new EmailSender(this, new EmailSendingCallback() {
            @Override
            public void onEmailSendingComplete(boolean isSuccessful) {
                // Show the appropriate toast message based on the email sending status
                if (isSuccessful) {
                    Toast.makeText(suggestions.this, "Files are sent for reviewing", Toast.LENGTH_SHORT).show();
                    utils.navigateToMainActivity(context, email);
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(suggestions.this, "There is a problem with the email server please try again later", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // Create an instance of EmailData and execute the task with the callback
        emailSender.execute(emailData);
    }

    // Method to convert Bitmap to Base64
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Method to create a temporary file with Base64-encoded content
    private File createTempFile(String base64EncodedContent, String fileName) {
        try {
            File tempFile = new File(getCacheDir(), fileName);
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] data = Base64.decode(base64EncodedContent, Base64.DEFAULT);
                outputStream.write(data);
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void selectFile() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33 and above
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Files"), PICK_FILE_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.READ_MEDIA_AUDIO
                }, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else { // API level 32 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Files"), PICK_FILE_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    handleSelectedFile(uri);
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                handleSelectedFile(uri);
            }

            if (selectedFiles.isEmpty()) {
                Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSelectedFile(Uri uri) {
        String filePath = getFilePathFromUri(uri);
        if (filePath != null) {
            selectedFiles.add(new File(filePath));
        } else {
            Toast.makeText(this, "Failed to retrieve file", Toast.LENGTH_SHORT).show();
        }
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
                File tempFile = new File(getCacheDir(), fileName);
                copyUriToFile(uri, tempFile);
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

    private void initialize() {
        sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("Mail", "");
        documentName = sharedPreferences.getString("RegNo", "");
        displayname = sharedPreferences.getString("FullName", "");

        editSuggestion = findViewById(R.id.edit_suggestion);
        uploadFileButton = findViewById(R.id.btn_upload);
        submitButton = findViewById(R.id.submit);
        backPressedBtn = findViewById(R.id.back_button);
        progressBar=findViewById(R.id.progressBar);

        selectedFiles = new ArrayList<>();
    }
}
