package org.theronin.budgettracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.BudgetContract.CategoryTable;
import org.theronin.budgettracker.data.BudgetContract.CategoryView;
import org.theronin.budgettracker.data.BudgetContract.CurrencyTable;
import org.theronin.budgettracker.data.BudgetContract.EntryTable;
import org.theronin.budgettracker.data.BudgetContract.EntryView;
import org.theronin.budgettracker.data.BudgetContract.ExchangeRateTable;

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
        sqLiteDatabase.execSQL(CurrencyTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(buildDefaultCurrenciesQuery());

        sqLiteDatabase.execSQL(ExchangeRateTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(CategoryTable.SQL_CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(EntryTable.SQL_CREATE_ENTRIES_TABLE);
        sqLiteDatabase.execSQL(CategoryView.SQL_CREATE_CATEGORIES_VIEW);
        sqLiteDatabase.execSQL(EntryView.SQL_CREATE_CATEGORIES_VIEW);
    }

    public void dropTables(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP VIEW IF EXISTS " + CategoryView.VIEW_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EntryTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoryTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExchangeRateTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CurrencyTable.TABLE_NAME);
    }

    private String buildDefaultCurrenciesQuery() {
        String[] codes = context.getResources().getStringArray(R.array.currency_codes);
        String[] symbols = context.getResources().getStringArray(R.array.currency_symbols);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("INSERT INTO %s (%s, %s) VALUES ",
                CurrencyTable.TABLE_NAME,
                CurrencyTable.COL_CODE,
                CurrencyTable.COL_SYMBOL));

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
