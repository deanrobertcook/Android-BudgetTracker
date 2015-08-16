package com.theronin.budgettracker.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.data.BudgetContract.EntriesTable;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.Entry;

import static com.theronin.budgettracker.data.BudgetProvider.DatabaseUtils.queryCategoryById;
import static com.theronin.budgettracker.data.BudgetProvider.DatabaseUtils.queryEntryById;

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

    private static final SQLiteQueryBuilder entryJoinedOnCategoryQueryBuilder;

    static {
        entryJoinedOnCategoryQueryBuilder = new SQLiteQueryBuilder();

        entryJoinedOnCategoryQueryBuilder.setTables(
                EntriesTable.TABLE_NAME + " LEFT JOIN " + CategoriesTable.TABLE_NAME +
                        " ON " +
                        EntriesTable.TABLE_NAME + "." + EntriesTable.COL_CATEGORY_ID + " = " +
                        CategoriesTable.TABLE_NAME + "." + CategoriesTable._ID
        );
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
        Cursor cursor = dbHelper.getReadableDatabase().query(
                CategoriesTable.TABLE_NAME,
                projection,
                CategoriesTable._ID + " = ?",
                new String[]{id},
                null, null, null
        );
        cursor.setNotificationUri(getContext().getContentResolver(), CategoriesTable.CONTENT_URI);
        return cursor;
    }

    private Cursor queryCategories(String[] projection, String selection, String[] selectionArgs) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                CategoriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
        cursor.setNotificationUri(getContext().getContentResolver(), CategoriesTable.CONTENT_URI);
        return cursor;
    }

    private Cursor queryEntryWithId(String id, String[] projection) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                EntriesTable.TABLE_NAME,
                projection,
                EntriesTable._ID + " = ?",
                new String[]{id},
                null, null, null
        );
        cursor.setNotificationUri(getContext().getContentResolver(), EntriesTable.CONTENT_URI);
        return cursor;
    }

    private Cursor queryEntries(String[] projection, String selection, String[] selectionArgs) {
        Cursor cursor = entryJoinedOnCategoryQueryBuilder.query(
                dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
        cursor.setNotificationUri(getContext().getContentResolver(), EntriesTable.CONTENT_URI);
        return cursor;
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
        getContext().getContentResolver().notifyChange(CategoriesTable.CONTENT_URI, null);
        return CategoriesTable.CONTENT_URI.buildUpon().appendPath(Long.toString(categoryId))
                .build();
    }

    private Uri insertEntry(ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long entryId = db.insert(EntriesTable.TABLE_NAME, null, values);
        updateCategoryOnInsertEntry(values);

        getContext().getContentResolver().notifyChange(EntriesTable.CONTENT_URI, null);
        getContext().getContentResolver().notifyChange(CategoriesTable.CONTENT_URI, null);

        return EntriesTable.CONTENT_URI.buildUpon().appendPath(Long.toString(entryId))
                .build();
    }
    /**
     * Updates the earliest Entry date, the Entry frequency and the total sum amount of all
     * Entries for the given categoryId.
     * @param entryValues
     * @return
     */
    private void updateCategoryOnInsertEntry(ContentValues entryValues) {
        String categoryId = entryValues.getAsString(EntriesTable.COL_CATEGORY_ID);
        String entryDate = entryValues.getAsString(EntriesTable.COL_DATE_ENTERED);
        Long entryAmount = entryValues.getAsLong(EntriesTable.COL_AMOUNT_CENTS);

        Cursor cursor = queryCategoryById(dbHelper.getReadableDatabase(), categoryId);

        cursor.moveToFirst();
        Category category = Category.fromCursor(cursor);

        ContentValues categoryValues = new ContentValues();
        if (entryDate.compareTo(category.date) < 0) {
            categoryValues.put(CategoriesTable.COL_FIRST_ENTRY_DATE, entryDate);
        }
        categoryValues.put(CategoriesTable.COL_ENTRY_FREQUENCY, category.frequency + 1);
        categoryValues.put(CategoriesTable.COL_TOTAL_AMOUNT, category.total + entryAmount);

        dbHelper.getWritableDatabase().update(
                CategoriesTable.TABLE_NAME,
                categoryValues,
                CategoriesTable._ID + " = ?",
                new String[]{categoryId});
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case ENTRY_WITH_ID:
                String entryId = uri.getLastPathSegment();
                return deleteEntry(entryId);

            default:
                throw new UnsupportedOperationException("Unknown or invalid Uri: " +
                        uri.toString());
        }
    }

    private int deleteEntry(String entryId) {
        updateCategoryOnDeleteEntry(entryId);
        int numDeleted = dbHelper.getWritableDatabase().delete(
                EntriesTable.TABLE_NAME,
                EntriesTable._ID + " = ?",
                new String[]{entryId});
        getContext().getContentResolver().notifyChange(EntriesTable.CONTENT_URI, null);
        return numDeleted;
    }

    private void updateCategoryOnDeleteEntry(String entryId) {
        Cursor cursor = queryEntryById(dbHelper.getReadableDatabase(), entryId);
        if (!cursor.moveToFirst()) {
            throw new IllegalStateException("Could not find an Entry that matches the id: " + entryId);
        }
        Entry entry = Entry.fromCursor(cursor);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * A class for very common queries made to the database
     */
    public static class DatabaseUtils {
        public static Cursor queryEntryById(SQLiteDatabase database, String entryId) {
            return database.query(
                    EntriesTable.TABLE_NAME,
                    Entry.projection,
                    EntriesTable._ID + " = ?",
                    new String[] {entryId},
                    null, null, null
            );
        }

        public static Cursor queryCategoryById(SQLiteDatabase database, String categoryId) {
            return database.query(
                    CategoriesTable.TABLE_NAME,
                    Category.projection,
                    CategoriesTable._ID + " = ?",
                    new String[] {categoryId},
                    null, null, null
            );
        }
    }
}
