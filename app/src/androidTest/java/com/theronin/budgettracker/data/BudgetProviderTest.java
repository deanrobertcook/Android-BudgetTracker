package com.theronin.budgettracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.theronin.budgettracker.data.BudgetContract.EntriesTable;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.Entry;
import com.theronin.budgettracker.utils.DateUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class BudgetProviderTest {

    private static final String TAG = BudgetProviderTest.class.getName();
    private static final String TEST_ITEM_ID = "1";
    private static final int EMPTY_DATABASE_FIRST_AUTOINCREMENT_INDEX = 1;

    private Context context = InstrumentationRegistry.getTargetContext();
    private BudgetDbHelper dbHelper;

    @BeforeClass
    public static void startTests() {
        Log.d(TAG, "***Starting Tests***");
    }

    @Before
    public void clearDatabase() {
        Log.d(TAG, "Clearing database");
        dbHelper = new BudgetDbHelper(context);

        //For some reason, using context.deleteDatabase() spoils the database for subsequent tests
        //instead, it's better to just drop the tables and recreate everything
        dbHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + EntriesTable.TABLE_NAME);
        dbHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
        dbHelper.onCreate(dbHelper.getWritableDatabase());

    }

    @After
    public void cleanUp() {
        Log.d(TAG, "cleaning up");
        dbHelper.close();
        dbHelper = null;
    }

    private long insertCategoryDirectlyToDatabase(Category category) {
        ContentValues values = new ContentValues();
        values.put(CategoriesTable.COL_CATEGORY_NAME, category.name);
        if (category.date != null) {
            values.put(CategoriesTable.COL_DATE_CREATED, category.date);
        }

        long categoryId = dbHelper.getWritableDatabase().insert(CategoriesTable.TABLE_NAME, null,
                values);
        if (categoryId == -1) {
            fail("An error occurred inserting the Category into the DB");
        }
        return categoryId;
    }

    private long insertEntryDirectlyToDatabase(Entry entry) {
        ContentValues values = new ContentValues();
        values.put(EntriesTable.COL_CATEGORY_ID, entry.categoryId);
        values.put(EntriesTable.COL_DATE_ENTERED, entry.dateEntered);
        values.put(EntriesTable.COL_AMOUNT_CENTS, entry.amount);

        long entryId = dbHelper.getWritableDatabase().insert(EntriesTable.TABLE_NAME, null, values);
        if (entryId == -1) {
            fail("An error occurred inserting the Entry into the DB");
        }
        return entryId;
    }

    @Test
    public void getCategoryDirectoryType() {
        Log.d(TAG, "getCategoryDirectoryType");
        String expectedType = CategoriesTable.CONTENT_TYPE;
        Uri categoryDirUri = CategoriesTable.CONTENT_URI;
        String matchedType = context.getContentResolver().getType(categoryDirUri);
        assertEquals("Categories dir type is incorrect", expectedType, matchedType);
    }

    @Test
    public void getCategoryItemType() {
        Log.d(TAG, "getCategoryItemType");
        String expectedType = CategoriesTable.CONTENT_ITEM_TYPE;
        Uri categoryItemUri = CategoriesTable.CONTENT_URI.buildUpon().appendPath(TEST_ITEM_ID)
                .build();
        String matchedType = context.getContentResolver().getType(categoryItemUri);
        assertEquals("Category item type is incorrect", expectedType, matchedType);
    }

    @Test
    public void getEntriesDirectoryType() {
        Log.d(TAG, "getEntriesDirectoryType");
        String expectedType = EntriesTable.CONTENT_TYPE;
        Uri entriesDirUri = EntriesTable.CONTENT_URI;
        String matchedType = context.getContentResolver().getType(entriesDirUri);
        assertEquals("Entries dir type is incorrect", expectedType, matchedType);
    }

    @Test
    public void getEntryItemType() {
        Log.d(TAG, "getEntryItemType");
        String expectedType = EntriesTable.CONTENT_ITEM_TYPE;
        Uri entryItemUri = EntriesTable.CONTENT_URI.buildUpon().appendPath(TEST_ITEM_ID).build();
        String matchedType = context.getContentResolver().getType(entryItemUri);
        assertEquals("Entry item type is incorrect", expectedType, matchedType);
    }

    @Test
    public void querySingleCategory() {
        Log.d(TAG, "querySingleCategory");

        //insert some expectedCategory directly into the database
        Category expectedCategory = new Category("cashews", null);
        insertCategoryDirectlyToDatabase(expectedCategory);

        //query the content provider
        Uri categoryURI = CategoriesTable.CONTENT_URI.buildUpon().appendPath("1").build();

        Cursor result = context.getContentResolver().query(
                categoryURI,
                new String[]{
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_DATE_CREATED},
                null, null, null);
        result.moveToFirst();

        assertEquals("Unexpected category name", expectedCategory.name, result.getString(0));
        assertEquals("Unexpected category date", DateUtils.getCurrentDateString(), result
                .getString(1));
    }

    @Test
    public void queryAllCategories() {
        Log.d(TAG, "queryAllCategories");

        //Enter a few categories directly into the database. Store them as a set to check off
        //later
        ArrayList<Category> categories = new ArrayList<>();
        categories.add(new Category("cashews", DateUtils.getCurrentDateString()));
        categories.add(new Category("bananas", DateUtils.getCurrentDateString()));
        categories.add(new Category("apples", DateUtils.getCurrentDateString()));
        categories.add(new Category("coffee", DateUtils.getCurrentDateString()));
        categories.add(new Category("tea", DateUtils.getCurrentDateString()));

        for (Category category : categories) {
            insertCategoryDirectlyToDatabase(category);
        }

        //Query all current categories and check them off of the hashset using the contentProvider
        Uri categoriesUri = CategoriesTable.CONTENT_URI;
        Cursor result = context.getContentResolver().query(
                categoriesUri,
                new String[]{
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_DATE_CREATED},
                null, null, null);

        while (result.moveToNext()) {
            for (int i = 0; i < categories.size(); i++) {
                Category tempCategory = categories.get(i);
                if (tempCategory.name.equals(result.getString(0))
                        && tempCategory.date.equals(result.getString(1))) {
                    categories.remove(tempCategory);
                }
            }
        }
        assertTrue(categories.size() + " categories remained in the set", categories.isEmpty());
    }

    @Test
    public void querySingleEntry() {
        Log.d(TAG, "querySingleEntry");

        //insert a category to satisfy constraints
        long categoryId = insertCategoryDirectlyToDatabase(new Category("cashews", null));

        //insert some expectedCategory directly into the database
        Entry expectedEntry = new Entry(categoryId, DateUtils.getCurrentDateString(), 250);
        insertEntryDirectlyToDatabase(expectedEntry);

        //query the content provider
        Uri entryUri = EntriesTable.CONTENT_URI.buildUpon().appendPath("1").build();

        Cursor result = context.getContentResolver().query(
                entryUri,
                new String[]{
                        EntriesTable.COL_CATEGORY_ID,
                        EntriesTable.COL_DATE_ENTERED,
                        EntriesTable.COL_AMOUNT_CENTS},
                null, null, null);
        result.moveToFirst();

        assertEquals("Unexpected Entry categoryId", expectedEntry.categoryId, result.getLong(0));
        assertEquals("Unexpected Entry dateEntered", expectedEntry.dateEntered, result.
                getString(1));
        assertEquals("Unexpected Entry amount", expectedEntry.amount, result.getLong(2));
    }

    @Test
    public void queryAllEntries() {
        Log.d(TAG, "queryAllEntries");

        //insert a few categories to satisfy constraints
        long categoryId1 = insertCategoryDirectlyToDatabase(new Category("cashews", null));
        long categoryId2 = insertCategoryDirectlyToDatabase(new Category("apples", null));
        long categoryId3 = insertCategoryDirectlyToDatabase(new Category("bananas", null));
        //Enter a few categories directly into the database. Store them as a set to check off
        //later
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(categoryId1, DateUtils.getCurrentDateString(), 200));
        entries.add(new Entry(categoryId1, DateUtils.getCurrentDateString(), 100));
        entries.add(new Entry(categoryId2, DateUtils.getCurrentDateString(), 2000));
        entries.add(new Entry(categoryId2, DateUtils.getCurrentDateString(), 2240));
        entries.add(new Entry(categoryId3, DateUtils.getCurrentDateString(), 230));
        entries.add(new Entry(categoryId3, DateUtils.getCurrentDateString(), 420));

        for (Entry entry : entries) {
            insertEntryDirectlyToDatabase(entry);
        }

        //Query all current categories and check them off of the hashset using the contentProvider
        Uri entriesUri = EntriesTable.CONTENT_URI;
        Cursor result = context.getContentResolver().query(
                entriesUri,
                new String[]{
                        EntriesTable.COL_CATEGORY_ID,
                        EntriesTable.COL_DATE_ENTERED,
                        EntriesTable.COL_AMOUNT_CENTS},
                null, null, null);

        while (result.moveToNext()) {
            for (int i = 0; i < entries.size(); i++) {
                Entry tempEntry = entries.get(i);
                if (tempEntry.categoryId == result.getLong(0)
                        && tempEntry.dateEntered.equals(result.getString(1))
                        && tempEntry.amount == result.getLong(2)) {
                    entries.remove(tempEntry);
                }
            }
        }
        assertTrue(entries.size() + " entries remained in the set", entries.isEmpty());
    }

    @Test
    public void addCategory() {
        Log.d(TAG, "addCategory");
        //Create a new category
        Category category = new Category("cashews", null);
        //And a ContentValues with the name data (date should be done automatically)
        ContentValues values = new ContentValues();
        values.put(CategoriesTable.COL_CATEGORY_NAME, category.name);


        //Add to the content provider
        Uri newCategoryItemUri = context.getContentResolver().insert(
                CategoriesTable.CONTENT_URI,
                values);

        //Query the database directly to check
        Cursor cursor = dbHelper.getReadableDatabase().query(
                CategoriesTable.TABLE_NAME,
                new String[]{
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_DATE_CREATED},
                CategoriesTable.COL_CATEGORY_NAME + " = ?",
                new String[]{category.name},
                null, null, null);

        //There should only be one result for a given name
        assertEquals("Expected only one result for a given category name", 1, cursor.getCount());

        cursor.moveToFirst();
        //Check the name matches and that the date of insertion is saved in the db
        assertEquals(category.name, cursor.getString(0));
        assertEquals(DateUtils.getCurrentDateString(), cursor.getString(1));

        //Check the ID is returned in the URI from the insert
        int categoryId = Integer.parseInt(newCategoryItemUri.getLastPathSegment());
        assertEquals("The ID of the inserted category is incorrect",
                EMPTY_DATABASE_FIRST_AUTOINCREMENT_INDEX, categoryId);
    }

    @Test
    public void addEntry() {
        Log.d(TAG, "addEntry");
        //Create a category to satisfy constraint
        Category category = new Category("cashews", null);
        long categoryId = insertCategoryDirectlyToDatabase(category);

        //Create a new entry to match the category just added
        Entry entry = new Entry(categoryId, DateUtils.getCurrentDateString(), 100);
        //re-use values object
        ContentValues values = new ContentValues();
        values.put(EntriesTable.COL_CATEGORY_ID, entry.categoryId);
        values.put(EntriesTable.COL_DATE_ENTERED, entry.dateEntered);
        values.put(EntriesTable.COL_AMOUNT_CENTS, entry.amount);


        //Add to the content provider
        Uri newEntryItemUri = context.getContentResolver().insert(
                EntriesTable.CONTENT_URI,
                values);
        String entryId = newEntryItemUri.getLastPathSegment();


        //Query the database directly to check
        Cursor cursor = dbHelper.getReadableDatabase().query(
                EntriesTable.TABLE_NAME,
                new String[]{
                        EntriesTable.COL_CATEGORY_ID,
                        EntriesTable.COL_DATE_ENTERED,
                        EntriesTable.COL_AMOUNT_CENTS,},
                EntriesTable._ID + " = ?",
                new String[]{entryId},
                null, null, null);

        //There should only be one result for a entry Id
        assertEquals("Expected only one result for an entry ID", 1, cursor.getCount());

        cursor.moveToFirst();
        //Check the name matches and that the date of insertion is saved in the db
        assertEquals(entry.categoryId, cursor.getLong(0));
        assertEquals(entry.dateEntered, cursor.getString(1));
        assertEquals(entry.amount, cursor.getLong(2));
    }

}