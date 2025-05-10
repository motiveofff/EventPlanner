
package com.example.eventplanner;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;


public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "events.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, datetime TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void insertEvent(String title, String datetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("datetime", datetime);
        db.insert("events", null, cv);
    }

    public Cursor getAllEvents() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM events ORDER BY datetime ASC", null);
    }
}
