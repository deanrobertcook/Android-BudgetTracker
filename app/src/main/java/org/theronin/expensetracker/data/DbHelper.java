package org.theronin.expensetracker.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.parse.ParseUser;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.Contract.CategoryTable;
import org.theronin.expensetracker.data.Contract.CategoryView;
import org.theronin.expensetracker.data.Contract.CurrencyTable;
import org.theronin.expensetracker.data.Contract.EntryTable;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.Contract.ExchangeRateTable;
import org.theronin.expensetracker.model.Currency;

import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    public static final String TAG = DbHelper.class.getName();

    public static final int DATABASE_VERSION = 1;

    private static DbHelper instance;

    private Context context;
    private ParseUser currentUser;

    public static synchronized DbHelper getInstance(Context context) {
        //TODO check to see if there's anything I need to do when the database changes
        if (instance == null || ParseUser.getCurrentUser() != instance.currentUser) {
            if (ParseUser.getCurrentUser() == null) {
                throw new IllegalStateException("The current user is null, there is no database " +
                        "that can be created or retrieved");
            }

            if (instance != null) {
                instance.close();
            }

            instance = new DbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DbHelper(Context context) {
        super(context, ParseUser.getCurrentUser().getObjectId(), null, DATABASE_VERSION);
        this.currentUser = ParseUser.getCurrentUser();
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
        setFullSyncWithBackend();
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

    private void setFullSyncWithBackend() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(context.getString(R.string.pref_newly_created_database), true).apply();
    }
}
