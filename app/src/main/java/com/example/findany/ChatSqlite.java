package com.example.findany;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatSqlite extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FindAnyChatData.db";
    private static final int DATABASE_VERSION = 1;

    public ChatSqlite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the initial table(s) when the database is first created
        createTable(db, "messages"); // Create the 'messages' table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema changes if needed
    }

    public void createTable(SQLiteDatabase db, String tableName) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "messageid TEXT PRIMARY KEY," +
                "sendername TEXT," +
                "regno TEXT," +
                "messagetext TEXT," +
                "messagetype TEXT," +
                "timestamp INTEGER," +
                "url TEXT)"; // Add the "url" field to the table definition
        db.execSQL(createTableQuery);
    }

    public void insertChatMessage(String tableName, String messageId, String senderName, String regNo, String messageText, String messageType, long timestamp, String url) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if a record with the same messageid already exists
        Cursor cursor = db.query(tableName, null, "messageid=?", new String[]{messageId}, null, null, null);
        if (cursor.getCount() == 0) {
            // Record with the same messageid does not exist; proceed with insertion
            ContentValues values = new ContentValues();
            values.put("messageid", messageId);
            values.put("sendername", senderName);
            values.put("regno", regNo);
            values.put("messagetext", messageText);
            values.put("messagetype", messageType);
            values.put("timestamp", timestamp);
            values.put("url", url); // Add the "url" value to the ContentValues

            long newRowId = db.insert(tableName, null, values);
        }
        cursor.close();
        db.close();
    }

    public Cursor getAllDataFromTable(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + tableName;
        return db.rawQuery(query, null);
    }

    public void clearTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, null, null);
        db.close();
    }


}

