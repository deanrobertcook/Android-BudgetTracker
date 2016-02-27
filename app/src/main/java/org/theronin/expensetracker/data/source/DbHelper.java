package org.theronin.expensetracker.data.source;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.theronin.expensetracker.data.Contract.CategoryTable;
import org.theronin.expensetracker.data.Contract.CategoryView;
import org.theronin.expensetracker.data.Contract.CurrencyTable;
import org.theronin.expensetracker.data.Contract.EntryTable;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.Contract.ExchangeRateTable;
import org.theronin.expensetracker.data.SupportedCurrencies;
import org.theronin.expensetracker.model.Currency;

import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    public static final String TAG = DbHelper.class.getName();

    public static final int DATABASE_VERSION = 1;

    private static DbHelper instance;

    private String currentDatabaseName;

    public static synchronized DbHelper getInstance(Context context, String databaseName) {
        if (instance == null || !databaseName.equals(instance.currentDatabaseName)) {
            closeInstance();
            instance = new DbHelper(context.getApplicationContext(), databaseName);
        }
        return instance;
    }

    public static synchronized DbHelper getInMemoryInstance(Context context) {
        return getInstance(context, null);
    }

    private static void closeInstance() {
        if (instance != null) {
            instance.close();
        }
        instance = null;
    }

    private DbHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
        this.currentDatabaseName = databaseName;
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
        List<Currency> currencyList = new SupportedCurrencies().getList();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("INSERT INTO %s (%s, %s) VALUES ",
                CurrencyTable.TABLE_NAME,
                CurrencyTable.COL_CODE,
                CurrencyTable.COL_SYMBOL));

        int i = 0;
        for (Currency currency : currencyList) {
            sb.append(
                    String.format("('%s', '%s')", currency.code, currency.symbol)
            );
            if (i < currencyList.size() - 1) {
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }
}
