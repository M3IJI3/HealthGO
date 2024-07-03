package com.example.healthgo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "users.db";
    public static final String TABLE_USER = "user_table";
    public static final String COL_ID = "ID";
    public static final String COL_FIRST_NAME = "FIRST_NAME";
    public static final String COL_LAST_NAME = "LAST_NAME";
    public static final String COL_EMAIL = "EMAIL";
    public static final String COL_PASSWORD = "PASSWORD";
    public static final String COL_PHONE = "PHONE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "FIRST_NAME TEXT, LAST_NAME TEXT, EMAIL TEXT, PASSWORD TEXT, PHONE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public boolean insertData(String firstName, String lastName, String email, String password, String phone)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_FIRST_NAME, firstName);
        contentValues.put(COL_LAST_NAME, lastName);
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_PASSWORD, password);
        contentValues.put(COL_PHONE, phone);
        long result = db.insert(TABLE_USER, null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER
                + " WHERE EMAIL = ? AND PASSWORD = ?", new String[]{email, password});
        return cursor.getCount() > 0;
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{ COL_ID, COL_FIRST_NAME, COL_LAST_NAME, COL_EMAIL },
                COL_EMAIL + "=?", new String[]{email}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
}
