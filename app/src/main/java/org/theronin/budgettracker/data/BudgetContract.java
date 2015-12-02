package org.theronin.budgettracker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import org.theronin.budgettracker.BuildConfig;

public class BudgetContract {

    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class CategoriesTable implements BaseColumns {
        /**
         * SQLite constants
         */
        public static final String TABLE_NAME = "categories_base";

        public static final String COL_CATEGORY_NAME = "category_name";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CATEGORY_NAME + " TEXT NOT NULL, " +
                "UNIQUE (" + COL_CATEGORY_NAME + ") ON CONFLICT IGNORE)";
    }

    public static final class CategoriesView implements BaseColumns {

        /**
         * Provider constants
         */
        public static final String PROVIDER_PATH = "category";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath
                (PROVIDER_PATH).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "." + PROVIDER_PATH;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "." + PROVIDER_PATH;

        /**
         * SQLite constants
         */
        public static final String VIEW_NAME = "categories";

        public static final String COL_FIRST_ENTRY_DATE = "first_entry_date";
        public static final String COL_TOTAL_AMOUNT = "total_amount_cents";
        public static final String COL_ENTRY_FREQUENCY = "entry_frequency";
        //these need to be the same so the business layer doesn't need to know about categories_base
        public static final String COL_CATEGORY_NAME = CategoriesTable.COL_CATEGORY_NAME;

    //@formatter:off
        public static final String SQL_CREATE_CATEGORIES_VIEW =
                "CREATE VIEW " + VIEW_NAME + " AS " +

                "SELECT " +
                        CategoriesTable.TABLE_NAME + "." + CategoriesTable._ID + " AS " + CategoriesView._ID + ", " +

                        "MIN(" + EntriesTable.TABLE_NAME + "." + EntriesTable.COL_DATE_ENTERED+ ") AS " + COL_FIRST_ENTRY_DATE + ", " +
                        "IFNULL(SUM(" + EntriesTable.TABLE_NAME + "." + EntriesTable.COL_AMOUNT_CENTS + "), 0) AS " + COL_TOTAL_AMOUNT + ", " +
                        "COUNT(" + EntriesTable.TABLE_NAME + "." + EntriesTable._ID + ")" + " AS " + COL_ENTRY_FREQUENCY + ", " +
                        CategoriesTable.TABLE_NAME + "." + CategoriesTable.COL_CATEGORY_NAME + " AS " + COL_CATEGORY_NAME + " " +

                "FROM " +
                        CategoriesTable.TABLE_NAME + " LEFT OUTER JOIN " + EntriesTable.TABLE_NAME + " " +

                "ON " +
                        CategoriesTable.TABLE_NAME + "." + CategoriesTable._ID + " = " + EntriesTable.COL_CATEGORY_ID + " " +

                "GROUP BY " +
                        CategoriesTable.TABLE_NAME + "." + CategoriesTable._ID + ", " +
                        CategoriesTable.TABLE_NAME + "." + CategoriesTable.COL_CATEGORY_NAME;

    //@formatter:on

        /**
         * Helper projections for faster query write-ups
         */
        public static final String[] RAW_PROJECTION = {
                _ID,
                COL_FIRST_ENTRY_DATE,
                COL_TOTAL_AMOUNT,
                COL_ENTRY_FREQUENCY,
                COL_CATEGORY_NAME
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_FIRST_ENTRY_DATE = 1;
        public static final int INDEX_TOTAL_AMOUNT = 2;
        public static final int INDEX_ENTRY_FREQUENCY = 3;
        public static final int INDEX_CATEGORY_NAME = 4;
    }

    public static final class EntriesTable implements BaseColumns {

        /**
         * Provider constants
         */
        public static final String PROVIDER_PATH = "entry";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath
                (PROVIDER_PATH).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "." + PROVIDER_PATH;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "." + PROVIDER_PATH;

        /**
         * SQLite constants
         */
        public static final String TABLE_NAME = "entries";

        public static final String COL_DATE_ENTERED = "date_entered";
        public static final String COL_CATEGORY_ID = "category_id";
        public static final String COL_AMOUNT_CENTS = "amount_cents";
        public static final String COL_CURRENCY_CODE = "currency_entered";

        public static final String SQL_CREATE_ENTRIES_TABLE = "CREATE TABLE " + EntriesTable
                .TABLE_NAME + " (" +

                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_DATE_ENTERED + " INTEGER NOT NULL, " +
                COL_CATEGORY_ID + " INTEGER NOT NULL, " +
                COL_AMOUNT_CENTS + " INTEGER NOT NULL, " +
                COL_CURRENCY_CODE + " TEXT NOT NULL, " +

                "FOREIGN KEY (" + COL_CATEGORY_ID + ") REFERENCES " +
                CategoriesView.VIEW_NAME + " (" + CategoriesView._ID + "), " +

                "FOREIGN KEY (" + COL_CURRENCY_CODE + ") REFERENCES " +
                CurrenciesTable.TABLE_NAME + " (" + CurrenciesTable.COL_CODE + "))";

        /**
         * Helper projections for faster query write-ups
         */
        public static final String[] RAW_PROJECTION = {
                _ID,
                COL_DATE_ENTERED,
                COL_CATEGORY_ID,
                COL_AMOUNT_CENTS
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_DATE_ENTERED = 1;
        public static final int INDEX_CATEGORY_ID = 2;
        public static final int INDEX_AMOUNT_CENTS = 3;
    }

    public static final class CurrenciesTable implements BaseColumns {
        /**
         * SQLite constants
         */
        public static final String TABLE_NAME = "currencies";

        public static final String COL_CODE = "code";
        public static final String COL_SYMBOL = "symbol";
        public static final String COL_NAME = "name";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_CODE + " TEXT NOT NULL, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_SYMBOL + " TEXT NOT NULL, " +
                "UNIQUE (" + COL_CODE + ") ON CONFLICT IGNORE)";
    }

    public static final class ExchangeRatesTable implements BaseColumns {
        /**
         * SQLite constants
         */
        public static final String TABLE_NAME = "exchange_rates";

        public static final String COL_CURRENCY_CODE = "currency_code";
        public static final String COL_DATE = "date";
        public static final String COL_USD_RATE = "usd_rate";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_CURRENCY_CODE + " TEXT NOT NULL, " +
                COL_DATE + " INTEGER NOT NULL, " +
                COL_USD_RATE + " REAL NOT NULL, " +
                "FOREIGN KEY (" + COL_CURRENCY_CODE + ") REFERENCES " + CurrenciesTable.TABLE_NAME + " (" + CurrenciesTable.COL_CODE + "), " +
                "UNIQUE (" + COL_CURRENCY_CODE + ", " + COL_DATE + ") ON CONFLICT IGNORE)";
    }
}
