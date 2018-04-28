package com.example.android.sneakerinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.sneakerinventory.data.SneakerContract.*;

/**
 * Created by Mrome on 2/19/2018.
 */

public class SneakerDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public SneakerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the sneakers table
        String SQL_CREATE_SNEAKERS_TABLE =  "CREATE TABLE " + SneakerEntry.TABLE_NAME + " ("
                + SneakerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SneakerEntry.COLUMN_SNEAKER_NAME + " TEXT NOT NULL, "
                + SneakerEntry.COLUMN_SNEAKER_PRICE + " INTEGER NOT NULL, "
                + SneakerEntry.COLUMN_SNEAKER_QUANTITY + " INTEGER DEFAULT 0, "
                + SneakerEntry.COLUMN_IMAGE + " BLOB, "
                + SneakerEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_SNEAKERS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
