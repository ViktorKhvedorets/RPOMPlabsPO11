package com.example.kozinlab3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBInit extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "newnotes.db";
    private static final String DATABASE_PATH = "/data/data/com.example.kozinlab3/databases/";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_COUNTRY = "country";

    private final Context context;
    private SQLiteDatabase database;

    public DBInit(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    public void copyDatabaseFromAssets() {
        try {
            InputStream inputStream = context.getAssets().open(DATABASE_NAME);
            String outFileName = DATABASE_PATH + DATABASE_NAME;

            OutputStream outputStream = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DbInit", "Ошибка копирования базы данных", e);
        }
    }

    private boolean checkDatabaseExists() {
        SQLiteDatabase db = null;
        try {
            String path = DATABASE_PATH + DATABASE_NAME;
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            Log.e("DbInit", "База данных не существует", e);
        }
        if (db != null) {
            db.close();
            return true;
        }
        return false;
    }

    public void initializeDatabase() {
        if (!checkDatabaseExists()) {
            this.getReadableDatabase();
            copyDatabaseFromAssets();
        }
    }

    public void addNote(String country) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COUNTRY, country);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public boolean deleteNote(int id) {
        // Получаем ссылку на базу данных с правами на запись
        SQLiteDatabase db = this.getWritableDatabase();

        // Удаляем запись по id
        int rowsDeleted = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();  // Закрываем базу данных

        // Возвращаем true, если хотя бы одна строка была удалена
        return rowsDeleted > 0;
    }


    public void updateNote(int id, String country) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COUNTRY, country);
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}