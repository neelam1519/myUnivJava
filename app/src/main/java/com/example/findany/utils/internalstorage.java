package com.example.findany.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.findany.callbacks.FilesCallback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class internalstorage {
    static String TAG="internalstorage";

    public static List<File> listFilesInPdfCacheDirectory(Context context,String folderName, String folder) {
        List<File> files = new ArrayList<>();

        try {
            File pdfCacheDir = new File(context.getCacheDir(), folder);
            File folderCacheDir = new File(pdfCacheDir, folderName);

            if (folderCacheDir.exists() && folderCacheDir.isDirectory()) {
                File[] fileList = folderCacheDir.listFiles();
                if (fileList != null) {
                    files.addAll(Arrays.asList(fileList));
                }
            } else {
                // Log or handle the case where the directory does not exist or is not a directory
                Log.e("FileList", "Directory does not exist or is not a directory: " + folderCacheDir.getAbsolutePath());
            }
        } catch (SecurityException e) {
            // Log or handle security-related exceptions
            Log.e("FileList", "SecurityException: " + e.getMessage());
        } catch (Exception e) {
            // Log or handle other exceptions
            Log.e("FileList", "Exception: " + e.getMessage());
        }
        return files;
    }

    public static File createCacheDirectoryForFolder(Context context,String folderName,String folder) {
        File cacheDir = new File(context.getCacheDir(), folder);
        File folderCacheDir = new File(cacheDir, folderName);
        if (!folderCacheDir.exists()) {
            folderCacheDir.mkdirs();
        }
        return folderCacheDir;
    }

    public static void getFilesInSubfolder(Context context,String subfolderName, FilesCallback callback) {
        // Get the internal storage directory
        File internalStorageDir = new File(context.getFilesDir(), subfolderName);

        // Check if the subfolder exists
        if (internalStorageDir.exists() && internalStorageDir.isDirectory()) {
            // Get the list of files in the subfolder
            File[] filesInSubfolder = internalStorageDir.listFiles();

            // Pass the files to the callback
            if (callback != null) {
                callback.onFilesReceived(filesInSubfolder);
            }
        } else {
            // Pass null to the callback if the subfolder doesn't exist
            if (callback != null) {
                callback.onFilesReceived(null);
            }
        }
    }

    public static void randomizeAndWriteToOriginalFile(Context context,File inputFile) throws IOException {
        // Read the CSV file
        List<String> numbers = readCSV(context,inputFile);

        // Shuffle the numbers
        Collections.shuffle(numbers);

        // Write the randomized numbers back to the original CSV file
        writeCSV(context,inputFile, numbers);
    }

    private static List<String> readCSV(Context context,File file) throws IOException {
        List<String> numbers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming each line contains a single number
                numbers.add(line.trim());
            }
        }

        return numbers;
    }

    private static void writeCSV(Context context,File file, List<String> data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String number : data) {
                writer.write(number);
                writer.newLine();
            }
            Toast.makeText(context, "Randomized completed", Toast.LENGTH_SHORT).show();
        }
    }

    public static void createEmptyFile(Context context, String subfolder, String fileName) {
        // Create a directory for the subfolder if it doesn't exist
        File subfolderDir = new File(context.getFilesDir(), subfolder);
        if (!subfolderDir.exists()) {
            subfolderDir.mkdir();
        }

        // Create an empty file within the subfolder
        File emptyFile = new File(subfolderDir, fileName);

        try {
            if (!emptyFile.exists()) {
                if (emptyFile.createNewFile()) {
                    Log.d(TAG, "Empty file created successfully: " + emptyFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "Failed to create empty file");
                }
            } else {
                Log.d(TAG, "File already exists: " + emptyFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating empty file: " + e.getMessage());
        }
    }
    public static void deleteSubdirectory(Context context, String subdirectoryName) {
        File internalStorageDir = context.getFilesDir();
        File subdirectory = new File(internalStorageDir, subdirectoryName);

        if (subdirectory.exists() && subdirectory.isDirectory()) {
            deleteDirectory(subdirectory);
        }
    }

    private static void deleteDirectory(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
        Log.d(TAG,"Directory deleted sucessfully");
    }
}
