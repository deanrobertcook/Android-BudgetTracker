package com.theronin.budgettracker.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.data.BudgetContract.EntriesTable;

public class BudgetProvider extends ContentProvider {

    /**
     * URL match values
     */
    public static final int CATEGORIES = 100;
    public static final int CATEGORY_WITH_ID = 101;
    public static final int ENTRIES = 200;
    public static final int ENTRY_WITH_ID = 201;

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private BudgetDbHelper dbHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = BudgetContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, CategoriesTable.PROVIDER_PATH, CATEGORIES);
        matcher.addURI(authority, CategoriesTable.PROVIDER_PATH + "/#", CATEGORY_WITH_ID);
        matcher.addURI(authority, EntriesTable.PROVIDER_PATH, ENTRIES);
        matcher.addURI(authority, EntriesTable.PROVIDER_PATH + "/#", ENTRY_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new BudgetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        int matchedCode = uriMatcher.match(uri);
        switch (matchedCode) {
            case CATEGORIES:
                return CategoriesTable.CONTENT_TYPE;
            case CATEGORY_WITH_ID:
                return CategoriesTable.CONTENT_ITEM_TYPE;
            case ENTRIES:
                return EntriesTable.CONTENT_TYPE;
            case ENTRY_WITH_ID:
                return EntriesTable.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri.toString());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(
                CategoriesTable.TABLE_NAME,
                null,
                values
        );
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
