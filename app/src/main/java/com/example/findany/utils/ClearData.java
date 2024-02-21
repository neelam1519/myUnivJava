package com.example.findany.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class ClearData {

    private static final String TAG = "ClearData";

    public static void clearCache(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            Log.d(TAG, "Clearing cache directory: " + cacheDir.getAbsolutePath());
            if (deleteRecursive(cacheDir)) {
                Log.d(TAG, "Cache cleared successfully");
            } else {
                Log.e(TAG, "Failed to clear cache");
            }
        }
    }

    private static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }
}
