package com.example.findany.Firebase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class Storage {
    static String TAG="Storage";

    public static void deleteFilesInSubfolder(String storagepath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://adroit-chemist-368310.appspot.com/");
        StorageReference subfolderRef = storageRef.child(storagepath);

        subfolderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    if (listResult.getItems().isEmpty()) {
                        System.out.println("No files found in subfolder: " + storagepath);
                    } else {
                        for (StorageReference item : listResult.getItems()) {
                            item.delete()
                                    .addOnSuccessListener(aVoid -> System.out.println("File deleted successfully: " + item.getName()))
                                    .addOnFailureListener(e -> System.err.println("Error deleting file: " + item.getName() + ", " + e.getMessage()));
                        }
                    }
                })
                .addOnFailureListener(e -> System.err.println("Error listing files in subfolder: " + e.getMessage()));
    }

    public static Task<Void> uploadFile(Context context, File file, String destinationPath) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        if (file != null) {
            // Extract the file name from the file object
            String fileName = file.getName();

            // Get a reference to store the file with the specified file name
            StorageReference fileRef = storageRef.child(destinationPath + "/" + fileName);

            // Create an UploadTask and return its Task<Void>
            return fileRef.putFile(Uri.fromFile(file))
                    .addOnSuccessListener(taskSnapshot -> {
                        // File uploaded successfully
                        Log.d(TAG, "File uploaded successfully: " + fileName);
                        Toast.makeText(context, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        Log.e(TAG, "Failed to upload file: " + fileName, exception);
                        Toast.makeText(context, "Failed to upload file", Toast.LENGTH_SHORT).show();
                    })
                    .continueWithTask(task -> {
                        // Return the original task
                        return Tasks.forResult(null);
                    });
        } else {
            Log.e(TAG, "File object is null");
            Toast.makeText(context, "File object is null", Toast.LENGTH_SHORT).show();
            return Tasks.forException(new NullPointerException("File object is null"));
        }
    }

    public static Task<Void> downloadStorageFile(Context context, String storagename, String documentName, String fileName) {
        // Get a reference to the Firebase Storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference pointing to the file you want to download
        StorageReference storageRef = storage.getReference().child(storagename).child(documentName).child(fileName);

        // Get internal storage directory
        File internalStorageDir = context.getFilesDir();

        // Create a subfolder in internal storage with the documentName
        File documentFolder = new File(internalStorageDir, documentName);
        if (!documentFolder.exists()) {
            documentFolder.mkdir();
        }

        // Create a local file to store the downloaded file
        File localFile = new File(documentFolder, fileName);

        // Create a TaskCompletionSource to manually complete the task
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        // Return the task associated with the download
        storageRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    // File successfully downloaded, you can handle it here
                    Log.d(TAG, "File Downloaded Successfully: " + fileName);
                    // Manually complete the task
                    taskCompletionSource.setResult(null);
                })
                .addOnFailureListener(exception -> {
                    // Handle any errors that may occur
                    Log.d(TAG, "Unable to Download File: " + fileName);
                    exception.printStackTrace();
                    // Manually complete the task with an exception
                    taskCompletionSource.setException(exception);
                });

        return taskCompletionSource.getTask();
    }
}
