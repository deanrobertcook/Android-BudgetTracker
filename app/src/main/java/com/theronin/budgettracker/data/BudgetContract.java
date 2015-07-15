package com.theronin.budgettracker.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class BudgetContract {

    public static final String CONTENT_AUTHORITY = "com.theronin.budgettracker";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CATEGORIES = "categories";
    public static final String PATH_ENTRIES = "entries";


    public static final class EntriesTable implements BaseColumns {
        public static final String TABLE_NAME = "entries";

        public static final String COL_DATE_ENTERED = "date_entered";
        public static final String COL_CATEGORY_ID = "category_id";
        public static final String COL_AMOUNT_CENTS = "amount_cents";

        public static final String SQL_CREATE_ENTRIES_TABLE = "CREATE TABLE " + EntriesTable
                .TABLE_NAME + " (" +

                EntriesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                EntriesTable.COL_DATE_ENTERED + " INTEGER NOT NULL, " +
                EntriesTable.COL_CATEGORY_ID + " INTEGER NOT NULL, " +
                EntriesTable.COL_AMOUNT_CENTS + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + EntriesTable.COL_CATEGORY_ID + ") REFERENCES " +
                CategoriesTable.TABLE_NAME + " (" + CategoriesTable._ID + "))";
    }

    public static final class CategoriesTable implements BaseColumns {
        public static final String TABLE_NAME = "categories";

        public static final String COL_DATE_CREATED = "date_entered";
        public static final String COL_CATEGORY_NAME = "category_name";

        public static final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                CategoriesTable.TABLE_NAME + " (" +

                CategoriesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                CategoriesTable.COL_CATEGORY_NAME + " TEXT NOT NULL, " +
                CategoriesTable.COL_DATE_CREATED + " DATE DEFAULT (date('now')) NOT NULL, " +

                "UNIQUE (" + CategoriesTable.COL_CATEGORY_NAME + ") ON CONFLICT IGNORE)";
    }

}
