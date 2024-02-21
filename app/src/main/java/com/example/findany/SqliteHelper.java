package com.example.findany;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SqliteHelper extends SQLiteOpenHelper {
    private String TAG="SqliteHelper";
    private static String DB_PATH;
    private static String DB_NAME = "TIMETABLE.db"; // Replace with your db file's name
    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private static final int DB_VERSION = 3; // Assuming your old version was 1
    private String dayOfWeek;

    public SqliteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        this.mContext = context;
    }

    public void createDatabase() {
        boolean mDataBaseExist = checkDataBase();
        Log.d("SqliteHelper", "Database exists: " + mDataBaseExist);
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDataBase();
                Log.d("SqliteHelper", "Database copied successfully.");
            } catch (Exception e) {
                Log.e("SqliteHelper", "Error copying database", e);
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws Exception {
        Log.d("SqliteHelper", "Copying database from assets...");

        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public SQLiteDatabase openDatabase() {
        String mPath = DB_PATH + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null) {
            mDataBase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // No need to implement this method for a pre-existing database
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            // New database version exists. Copy the database from assets.
            try {
                copyDataBase();
            } catch (Exception e) {
                Log.e("SqliteHelper", "Error copying database during upgrade", e);
                throw new Error("Error copying database during upgrade");
            }
        }
    }
    public List<String> getColumnNames(String tableName) {
        List<String> columnNames = new ArrayList<>();

        // Open the database if it's not opened.
        if (mDataBase == null || !mDataBase.isOpen()) {
            openDatabase();
        }

        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
            String[] columnNamesArray = cursor.getColumnNames();

            // Convert array to list and remove the first item
            for (String colName : columnNamesArray) {
                columnNames.add(colName);
            }
            columnNames.remove(0);

        } catch (Exception e) {
            Log.e("SqliteHelper", "Error fetching column names", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return columnNames;
    }

        public List<String> getValuesFromSpecificColumn(String tableName, String columnName) {
            List<String> columnValues = new ArrayList<>();

            // Open the database if it's not opened.
            if (mDataBase == null || !mDataBase.isOpen()) {
                openDatabase();
            }

            Log.d("SQLITEHELPER",tableName);
            Log.d("SQLITEHELPER",columnName);

            Cursor cursor = null;
            try {
                cursor = mDataBase.rawQuery("SELECT " + columnName + " FROM " + tableName, null);
                int columnIndex = cursor.getColumnIndex(columnName);

                if (columnIndex != -1 && cursor.moveToFirst()) {
                    do {
                        String value = cursor.getString(columnIndex);
                        columnValues.add(value);
                    } while (cursor.moveToNext());
                } else {
                    Log.e("SqliteHelper", "Column " + columnName + " does not exist in table " + tableName);
                }
            } catch (Exception e) {
                Log.e("SqliteHelper", "Error fetching values from column " + columnName, e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            return columnValues;
        }
    public String getRoomNoForLecturerAndSubject(String tableName, String lecturerName, String subject) {
        String roomNo = null;

        // Open the database if it's not opened.
        if (mDataBase == null || !mDataBase.isOpen()) {
            openDatabase();
        }

        // Use a cursor to query the database
        Cursor cursor = null;
        try {
            cursor = mDataBase.query(tableName, new String[]{"ROOMNO"}, subject + " = ?", new String[]{lecturerName}, null, null, null);

            // Check if the cursor contains any results
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the room number from the ROOMNO column after checking the column index
                int columnIndex = cursor.getColumnIndex("ROOMNO");
                if (columnIndex != -1) {
                    roomNo = cursor.getString(columnIndex);

                    // If the room number is empty or null, reset roomNo to null
                    if (roomNo == null || roomNo.trim().isEmpty()) {
                        roomNo = "";
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SqliteHelper", "Error fetching room no for lecturer and subject", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return roomNo;
    }

    public String getLecturerName(Context context, String subject) {
        SharedPreferences ACEDEMICDETAILS = context.getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String selectedValues = ACEDEMICDETAILS.getString("selectedValues", "");

        try {
            // Parse the stored XML-escaped JSON string
            JSONObject jsonObject = new JSONObject(selectedValues);

            // Get the lecturer's name for the given subject
            if (jsonObject.has(subject)) {
                return jsonObject.getString(subject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    // Method to get the room number for a lecturer's name in a given table
    private String getRoomNumberForLecturerName(String tableName, String lecturerName,String subject) {
        String roomNo = "";

        Cursor roomCursor = mDataBase.query(tableName, new String[]{"ROOMNO"}, subject + " = ?", new String[]{lecturerName}, null, null, null);

        if (roomCursor != null) {
            try {
                if (roomCursor.moveToFirst()) {
                    roomNo = roomCursor.getString(0);
                }
            } finally {
                roomCursor.close();
            }
        }

        return roomNo;
    }


}

