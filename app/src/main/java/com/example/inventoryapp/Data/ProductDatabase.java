package com.example.inventoryapp.Data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.inventoryapp.Constant;

@Database(entities = {ProductEntry.class},version = 1,exportSchema = false)
public abstract class ProductDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static ProductDatabase sInstance;

    public static ProductDatabase getsInstance(Context context) {
        if(sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        ProductDatabase.class, Constant.DATABASE_NAME).build();
            }
        }
        return sInstance;
    }
    public abstract ProductDao productDao();
}
