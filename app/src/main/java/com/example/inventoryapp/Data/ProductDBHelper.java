package com.example.inventoryapp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class ProductDBHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ProductDBHelper.class.getSimpleName();

    /**
     * Name of database file
     */
    private static final String DATABASE_NAME = "storehouse.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ProductContract}
     *
     * @param context of the app
     */
    public ProductDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                ProductContract.ProductEntry.TABLE_NAME + "(" +
                ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_CONDITION + " INTEGER NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT, " +
                ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_PICTURE + " TEXT NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER DEFAULT 0);";
        Log.v(LOG_TAG, SQL_CREATE_PRODUCTS_TABLE);

        //Create Table
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    /**
     * This method is called when the database needs to be upgraded
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
