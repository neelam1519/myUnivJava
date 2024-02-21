package com.example.findany;

import static com.example.findany.utils.utils.isConnectedToInternet;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.findany.callbacks.EmailSendingCallback;
import com.example.findany.model.EmailData;
import com.example.findany.utils.ClearData;
import com.example.findany.utils.internalstorage;
import com.example.findany.utils.utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class materials_display_upload extends AppCompatActivity {
    String TAG="materials_display_upload";
    private static final int PICK_FILE_REQUEST = 1;
    private static  final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE=999;
    private static final String TEMP_FILE_EXTENSION = ".temp";
    FirebaseStorage storage;
    FirebaseFirestore db;
    StorageReference storageRef;
    BottomNavigationView bottomNavigation;
    FloatingActionButton uploadButton;
    ProgressDialog progressDialog;
    SharedPreferences UserDetailsPrefs;
    GridLayout pdfGridLayout;
    androidx.appcompat.widget.Toolbar toolbar;
    List<File>  cachedPdfFiles,selectedFiles= new ArrayList<>();
    List<String> allfiles = new ArrayList<String>();
    Map<File, TextView> textViewMap = new HashMap<>();
    Set<File> savetooffline = new HashSet<>();
    String subject, message, folder, displayname, documentname, email, branch, year, unit, mailsubject, cachefolder;
    Boolean GridLayoutForPdf=true, GridLayoutForQuestionpapers=false;
    private StorageTask<FileDownloadTask.TaskSnapshot> downloadTask;
    private Set<String> addedFileNames = new HashSet<>();
    SharedPreferences downloadcount;
    ProgressBar progressBar;
    List<String> downloadcountlist = new ArrayList<>(); // Your list of strings
    Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.materials_display_upload);

        Log.d(TAG,"Onstart Called");

        initializeViews();

        Intent intent = getIntent();
        year = intent.getStringExtra("year");
        branch = intent.getStringExtra("branch");
        subject = intent.getStringExtra("subject");
        unit=intent.getStringExtra("unit");

        if(utils.ColleageEmail(email,"@klu.ac.in")){
            uploadButton.setVisibility(View.VISIBLE);
        }else{
            uploadButton.setVisibility(View.GONE);
        }

        UploadButton();
        configureBottomNavigation();
    }
    private void UploadButton() {
        uploadButton.setOnClickListener(v -> {
            if (isConnectedToInternet(context)) {
                Toast.makeText(this, "If you have any Google Drive link or any other link send it through review", Toast.LENGTH_SHORT).show();
                selectFile();
            } else {
                Toast.makeText(this, "Connect to the internet", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void configureBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Log.d("navigation","bottomm navigation");
            switch (item.getItemId()) {
                case R.id.menu_pdf:
                    Log.d(TAG,"1");
                    toolbar.setTitle("PDFs");
                    folder="/"+year+"/"+branch+"/"+subject+"/"+unit;
                    cachefolder = subject+"/ "+unit;
                    updateGridLayout(folder);
                    GridLayoutForPdf = true;
                    GridLayoutForQuestionpapers = false;
                    return true;
                case R.id.menu_question_papers:
                    Log.d(TAG,"2");
                    toolbar.setTitle("QUESTION PAPERS");
                    folder="/"+year+"/"+branch+"/"+subject+"/"+"QUESTION_PAPERS";
                    updateGridLayout(folder);
                    cachefolder = subject+"/ "+"QUES";
                    GridLayoutForPdf = false;
                    GridLayoutForQuestionpapers = true;
                    return true;
                case R.id.menu_videos:
                    Log.d(TAG,"3");
                    toolbar.setTitle("Lectures ");
                    Intent intent=new Intent(this, youtubevideos.class);
                    intent.putExtra("branch",branch);
                    intent.putExtra("year",year);
                    intent.putExtra("subject",subject);
                    intent.putExtra("unit",unit);
                    startActivity(intent);
                    // Handle the Videos menu item
                    // Add your code here
                    return true;
                default:
                    return false;
            }
        });
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();

        pdfGridLayout = findViewById(R.id.pdfGridLayout);
        uploadButton = findViewById(R.id.uploadButton);
        storage = FirebaseStorage.getInstance();
        bottomNavigation = findViewById(R.id.bottomNavigation);
        toolbar = findViewById(R.id.toolbar);

        UserDetailsPrefs = getSharedPreferences("UserDetails", MODE_PRIVATE);

        displayname = UserDetailsPrefs.getString("FullName", "");
        documentname = UserDetailsPrefs.getString("RegNo", "");
        email = UserDetailsPrefs.getString("Mail", "");

    }
    @Override
    protected void onStart() {
        super.onStart();
        bottomNavigation.setSelectedItemId(R.id.menu_pdf);
        toolbar.setTitle("PDFs");
    }

    private void updateGridLayout(String folder){
        allfiles.clear();
        addedFileNames.clear();

        pdfGridLayout.removeAllViews();
        storageRef = storage.getReference().child(folder);
        Log.d("updateGridLayout: ",folder);
        displaycachedfiles();
    }

    private void displayFiles() {
        Log.d(TAG, "displayFiles started");

        if (storageRef == null) {
            Toast.makeText(materials_display_upload.this, "No Files Found", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No Files Found");
            progressBar.setVisibility(View.GONE);
            return;
        }

        storageRef.listAll().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE); // Hide the progress bar

            if (task.isSuccessful()) {
                ListResult result = task.getResult();
                List<StorageReference> items = result.getItems();

                if (items.isEmpty()) {
                    Toast.makeText(materials_display_upload.this, "No Files Found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(materials_display_upload.this, "Getting details....", Toast.LENGTH_SHORT).show();

                    // Iterate through storage items and add unique filenames to the all-files list
                    for (StorageReference item : items) {
                        String itemName = item.getName();
                        if (!allfiles.contains(itemName)) {
                            createPdf(item);
                            Log.d(TAG, itemName);
                        }
                    }
                }
            } else {
                Toast.makeText(materials_display_upload.this, "Error retrieving files", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error retrieving files", task.getException());
            }
        });
    }


    private void createPdf(StorageReference storageReference) {
        String fileExtension = utils.getFileExtension(storageReference.getName());

        if (fileExtension.equalsIgnoreCase("pdf")) {
            File tempDir = internalstorage.createCacheDirectoryForFolder(context, cachefolder, "temp");
            File localTempFile = new File(tempDir, storageReference.getName());

            // Show a progress bar while downloading
            ProgressDialog progressDialog = new ProgressDialog(materials_display_upload.this);
            progressDialog.setMessage("Downloading " + storageReference.getName());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.show();

            downloadTask = storageReference.getFile(localTempFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        downloadcountlist.add(localTempFile.getName());

                        String filename = utils.removeFileExtension(localTempFile.getName());
                        loadPdfThumbnail(filename, localTempFile, false);

                        // Dismiss the progress bar after successful download
                        progressDialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(materials_display_upload.this, "Failed to download PDF", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss(); // Dismiss the progress bar on failure
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        // Update progress bar during download
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        progressDialog.setProgress(progress);
                    });
        }
    }

    private void displaycachedfiles() {

        List<File> cachedTempFiles=new ArrayList<>();

        //cachedPdfFiles = listFilesInPdfCacheDirectory(cachefolder,"pdf_cache");

//        for (File pdfFile : cachedPdfFiles) {
//            String pdfFileName = pdfFile.getName();
//            for (File tempFile : cachedTempFiles) {
//                String tempFileName = tempFile.getName();
//                if (pdfFileName.equals(tempFileName)) {
//                    // Match found, delete the temp file
//                    if (tempFile.delete()) {
//                        // Successfully deleted the file
//                        Log.d("FileDeleted", "Deleted: " + tempFileName);
//                    } else {
//                        // Failed to delete the file
//                        Log.e("FileDeleted", "Failed to delete: " + tempFileName);
//                    }
//                }
//            }
//        }

        //Log.d("cachedfiles", String.valueOf(cachedPdfFiles));

        cachedTempFiles = internalstorage.listFilesInPdfCacheDirectory(getApplicationContext(),cachefolder, "temp");
        Log.d("CachedFiles", String.valueOf(cachedTempFiles));

        if (cachedTempFiles.isEmpty()) {
            if (!isConnectedToInternet(context)) {
                Toast.makeText(this, "No offline files available", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        } else {
//            for (File pdfFile : cachedPdfFiles) {
//
//                allfiles.add(pdfFile.getName());
//                String filename = removeFileExtension(pdfFile.getName());
//                loadPdfThumbnail(filename, pdfFile, true);
//            }

            for (File tempFile : cachedTempFiles) {
                Log.d("TempFile Name: ", String.valueOf(tempFile));

                allfiles.add(tempFile.getName());
                String filename = utils.removeFileExtension(tempFile.getName());
                loadPdfThumbnail(filename, tempFile, false);
            }
            progressBar.setVisibility(View.GONE);
        }
        if(isConnectedToInternet(context)) {
            displayFiles();
        }else{
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Connect to internet to load more", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdfToFolderCache(File sourceFile, File folderCacheDir) {
        File destinationFile = new File(folderCacheDir, sourceFile.getName());

        if (!isConnectedToInternet(context) && destinationFile.exists()) {
            destinationFile.delete();
        }

        try {
            if (sourceFile.exists()) {
                // Using try-with-resources to automatically close streams
                try (InputStream inputStream = new FileInputStream(sourceFile);
                     OutputStream outputStream = new FileOutputStream(destinationFile)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deletePdfInFolderCache(File sourceFile, File folderCacheDir) {
        File destinationFile = new File(folderCacheDir, sourceFile.getName());

        if (destinationFile.exists()) {
            destinationFile.delete(); // Delete the existing file
        }
    }


    private void sendoffline(){
        File folderCacheDir = internalstorage.createCacheDirectoryForFolder(context,cachefolder,"pdf_cache");

        for (File file : savetooffline) {
            Log.d("files", String.valueOf(file));
            String filename = utils.removeFileExtension(file.getName());
            TextView textView = textViewMap.get(file);
            if (textView != null) {
                textView.setText(filename); // Update the filename in the associated TextView
            }
            savePdfToFolderCache(file, folderCacheDir);
        }

        Toast.makeText(getApplicationContext(),"Selected files are added from offline mode",Toast.LENGTH_SHORT).show();
    }
    private void sendonline(){
        Log.d("savetooffline", String.valueOf(savetooffline));
        File folderCacheDir = internalstorage.createCacheDirectoryForFolder(context,cachefolder,"pdf_cache");
        for (File file : savetooffline) {
            String filename = utils.removeFileExtension(file.getName());
            TextView textView = textViewMap.get(file);
            if (textView != null) {
                textView.setText(filename); // Update the filename in the associated TextView
            }
            deletePdfInFolderCache(file, folderCacheDir);
        }
        savetooffline.clear();

        Toast.makeText(getApplicationContext(),"Selected files are removed from offline mode",Toast.LENGTH_SHORT).show();
    }

    private void toggleFileSelection(File pdf, String text) {
        Log.d("pdf", String.valueOf(pdf));

        if (savetooffline.contains(pdf)) {
            // If the file is already selected, remove it from the list
            removeFileSelection(pdf, text);
        } else {
            // If the file is not selected, add it to the list
            addFileSelection(pdf, text);
        }
    }
    private void removeFileSelection(File pdf, String text) {
        savetooffline.remove(pdf);
        updateTextView(pdf, text);
        Log.d("savetoffline", String.valueOf(savetooffline));
    }
    private void addFileSelection(File pdf, String text) {
        savetooffline.add(pdf);
        updateTextView(pdf, "SELECTED");
        Log.d("savetoffline", String.valueOf(savetooffline));
    }
    private void updateTextView(File pdf, String text) {
        TextView textView = textViewMap.get(pdf);
        if (textView != null) {
            textView.setText(text); // Update the filename in the associated TextView
        }
    }
    private void loadPdfThumbnail(String fileName, File localFile, Boolean cache) {
        try {
            // Open the PDF file for reading
            Log.d("materialsdisplayupload", "Thumbnail Loading: "+fileName);
            try (ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(localFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
                // Create a PDF renderer for the file
                try (PdfRenderer renderer = new PdfRenderer(fileDescriptor)) {
                    // Get the number of pages in the PDF
                    int pageCount = renderer.getPageCount();

                    if (pageCount > 0) {
                        // Open the first page of the PDF
                        try (PdfRenderer.Page page = renderer.openPage(0)) {
                            // Create a bitmap to render the page
                            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                            // Render the page content onto the bitmap
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                            // Calculate the height for cropping (40% from top to bottom)
                            int croppedHeight = (int) (bitmap.getHeight() * 0.4);
                            // Crop the bitmap to the calculated height
                            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), croppedHeight);
                            // Display the cropped thumbnail in the GridLayout
                            showInGrid(fileName, croppedBitmap, localFile, cache);
                        }
                    } else {
                        // Set a default icon for empty PDFs
                        Bitmap compressedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pdf);
                        // Display the default thumbnail in the GridLayout
                        showInGrid(fileName, compressedBitmap, localFile, cache);
                    }
                }
            }
        } catch (IOException e) {
            Log.d(TAG,"ShowInGrid Error");
            e.printStackTrace();
        }
    }

    private void showInGrid(String fileName, Bitmap thumbnailBitmap, File localFile, Boolean cache) {

        String filePath = localFile.getAbsolutePath();
        File file = new File(filePath);
        Log.d(TAG,"file: "+file);
        String folderPath = file.getParent();
        Log.d(TAG,"folderpath: "+folderPath);
        String folderName = new File(folderPath).getName();
        Log.d(TAG,"foldername: "+folderName);
        Log.d(TAG,"CacheFolder: "+cachefolder);

        addedFileNames.add(fileName);

        // Create a frame layout to hold the file entry
        FrameLayout frameLayout = new FrameLayout(this);

        // Create an ImageView for displaying the thumbnail
        ImageView thumbnailImageView = new ImageView(this);
        thumbnailImageView.setImageBitmap(thumbnailBitmap);

        // Set the thumbnail image view's layout parameters
        FrameLayout.LayoutParams thumbnailParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        thumbnailImageView.setLayoutParams(thumbnailParams);

        // Create a TextView for displaying the filename
        TextView fileNameTextView = new TextView(this);
        fileNameTextView.setText(fileName);
        fileNameTextView.setTextSize(14);
        fileNameTextView.setTextColor(Color.WHITE);

        // Set the TextView's layout parameters to take 20% of the ImageButton's height from bottom to top
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.BOTTOM;
        fileNameTextView.setLayoutParams(textParams);

        // Set a background color for the TextView
        fileNameTextView.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black

        // Create an ImageButton with a transparent background
        ImageButton fileButton = new ImageButton(this);
        fileButton.setBackgroundColor(Color.TRANSPARENT);

        // Set a fixed height for the ImageButton (e.g., 100dp, adjust as needed)
        int buttonHeight = (int) getResources().getDimension(R.dimen.fixed_button_height);
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                buttonHeight
        );
        fileButton.setLayoutParams(buttonParams);

        // Add a click listener to handle button actions
        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(materials_display_upload.this, "Clicked: " + localFile.getName(), Toast.LENGTH_SHORT).show();
                openPdfFile(localFile);
            }
        });

        // Add a long click listener for additional actions
        fileButton.setOnLongClickListener(v -> {
            Log.d("createpdf", String.valueOf(localFile));
            toggleFileSelection(localFile, localFile.getName());
            return true;
        });

        // Wrap the ImageButton in a FrameLayout to create the border effect
        FrameLayout borderLayout = new FrameLayout(this);

        // Create a drawable for the border
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setStroke(2, Color.BLACK); // 2-pixel black border

        // Set the border drawable as the background of the borderLayout
        borderLayout.setBackground(borderDrawable);

        // Add the ImageButton to the borderLayout
        borderLayout.addView(fileButton);

        // Add the ImageView, TextView, and bordered ImageButton to the frame layout
        frameLayout.addView(thumbnailImageView);
        frameLayout.addView(fileNameTextView);
        frameLayout.addView(borderLayout);

        // Configure layout parameters for the frame layout
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 0;
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 0.5f);
        layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        layoutParams.setMargins(10, 10, 10, 10);
        frameLayout.setLayoutParams(layoutParams);

        // Add space between the two items
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.space_height_upload)
        ));

        // Add the frame layout to the parent GridLayout
        pdfGridLayout.addView(frameLayout);

        // Store the TextView reference in the map using the associated PDF file
        textViewMap.put(localFile, fileNameTextView);

    }

    private void openPdfFile(File localFile) {

        Uri fileUri = FileProvider.getUriForFile(this, "com.neelam.findany.fileprovider", localFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33 and above
                boolean allPermissionsGranted = true;

                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (allPermissionsGranted) {
                    selectFile();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            } else { // API level 32 and below
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void selectFile() {
        selectedFiles.clear();

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

            if (!selectedFiles.isEmpty()) {
                sendEmail();
            } else {
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
    private void sendEmail() {
        // Determine the appropriate subject and message based on the selected mode
        message = folder + "\n" +
                "Name:" + displayname + "\n" +
                "RegNo:" + documentname;
        if (GridLayoutForPdf) {
            mailsubject = "MATERIAL PDFS";
        } else if (GridLayoutForQuestionpapers) {
            mailsubject = "QUESTION PAPERS";
        }
        EmailData emailData = new EmailData(selectedFiles.toArray(new File[0]), "neelammsr@gmail.com", mailsubject, message);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending email...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Use a spinner for loading animation
        progressDialog.show();

        // Create an instance of EmailSender and execute the task with the callback
        EmailSender emailSender = new EmailSender(this, new EmailSendingCallback() {
            @Override
            public void onEmailSendingComplete(boolean isSuccessful) {
                progressDialog.dismiss(); // Dismiss the progress dialog when email sending is complete

                // Show the appropriate toast message based on the email sending status
                if (isSuccessful) {
                    Toast.makeText(materials_display_upload.this, "Files are sent for reviewing", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(materials_display_upload.this, "There is a problem with the email server please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emailSender.execute(emailData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClearData.clearCache(this);

    }
}