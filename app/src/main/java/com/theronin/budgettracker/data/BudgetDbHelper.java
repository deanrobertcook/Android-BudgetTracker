package com.theronin.budgettracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.theronin.budgettracker.BuildConfig;
import com.theronin.budgettracker.data.BudgetContractV2.CategoriesTable;
import com.theronin.budgettracker.data.BudgetContractV2.EntriesTable;

public class BudgetDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "budgettracker.db";
    public static final int DATABASE_VERSION = 2;

    public BudgetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public BudgetDbHelper(Context context, int databaseVersion, String databaseName) {
        super(context, databaseName, null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTables(sqLiteDatabase);
    }

    public static void createTables(SQLiteDatabase sqLiteDatabase) {
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
            switch (oldVersion) {
                case 1:
                    sqLiteDatabase.execSQL(upgrade1To2());
                    break;
                default:
                    throw new IllegalStateException("Unknown old version: " + oldVersion);
            }
        }
    }

    private String upgrade1To2() {
        return "ALTER TABLE " + CategoriesTable.TABLE_NAME +
                "RENAME COLUMN date_created to " + CategoriesTable.COL_FIRST_ENTRY_DATE + ", " +
                "ADD (" + CategoriesTable.COL_TOTAL_AMOUNT + " INTEGER DEFAULT 0 NOT NULL, " +
                CategoriesTable.COL_ENTRY_FREQUENCY + " INTEGER DEFAULT 0 NOT NULL)";
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) {
            dropTables(sqLiteDatabase);
            createTables(sqLiteDatabase);
        }
    }

    public static void dropTables(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EntriesTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
    }
}
