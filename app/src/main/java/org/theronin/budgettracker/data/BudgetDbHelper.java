package org.theronin.budgettracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.theronin.budgettracker.BuildConfig;
import org.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;

public class BudgetDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "budgettracker.db";
    public static final int DATABASE_VERSION = 2;

    public BudgetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        switch (oldVersion) {
            case 1:
                upgrade1To2(sqLiteDatabase);
                break;
            default:
                throw new IllegalStateException("Unknown old version: " + oldVersion);
        }
    }

    private void upgrade1To2(SQLiteDatabase database) {
        database.beginTransaction();
        try {
            database.execSQL("ALTER TABLE " + CategoriesTable.TABLE_NAME + " RENAME TO tmp_table");
            database.execSQL(CategoriesTable.SQL_CREATE_CATEGORIES_TABLE);
            database.execSQL("INSERT INTO " + CategoriesTable.TABLE_NAME + " (" +
                        CategoriesTable._ID + ", " +
                        CategoriesTable.COL_FIRST_ENTRY_DATE + ", " +
                        CategoriesTable.COL_CATEGORY_NAME + ") " +
                    "SELECT " +
                        CategoriesTable._ID + ", " +
                        "date_created, " +
                        CategoriesTable.COL_CATEGORY_NAME + " " +
                        "FROM tmp_table");
            database.execSQL("DROP TABLE tmp_table");
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
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
