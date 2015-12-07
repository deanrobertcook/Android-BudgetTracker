package org.theronin.budgettracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.theronin.budgettracker.R;
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

    private static BudgetDbHelper instance;

    private Context context;

    public static synchronized BudgetDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BudgetDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private BudgetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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

    public void createTables(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CurrenciesTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(buildDefaultCurrenciesQuery());

        sqLiteDatabase.execSQL(ExchangeRatesTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(CategoriesTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(EntriesTable.SQL_CREATE_ENTRIES_TABLE);
        sqLiteDatabase.execSQL(CategoriesView.SQL_CREATE_CATEGORIES_VIEW);
        sqLiteDatabase.execSQL(EntriesView.SQL_CREATE_CATEGORIES_VIEW);
    }

    public void dropTables(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP VIEW IF EXISTS " + CategoriesView.VIEW_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EntriesTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExchangeRatesTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CurrenciesTable.TABLE_NAME);
    }

    private String buildDefaultCurrenciesQuery() {
        String[] codes = context.getResources().getStringArray(R.array.currency_codes);
        String[] symbols = context.getResources().getStringArray(R.array.currency_symbols);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("INSERT INTO %s (%s, %s) VALUES ",
                CurrenciesTable.TABLE_NAME,
                CurrenciesTable.COL_CODE,
                CurrenciesTable.COL_SYMBOL));

        for (int i = 0; i < codes.length; i++) {
            sb.append(
                    String.format("('%s', '%s')", codes[i], symbols[i])
            );
            if (i < codes.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
