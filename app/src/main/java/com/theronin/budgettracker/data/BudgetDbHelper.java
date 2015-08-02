package com.theronin.budgettracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.theronin.budgettracker.BuildConfig;
import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.data.BudgetContract.EntriesTable;

public class BudgetDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "budgettracker.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TAG = BudgetDbHelper.class.getName();

    public BudgetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CategoriesTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(EntriesTable.SQL_CREATE_ENTRIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // During development, just drop them and start over
        if (BuildConfig.DEBUG) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EntriesTable.TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
            onCreate(sqLiteDatabase);
        } else {
            //below is the proper way to handle Database upgrading, steadily working through
            // upgrades
            switch (oldVersion) {
                case 1:
                    //Upgrade from database version 1 -> 2
                    //No break!
                case 2:
                    //Upgrade from database version 2 -> 3, etc...
                    break;
                default:
                    throw new IllegalStateException("Unknown old version: " + oldVersion);
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EntriesTable.TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
