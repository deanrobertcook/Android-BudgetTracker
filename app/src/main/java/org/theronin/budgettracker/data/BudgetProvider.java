package org.theronin.budgettracker.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import org.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import org.theronin.budgettracker.data.BudgetContract.CategoriesView;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import org.theronin.budgettracker.model.Entry;

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

        matcher.addURI(authority, CategoriesView.PROVIDER_PATH, CATEGORIES);
        matcher.addURI(authority, CategoriesView.PROVIDER_PATH + "/#", CATEGORY_WITH_ID);
        matcher.addURI(authority, EntriesTable.PROVIDER_PATH, ENTRIES);
        matcher.addURI(authority, EntriesTable.PROVIDER_PATH + "/#", ENTRY_WITH_ID);

        return matcher;
    }

    private static final SQLiteQueryBuilder entryJoinedOnCategoryQueryBuilder;

    static {
        entryJoinedOnCategoryQueryBuilder = new SQLiteQueryBuilder();

        entryJoinedOnCategoryQueryBuilder.setTables(
                EntriesTable.TABLE_NAME + " LEFT JOIN " + CategoriesView.VIEW_NAME +
                        " ON " +
                        EntriesTable.TABLE_NAME + "." + EntriesTable.COL_CATEGORY_ID + " = " +
                        CategoriesView.VIEW_NAME + "." + CategoriesView._ID
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
                CategoriesView.VIEW_NAME,
                projection,
                CategoriesView._ID + " = ?",
                new String[]{id},
                null, null, null
        );
        cursor.setNotificationUri(getContext().getContentResolver(), CategoriesView.CONTENT_URI);
        return cursor;
    }

    private Cursor queryCategories(String[] projection, String selection, String[] selectionArgs) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                CategoriesView.VIEW_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
        cursor.setNotificationUri(getContext().getContentResolver(), CategoriesView.CONTENT_URI);
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
                return CategoriesView.CONTENT_TYPE;
            case CATEGORY_WITH_ID:
                return CategoriesView.CONTENT_ITEM_TYPE;
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
                return CategoriesView.CONTENT_URI.buildUpon()
                        .appendPath(Long.toString(insertCategory(values))).build();
            case ENTRIES:
                return insertEntry(values);
            default:
                throw new UnsupportedOperationException("Unknown or invalid Uri: " + uri.toString
                        ());
        }
    }

    private long insertCategory(ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long categoryId = db.insert(CategoriesTable.TABLE_NAME, null, values);
        notifyChanges();
        return categoryId;
    }

    private void notifyChanges() {
        getContext().getContentResolver().notifyChange(EntriesTable.CONTENT_URI, null);
        getContext().getContentResolver().notifyChange(CategoriesView.CONTENT_URI, null);
    }

    private Uri insertEntry(ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long entryId = db.insert(EntriesTable.TABLE_NAME, null, values);
        notifyChanges();

        return EntriesTable.CONTENT_URI.buildUpon().appendPath(Long.toString(entryId))
                .build();
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
        int numDeleted = dbHelper.getWritableDatabase().delete(
                EntriesTable.TABLE_NAME,
                EntriesTable._ID + " = ?",
                new String[]{entryId});
        notifyChanges();
        return numDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        switch (uriMatcher.match(uri)) {
            case ENTRIES:
                return bulkInsertEntries(values);
            default:
                throw new UnsupportedOperationException("Unknown or invalid Uri: " +
                        uri.toString());
        }
    }

    private int bulkInsertEntries(ContentValues[] valuesArray) {
        switchCategoryNamesToIds(valuesArray);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues values : valuesArray) {
                db.insert(
                        EntriesTable.TABLE_NAME,
                        null,
                        values
                );
            }

            db.setTransactionSuccessful();
            notifyChanges();
        } finally {
            db.endTransaction();
        }

        return valuesArray.length;
    }

    private void switchCategoryNamesToIds(ContentValues[] valuesArray) {
        for (ContentValues values : valuesArray) {
            String categoryName = values.getAsString(Entry.projection[Entry.INDEX_CATEGORY_NAME]);
            long categoryId = getCategoryId(categoryName);
            values.remove(Entry.projection[Entry.INDEX_CATEGORY_NAME]);
            values.put(EntriesTable.COL_CATEGORY_ID, categoryId);
        }
    }

    private long getCategoryId(String categoryName) {
        Cursor cursor = dbHelper.getWritableDatabase().query(
                CategoriesView.VIEW_NAME,
                CategoriesView.RAW_PROJECTION,
                CategoriesView.COL_CATEGORY_NAME + " = ?",
                new String[]{categoryName},
                null, null, null
        );

        if (!cursor.moveToFirst()) {
            ContentValues newCategoryValues = new ContentValues();
            newCategoryValues.put(CategoriesView.COL_CATEGORY_NAME, categoryName);
            return insertCategory(newCategoryValues);
        }

        long id = cursor.getLong(CategoriesView.INDEX_ID);
        cursor.close();
        return id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
