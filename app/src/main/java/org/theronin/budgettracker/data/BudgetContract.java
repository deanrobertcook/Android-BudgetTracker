package org.theronin.budgettracker.data;

import android.provider.BaseColumns;

public class BudgetContract {

    public static final class CategoryTable implements BaseColumns {
        /**
         * SQLite constants
         */
        public static final String TABLE_NAME = "category_base";

        public static final String COL_NAME = "name";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                "UNIQUE (" + COL_NAME + ") ON CONFLICT IGNORE)";

        /**
         * Helper projections for faster query write-ups
         */
        public static final String[] PROJECTION = {
                _ID,
                COL_NAME
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_NAME = 1;
    }

    public static final class EntryTable implements BaseColumns {

        /**
         * SQLite constants
         */
        public static final String TABLE_NAME = "entry_base";

        public static final String COL_DATE = "date";
        public static final String COL_CATEGORY_ID = "category_id";
        public static final String COL_AMOUNT = "amount";
        public static final String COL_CURRENCY_ID = "currency_id";

        public static final String SQL_CREATE_ENTRIES_TABLE = "CREATE TABLE " + EntryTable
                .TABLE_NAME + " (" +

                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_DATE + " INTEGER NOT NULL, " +
                COL_CATEGORY_ID + " INTEGER NOT NULL, " +
                COL_AMOUNT + " INTEGER NOT NULL, " +
                COL_CURRENCY_ID + " INTEGER NOT NULL, " +

                "FOREIGN KEY (" + COL_CATEGORY_ID + ") REFERENCES " +
                CategoryView.VIEW_NAME + " (" + CategoryView._ID + "), " +

                "FOREIGN KEY (" + COL_CURRENCY_ID + ") REFERENCES " +
                CurrencyTable.TABLE_NAME + " (" + CurrencyTable._ID + "))";

        /**
         * Helper projections for faster query write-ups
         */
        public static final String[] PROJECTION = {
                _ID,
                COL_DATE,
                COL_CATEGORY_ID,
                COL_AMOUNT,
                COL_CURRENCY_ID
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_DATE = 1;
        public static final int INDEX_CATEGORY_ID = 2;
        public static final int INDEX_AMOUNT = 3;
        public static final int INDEX_CURRENCY_ENTERED = 4;
    }

    public static final class CurrencyTable implements BaseColumns {
        /**
         * SQLite constants
         */
        public static final String TABLE_NAME = "currency";

        public static final String COL_CODE = "code";
        public static final String COL_SYMBOL = "symbol";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_CODE + " TEXT NOT NULL, " +
                COL_SYMBOL + " TEXT NOT NULL, " +
                "UNIQUE (" + COL_CODE + ") ON CONFLICT IGNORE)";

        /**
         * Helper projections for faster query write-ups
         */
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
        /**
         * SQLite constants
         */
        public static final String TABLE_NAME = "exchange_rate";

        public static final String COL_CURRENCY_CODE = "currency_code";
        public static final String COL_DATE = "date";
        public static final String COL_USD_RATE = "usd_rate";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_CURRENCY_CODE + " TEXT NOT NULL, " +
                COL_DATE + " INTEGER NOT NULL, " +
                COL_USD_RATE + " REAL NOT NULL, " +
                "FOREIGN KEY (" + COL_CURRENCY_CODE + ") REFERENCES " + CurrencyTable.TABLE_NAME + " (" + CurrencyTable.COL_CODE + "), " +
                "UNIQUE (" + COL_CURRENCY_CODE + ", " + COL_DATE + ") ON CONFLICT IGNORE)";
    }

    public static final class CategoryView implements BaseColumns {
        /**
         * SQLite constants
         */
        public static final String VIEW_NAME = "category";

        public static final String COL_CATEGORY_NAME = CategoryTable.COL_NAME;
        public static final String COL_FIRST_ENTRY_DATE = "first_entry_date";
        public static final String COL_TOTAL_AMOUNT = "total_amount";
        public static final String COL_ENTRY_FREQUENCY = "entry_frequency";

    //@formatter:off
        public static final String SQL_CREATE_CATEGORIES_VIEW =
                "CREATE VIEW " + VIEW_NAME + " AS " +

                "SELECT " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable._ID + " AS " + _ID + ", " +

                        CategoryTable.TABLE_NAME + "." + CategoryTable.COL_NAME + " AS " + COL_CATEGORY_NAME + ", " +
                        "MIN(" + EntryTable.TABLE_NAME + "." + EntryTable.COL_DATE+ ") AS " + COL_FIRST_ENTRY_DATE + ", " +
                        "IFNULL(SUM(" + EntryTable.TABLE_NAME + "." + EntryTable.COL_AMOUNT + "), 0) AS " + COL_TOTAL_AMOUNT + ", " +
                        "COUNT(" + EntryTable.TABLE_NAME + "." + EntryTable._ID + ")" + " AS " + COL_ENTRY_FREQUENCY + " " +

                "FROM " +
                        CategoryTable.TABLE_NAME + " LEFT OUTER JOIN " + EntryTable.TABLE_NAME + " " +

                "ON " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable._ID + " = " + EntryTable.COL_CATEGORY_ID + " " +

                "GROUP BY " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable._ID + ", " +
                        CategoryTable.TABLE_NAME + "." + CategoryTable.COL_NAME;

    //@formatter:on

        /**
         * Helper projections for faster query write-ups
         */
        public static final String[] PROJECTION = {
                _ID,
                COL_CATEGORY_NAME,
                COL_FIRST_ENTRY_DATE,
                COL_TOTAL_AMOUNT,
                COL_ENTRY_FREQUENCY,
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_CATEGORY_NAME = 1;
        public static final int INDEX_FIRST_ENTRY_DATE = 2;
        public static final int INDEX_TOTAL_AMOUNT = 3;
        public static final int INDEX_ENTRY_FREQUENCY = 4;
    }

    public static final class EntryView implements BaseColumns {
        /**
         * SQLite constants
         */
        public static final String VIEW_NAME = "entry";

        public static final String COL_DATE = EntryTable.COL_DATE;
        public static final String COL_AMOUNT = EntryTable.COL_AMOUNT;

        public static final String COL_CATEGORY_ID = EntryTable.COL_CATEGORY_ID;
        public static final String COL_CATEGORY_NAME = "currency_name";

        public static final String COL_CURRENCY_ID = EntryTable.COL_CURRENCY_ID;
        public static final String COL_CURRENCY_CODE = "currency_code";
        public static final String COL_CURRENCY_SYMBOL = "currency_symbol";

        //@formatter:off
        public static final String SQL_CREATE_CATEGORIES_VIEW =
                "CREATE VIEW " + VIEW_NAME + " AS " +

                "SELECT " +
                        EntryTable.TABLE_NAME + "." + EntryTable._ID + " AS " + _ID + ", " +

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

    //@formatter:on

        /**
         * Helper projections for faster query write-ups
         */
        public static final String[] PROJECTION = {
                _ID,
                COL_DATE,
                COL_AMOUNT,

                COL_CATEGORY_ID,
                COL_CATEGORY_NAME,

                COL_CURRENCY_ID,
                COL_CURRENCY_CODE,
                COL_CURRENCY_SYMBOL
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_DATE = 1;
        public static final int INDEX_AMOUNT = 2;

        public static final int INDEX_CATEGORY_ID = 3;
        public static final int INDEX_CATEGORY_NAME = 4;

        public static final int INDEX_CURRENCY_ID = 5;
        public static final int INDEX_CURRENCY_CODE = 6;
        public static final int INDEX_CURRENCY_SYMBOL = 7;
    }
}
