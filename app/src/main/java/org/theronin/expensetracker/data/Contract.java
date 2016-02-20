package org.theronin.expensetracker.data;

import android.provider.BaseColumns;

import org.theronin.expensetracker.data.backend.entry.SyncState;

public class Contract {
    
    public static final class CategoryTable implements BaseColumns {
        public static final String TABLE_NAME = "category_base";

        public static final String COL_NAME = "name";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                "UNIQUE (" + COL_NAME + ") ON CONFLICT IGNORE)";
    }

    public static final class EntryTable implements BaseColumns {
        public static final String TABLE_NAME = "entry_base";

        public static final String COL_GLOBAL_ID = "global_id";
        public static final String COL_DATE = "date";
        public static final String COL_CATEGORY_ID = "category_id";
        public static final String COL_AMOUNT = "amount";
        public static final String COL_CURRENCY_ID = "currency_id";
        public static final String COL_SYNC_STATUS = "sync_status";

        public static final String SQL_CREATE_ENTRIES_TABLE = "CREATE TABLE " + EntryTable
                .TABLE_NAME + " (" +

                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_GLOBAL_ID + " TEXT DEFAULT NULL, " +
                COL_SYNC_STATUS + " TEXT NOT NULL DEFAULT '" + SyncState.NEW + "', " +

                COL_DATE + " INTEGER NOT NULL, " +
                COL_CATEGORY_ID + " INTEGER NOT NULL, " +
                COL_AMOUNT + " INTEGER NOT NULL, " +
                COL_CURRENCY_ID + " INTEGER NOT NULL, " +

                "UNIQUE (" + COL_GLOBAL_ID + ") ON CONFLICT IGNORE, " +

                "FOREIGN KEY (" + COL_CATEGORY_ID + ") REFERENCES " +
                CurrencyTable.TABLE_NAME + " (" + CategoryTable._ID + "), " +

                "FOREIGN KEY (" + COL_CURRENCY_ID + ") REFERENCES " +
                CurrencyTable.TABLE_NAME + " (" + CurrencyTable._ID + "))";
    }

    public static final class CurrencyTable implements BaseColumns {
        public static final String TABLE_NAME = "currency";

        public static final String COL_CODE = "code";
        public static final String COL_SYMBOL = "symbol";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_CODE + " TEXT NOT NULL, " +
                COL_SYMBOL + " TEXT NOT NULL, " +
                "UNIQUE (" + COL_CODE + ") ON CONFLICT IGNORE)";

        public static final String[] PROJECTION = {
                _ID,
                COL_CODE,
                COL_SYMBOL
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_CODE = 1;
        public static final int INDEX_SYMBOL = 2;
    }

    public static final class ExchangeRateTable implements BaseColumns {
        public static final String TABLE_NAME = "exchange_rate";

        public static final String COL_CURRENCY_CODE = "currency_code";
        public static final String COL_DATE = "date";
        public static final String COL_USD_RATE = "usd_rate";

        /**
         * The last downlaoded attempt column will only contain a positive (valid) value when
         * there was a failed download attempt (download attempts is incremented).
         */
        public static final String COL_LAST_DOWNLOAD_ATTEMPT = "last_download_attempt";
        public static final String COL_DOWNLOAD_ATTEMPTS = "download_attempts";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_CURRENCY_CODE + " TEXT NOT NULL, " +
                COL_DATE + " INTEGER NOT NULL, " +
                COL_USD_RATE + " REAL NOT NULL DEFAULT -1.0, " +
                COL_LAST_DOWNLOAD_ATTEMPT + " INTEGER NOT NULL DEFAULT 0, " +
                COL_DOWNLOAD_ATTEMPTS + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (" + COL_CURRENCY_CODE + ") REFERENCES " + CurrencyTable.TABLE_NAME + " (" + CurrencyTable.COL_CODE + "), " +
                "UNIQUE (" + COL_CURRENCY_CODE + ", " + COL_DATE + ") ON CONFLICT IGNORE)";

        public static final String[] PROJECTION = {
                _ID,
                COL_CURRENCY_CODE,
                COL_DATE,
                COL_USD_RATE,
                COL_LAST_DOWNLOAD_ATTEMPT,
                COL_DOWNLOAD_ATTEMPTS
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_CURRENCY_CODE = 1;
        public static final int INDEX_DATE = 2;
        public static final int INDEX_USD_RATE = 3;
        public static final int INDEX_LAST_DOWNLOAD_ATTEMPT = 4;
        public static final int INDEX_DOWNLOAD_ATTEMPTS = 5;
    }

    public static final class CategoryView implements BaseColumns {
        public static final String VIEW_NAME = "category";

        public static final String COL_CATEGORY_NAME = CategoryTable.COL_NAME;
        public static final String COL_FIRST_ENTRY_DATE = "first_entry_date";
        public static final String COL_ENTRY_FREQUENCY = "entry_frequency";

        public static final String SQL_CREATE_CATEGORIES_VIEW =
                "CREATE VIEW " + VIEW_NAME + " AS " +

                "SELECT " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable._ID + " AS " + _ID + ", " +

                        CategoryTable.TABLE_NAME + "." + CategoryTable.COL_NAME + " AS " + COL_CATEGORY_NAME + ", " +
                        "MIN(" + EntryTable.TABLE_NAME + "." + EntryTable.COL_DATE+ ") AS " + COL_FIRST_ENTRY_DATE + ", " +
                        "COUNT(" + EntryTable.TABLE_NAME + "." + EntryTable._ID + ")" + " AS " + COL_ENTRY_FREQUENCY + " " +

                "FROM " +
                        CategoryTable.TABLE_NAME + " LEFT OUTER JOIN " + EntryTable.TABLE_NAME + " " +

                "ON " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable._ID + " = " + EntryTable.COL_CATEGORY_ID + " " +

                "GROUP BY " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable._ID + ", " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable.COL_NAME;

        public static final String[] PROJECTION = {
                _ID,
                COL_CATEGORY_NAME,
                COL_FIRST_ENTRY_DATE,
                COL_ENTRY_FREQUENCY,
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_CATEGORY_NAME = 1;
        public static final int INDEX_FIRST_ENTRY_DATE = 2;
        public static final int INDEX_ENTRY_FREQUENCY = 3;
    }

    public static final class EntryView implements BaseColumns {
        public static final String VIEW_NAME = "entry";

        public static final String COL_GLOBAL_ID = EntryTable.COL_GLOBAL_ID;
        public static final String COL_SYNC_STATUS = EntryTable.COL_SYNC_STATUS;

        public static final String COL_DATE = EntryTable.COL_DATE;
        public static final String COL_AMOUNT = EntryTable.COL_AMOUNT;

        public static final String COL_CATEGORY_ID = EntryTable.COL_CATEGORY_ID;
        public static final String COL_CATEGORY_NAME = "category_name";

        public static final String COL_CURRENCY_ID = EntryTable.COL_CURRENCY_ID;
        public static final String COL_CURRENCY_CODE = "currency_code";
        public static final String COL_CURRENCY_SYMBOL = "currency_symbol";

        public static final String SQL_CREATE_CATEGORIES_VIEW =
                "CREATE VIEW " + VIEW_NAME + " AS " +

                "SELECT " +
                        EntryTable.TABLE_NAME + "." + EntryTable._ID + " AS " + _ID + ", " +

                        EntryTable.TABLE_NAME + "." + EntryTable.COL_GLOBAL_ID + " AS " + COL_GLOBAL_ID + ", " +
                        EntryTable.TABLE_NAME + "." + EntryTable.COL_SYNC_STATUS + " AS " + COL_SYNC_STATUS + ", " +

                        EntryTable.TABLE_NAME + "." + EntryTable.COL_DATE + " AS " + COL_DATE + ", " +
                        EntryTable.TABLE_NAME + "." + EntryTable.COL_AMOUNT + " AS " + COL_AMOUNT + ", " +

                        EntryTable.TABLE_NAME + "." + EntryTable.COL_CATEGORY_ID + " AS " + COL_CATEGORY_ID + ", " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable.COL_NAME + " AS " + COL_CATEGORY_NAME + ", " +

                        EntryTable.TABLE_NAME + "." + EntryTable.COL_CURRENCY_ID + " AS " + COL_CURRENCY_ID + ", " +
                        CurrencyTable.TABLE_NAME  + "." + CurrencyTable.COL_CODE + " AS " + COL_CURRENCY_CODE + ", " +
                        CurrencyTable.TABLE_NAME + "." + CurrencyTable.COL_SYMBOL + " AS " + COL_CURRENCY_SYMBOL + " " +

                "FROM " +
                        EntryTable.TABLE_NAME +
                        " JOIN " + CategoryTable.TABLE_NAME +
                            " ON " + EntryTable.TABLE_NAME + "." + EntryTable.COL_CATEGORY_ID + " = " + CategoryTable.TABLE_NAME + "." + CategoryTable._ID + " " +
                        " JOIN " + CurrencyTable.TABLE_NAME +
                            " ON " + EntryTable.TABLE_NAME + "." + EntryTable.COL_CURRENCY_ID + " = " + CurrencyTable.TABLE_NAME + "." + CurrencyTable._ID;

        public static final String[] PROJECTION = {
                _ID,

                COL_GLOBAL_ID,
                COL_SYNC_STATUS,

                COL_DATE,
                COL_AMOUNT,

                COL_CATEGORY_ID,
                COL_CATEGORY_NAME,

                COL_CURRENCY_ID,
                COL_CURRENCY_CODE,
                COL_CURRENCY_SYMBOL
        };

        public static final int INDEX_ID = 0;

        public static final int INDEX_GLOBAL_ID = 1;
        public static final int INDEX_SYNC_STATUS = 2;

        public static final int INDEX_DATE = 3;
        public static final int INDEX_AMOUNT = 4;

        public static final int INDEX_CATEGORY_ID = 5;
        public static final int INDEX_CATEGORY_NAME = 6;

        public static final int INDEX_CURRENCY_ID = 7;
        public static final int INDEX_CURRENCY_CODE = 8;
        public static final int INDEX_CURRENCY_SYMBOL = 9;
    }
}
