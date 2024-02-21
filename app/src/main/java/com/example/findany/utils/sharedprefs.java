package com.example.findany.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class sharedprefs {

    public static void saveValueToPrefs(Context context, String sharedPrefName, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public static void saveLongToPrefs(Context context, String sharedPrefName, String key, Long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(key, value);
        editor.apply();
    }

    public static long getLongFromPrefs(Context context, String sharedPrefName, String key, long defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, defaultValue);
    }


    public static void saveListToPrefs(List<String> dataList, Context context, String sharedPrefName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String listString = android.text.TextUtils.join(",", dataList);
        editor.putString(key, listString);
        editor.apply();
    }
    public static List<String> getListFromPrefs(Context context, String sharedPrefName, String key) {
        // Get SharedPreferences instance
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        // Retrieve the comma-separated string from SharedPreferences
        String listString = sharedPreferences.getString(key, "");

        // Convert the string back to a list of strings
        List<String> dataList = new ArrayList<>(Arrays.asList(listString.split(",")));

        return dataList;
    }

    public static String getValueFromPrefs(Context context, String sharedPrefName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void clearSharedPreferences(Context context, String sharedPrefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void deleteListFromPrefs(Context context, String sharedPrefName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(key);
        editor.apply();
    }

    public static void saveDataInListFormat(Context context, String mainKey, String subKey, String value, String PREFERENCES_NAME) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        // Retrieve the existing map from SharedPreferences
        Map<String, Map<String, String>> mainMap = getDataMap(context, mainKey, PREFERENCES_NAME);

        // If the mainMap is null, create a new one
        if (mainMap == null) {
            mainMap = new HashMap<>();
        }

        // Get the subMap associated with the mainKey
        Map<String, String> subMap = mainMap.get(subKey);

        // If subMap is null, create a new one
        if (subMap == null) {
            subMap = new HashMap<>();
        }

        // Add or update the value in the subMap using the provided subKey
        subMap.put(subKey, value);

        // Put the updated subMap back into the mainMap
        mainMap.put(subKey, subMap);

        // Save the mainMap to SharedPreferences
        saveDataMap(context, mainKey, mainMap, PREFERENCES_NAME);
    }



    private static Map<String, Map<String, String>> getDataMap(Context context, String key,String PREFERENCES_NAME) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, null);

        if (json != null) {
            Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
            return gson.fromJson(json, type);
        }

        return null;
    }

    private static void saveDataMap(Context context, String key, Map<String, Map<String, String>> dataMap,String PREFERENCES_NAME) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(dataMap);
        editor.putString(key, json);
        editor.apply();
    }

    public static Map<String, String> retrieveDataFromListFormat(Context context, String mainKey, String PREFERENCES_NAME) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        // Retrieve the existing map from SharedPreferences
        Map<String, Map<String, String>> mainMap = getDataMap(context, mainKey, PREFERENCES_NAME);

        // Initialize a map to store all subkeys and values
        Map<String, String> allValues = new HashMap<>();

        // If the mainMap is not null, proceed with retrieving the values
        if (mainMap != null) {
            // Iterate over the mainMap entries
            for (Map.Entry<String, Map<String, String>> entry : mainMap.entrySet()) {
                // Get the subMap associated with each mainKey
                Map<String, String> subMap = entry.getValue();

                // If the subMap is not null, add its entries to the allValues map
                if (subMap != null) {
                    allValues.putAll(subMap);
                }
            }

            return allValues;
        }

        // Log that the values are not found

        // Return an empty map if the values are not found
        return allValues;
    }


}
