package org.theronin.budgettracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import org.theronin.budgettracker.data.BudgetContract.CategoriesView;
import org.theronin.budgettracker.data.BudgetContract.CurrenciesTable;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import org.theronin.budgettracker.data.BudgetContract.EntriesView;
import org.theronin.budgettracker.data.BudgetContract.ExchangeRatesTable;

public class BudgetDbHelper extends SQLiteOpenHelper {

    public static final String TAG = BudgetDbHelper.class.getName();

    public static final String DATABASE_NAME = "budgettracker.db";
    public static final int DATABASE_VERSION = 1;

    public BudgetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTables(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        dropTables(sqLiteDatabase);
        createTables(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        dropTables(sqLiteDatabase);
        createTables(sqLiteDatabase);
    }

    public static void createTables(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CurrenciesTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(CurrenciesTable.SQL_DEFAULT_CURRENCIES);

        sqLiteDatabase.execSQL(ExchangeRatesTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(CategoriesTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(EntriesTable.SQL_CREATE_ENTRIES_TABLE);
        sqLiteDatabase.execSQL(CategoriesView.SQL_CREATE_CATEGORIES_VIEW);
        sqLiteDatabase.execSQL(EntriesView.SQL_CREATE_CATEGORIES_VIEW);
    }

    public static void dropTables(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP VIEW IF EXISTS " + CategoriesView.VIEW_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EntriesTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExchangeRatesTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CurrenciesTable.TABLE_NAME);
    }
}
