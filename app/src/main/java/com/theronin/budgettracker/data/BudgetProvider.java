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

        String id;
        switch (uriMatcher.match(uri)) {
            case CATEGORIES:
                return queryCategories(projection, selection, selectionArgs);
            case CATEGORY_WITH_ID:
                id = uri.getLastPathSegment();
                return queryCategoryWithId(id, projection);
            case ENTRIES:
                return queryEntries(projection, selection, selectionArgs);
            case ENTRY_WITH_ID:
                id = uri.getLastPathSegment();
                return queryEntryWithId(id, projection);
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri.toString());
        }
    }

    private Cursor queryCategoryWithId(String id, String[] projection) {
        return dbHelper.getReadableDatabase().query(
                CategoriesTable.TABLE_NAME,
                projection,
                CategoriesTable._ID + " = ?",
                new String[]{id},
                null, null, null
        );
    }

    private Cursor queryCategories(String[] projection, String selection, String[] selectionArgs) {
        return dbHelper.getReadableDatabase().query(
                CategoriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
    }

    private Cursor queryEntryWithId(String id, String[] projection) {
        return dbHelper.getReadableDatabase().query(
                EntriesTable.TABLE_NAME,
                projection,
                EntriesTable._ID + " = ?",
                new String[]{id},
                null, null, null
        );
    }

    private Cursor queryEntries(String[] projection, String selection, String[] selectionArgs) {
        return dbHelper.getReadableDatabase().query(
                EntriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
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
        switch (uriMatcher.match(uri)) {
            case CATEGORIES:
                return insertCategory(values);
            case ENTRIES:
                return insertEntry(values);
            default:
                throw new UnsupportedOperationException("Unknown or invalid Uri: " + uri.toString
                        ());
        }
    }

    private Uri insertCategory(ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long categoryId = db.insert(CategoriesTable.TABLE_NAME, null, values);
        return CategoriesTable.CONTENT_URI.buildUpon().appendPath(Long.toString(categoryId))
                .build();
    }

    private Uri insertEntry(ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long entryId = db.insert(EntriesTable.TABLE_NAME, null, values);
        return EntriesTable.CONTENT_URI.buildUpon().appendPath(Long.toString(entryId))
                .build();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case ENTRY_WITH_ID:
                String entryId = uri.getLastPathSegment();
                return dbHelper.getWritableDatabase().delete(
                        EntriesTable.TABLE_NAME,
                        EntriesTable._ID + " = ?",
                        new String[]{entryId});

            default:
                throw new UnsupportedOperationException("Unknown or invalid Uri: " +
                        uri.toString());
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
