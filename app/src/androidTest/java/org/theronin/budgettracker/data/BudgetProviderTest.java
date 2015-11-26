package org.theronin.budgettracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.theronin.budgettracker.DatabaseDevUtils;
import org.theronin.budgettracker.DateDevUtils;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.theronin.budgettracker.DatabaseDevUtils.findCategoryId;
import static org.theronin.budgettracker.DatabaseDevUtils.findCategoryName;
import static org.theronin.budgettracker.DatabaseDevUtils.insertCategoryDirectlyToDatabase;
import static org.theronin.budgettracker.DatabaseDevUtils.insertEntryDirectlyToDatabase;
import static org.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import static org.theronin.budgettracker.utils.DateUtils.getStorageFormattedCurrentDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        DatabaseDevUtils.resetDatabase(dbHelper.getWritableDatabase());
    }

    @After
    public void cleanUp() {
        Log.d(TAG, "cleaning up");
        dbHelper.close();
        dbHelper = null;
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
        insertCategoryDirectlyToDatabase(dbHelper, expectedCategory);

        //query the content provider
        Uri categoryURI = CategoriesTable.CONTENT_URI.buildUpon().appendPath("1").build();

        Cursor result = context.getContentResolver().query(
                categoryURI,
                new String[]{
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_FIRST_ENTRY_DATE},
                null, null, null);
        result.moveToFirst();

        assertEquals("Unexpected category name", expectedCategory.name, result.getString(0));
        assertEquals("Unexpected category date", getStorageFormattedCurrentDate(), result
                .getString(1));
    }

    @Test
    public void queryAllCategories() {
        Log.d(TAG, "queryAllCategories");

        //Enter a few categories directly into the database. Store them as a set to check off
        //later
        ArrayList<Category> categories = new ArrayList<>();
        categories.add(new Category("cashews", getStorageFormattedCurrentDate()));
        categories.add(new Category("bananas", getStorageFormattedCurrentDate()));
        categories.add(new Category("apples", getStorageFormattedCurrentDate()));
        categories.add(new Category("coffee", getStorageFormattedCurrentDate()));
        categories.add(new Category("tea", getStorageFormattedCurrentDate()));

        for (Category category : categories) {
            insertCategoryDirectlyToDatabase(dbHelper, category);
        }

        //Query all current categories and check them off of the hashset using the contentProvider
        Uri categoriesUri = CategoriesTable.CONTENT_URI;
        Cursor result = context.getContentResolver().query(
                categoriesUri,
                new String[]{
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_FIRST_ENTRY_DATE},
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
        String categoryName = "cashews";
        insertCategoryDirectlyToDatabase(dbHelper, new Category(categoryName, null));

        //insert some expectedCategory directly into the database
        Entry expectedEntry = new Entry(categoryName, getStorageFormattedCurrentDate(), 250);
        insertEntryDirectlyToDatabase(dbHelper, expectedEntry);

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

        assertEquals("Unexpected Entry categoryId", expectedEntry.categoryName,
                findCategoryName(dbHelper, result.getLong(0)));
        assertEquals("Unexpected Entry dateEntered", expectedEntry.dateEntered, result.
                getString(1));
        assertEquals("Unexpected Entry amount", expectedEntry.amount, result.getLong(2));
    }

    @Test
    public void queryAllEntries() {
        Log.d(TAG, "queryAllEntries");

        //insert a few categories to satisfy constraints
        String[] categoryNames = {"cashews", "apples", "bananas"};
        insertCategoryDirectlyToDatabase(dbHelper, new Category(categoryNames[0], null));
        insertCategoryDirectlyToDatabase(dbHelper, new Category(categoryNames[1], null));
        insertCategoryDirectlyToDatabase(dbHelper, new Category(categoryNames[2], null));
        //Enter a few categories directly into the database. Store them as a set to check off
        //later
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(categoryNames[0], getStorageFormattedCurrentDate(), 200));
        entries.add(new Entry(categoryNames[0], getStorageFormattedCurrentDate(), 100));
        entries.add(new Entry(categoryNames[1], getStorageFormattedCurrentDate(), 2000));
        entries.add(new Entry(categoryNames[1], getStorageFormattedCurrentDate(), 2240));
        entries.add(new Entry(categoryNames[2], getStorageFormattedCurrentDate(), 230));
        entries.add(new Entry(categoryNames[2], getStorageFormattedCurrentDate(), 420));

        for (Entry entry : entries) {
            insertEntryDirectlyToDatabase(dbHelper, entry);
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
                if (tempEntry.categoryName.equals(findCategoryName(dbHelper, result.getLong(0)))
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
                        CategoriesTable.COL_FIRST_ENTRY_DATE},
                CategoriesTable.COL_CATEGORY_NAME + " = ?",
                new String[]{category.name},
                null, null, null);

        //There should only be one result for a given name
        assertEquals("Expected only one result for a given category name", 1, cursor.getCount());

        cursor.moveToFirst();
        //Check the name matches and that the date of insertion is saved in the db
        assertEquals(category.name, cursor.getString(0));
        assertEquals(getStorageFormattedCurrentDate(), cursor.getString(1));

        //Check the ID is returned in the URI from the insert
        int categoryId = Integer.parseInt(newCategoryItemUri.getLastPathSegment());
        assertEquals("The ID of the inserted category is incorrect",
                EMPTY_DATABASE_FIRST_AUTOINCREMENT_INDEX, categoryId);
    }

    @Test
    public void addEntry() {
        Log.d(TAG, "addEntry");
        //Create a category to satisfy constraint
        String categoryName = "cashews";
        Category category = new Category(categoryName, null);
        insertCategoryDirectlyToDatabase(dbHelper, category);

        //Create a new entry to match the category just added
        Entry entry = new Entry(categoryName, getStorageFormattedCurrentDate(), 100);
        //re-use values object
        ContentValues values = new ContentValues();
        values.put(EntriesTable.COL_CATEGORY_ID, findCategoryId(dbHelper, entry.categoryName));
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
        assertEquals(entry.categoryName, findCategoryName(dbHelper, cursor.getLong(0)));
        assertEquals(entry.dateEntered, cursor.getString(1));
        assertEquals(entry.amount, cursor.getLong(2));
    }

    @Test
    public void deleteEntry() {
        //set up an entry with a category
        String categoryName = "cashews";
        insertCategoryDirectlyToDatabase(dbHelper, new Category(categoryName));
        long entryId = insertEntryDirectlyToDatabase(dbHelper,
                new Entry(categoryName, getStorageFormattedCurrentDate(), 100));

        //do the deletion
        int numDeleted = context.getContentResolver().delete(
                EntriesTable.CONTENT_URI.buildUpon().appendPath(Long.toString(entryId)).build(),
                null, null
        );

        //query everything from the entries table
        Cursor cursor = dbHelper.getReadableDatabase().query(
                EntriesTable.TABLE_NAME,
                null, null, null, null, null, null
        );

        //assert the cursor contains nothing
        assertEquals("The number of deleted items is incorrect", 1, numDeleted);
        assertEquals("The entry was not deleted properly", 0, cursor.getCount());
    }

    @Test
    public void categoryShouldUpdateOnInsertEntry() {
        String categoryName = "cashews";
        String date = DateDevUtils.getRandomDate();
        long amount = 100;
        insertCategoryDirectlyToDatabase(dbHelper.getWritableDatabase(), new Category(categoryName));

        ContentValues values = new ContentValues();
        values.put(EntriesTable.COL_DATE_ENTERED, date);
        values.put(EntriesTable.COL_CATEGORY_ID, findCategoryId(dbHelper.getWritableDatabase(),
                categoryName));
        values.put(EntriesTable.COL_AMOUNT_CENTS, amount);

        context.getContentResolver().insert(
                EntriesTable.CONTENT_URI,
                values
        );

        Cursor cursor = context.getContentResolver().query(
                CategoriesTable.CONTENT_URI,
                Category.projection,
                CategoriesTable.COL_CATEGORY_NAME + " = ?",
                new String[]{categoryName}, null
        );

        cursor.moveToFirst();
        Category retrievedCategory = Category.fromCursor(cursor);
        cursor.close();
        assertEquals(date, retrievedCategory.date);
        assertEquals(1, retrievedCategory.frequency);
        assertEquals(100, retrievedCategory.total);

        String earlierDate = DateDevUtils.getDaysBefore(date, 5);

        values.put(EntriesTable.COL_DATE_ENTERED, earlierDate);
        context.getContentResolver().insert(
                EntriesTable.CONTENT_URI,
                values
        );

        //The cursor should be notified automatically on dataset change (wooh ContentProviders).
        cursor = context.getContentResolver().query(
                CategoriesTable.CONTENT_URI,
                Category.projection,
                CategoriesTable.COL_CATEGORY_NAME + " = ?",
                new String[]{categoryName}, null
        );
        cursor.moveToFirst();
        retrievedCategory = Category.fromCursor(cursor);
        assertEquals(earlierDate, retrievedCategory.date);
        assertEquals(2, retrievedCategory.frequency);
        assertEquals(200, retrievedCategory.total);
    }

    @Test
    public void categoryShouldBeUpdatedOnDeletedEntry() {
        //set up the category
        String categoryName = "cashews";
        insertCategoryDirectlyToDatabase(dbHelper.getWritableDatabase(), new Category(categoryName));

        //Create two entries, one earlier than the other
        String[] dates = {DateDevUtils.getDaysAgo(10), DateDevUtils.getDaysAgo(5)};
        long amount = 100;

        //insert the entries
        for (int i = 0; i < dates.length; i++) {
            ContentValues values = new ContentValues();
            values.put(EntriesTable.COL_DATE_ENTERED, dates[i]);
            values.put(EntriesTable.COL_CATEGORY_ID, findCategoryId(dbHelper.getWritableDatabase(),
                    categoryName));
            values.put(EntriesTable.COL_AMOUNT_CENTS, amount);

            context.getContentResolver().insert(
                    EntriesTable.CONTENT_URI,
                    values
            );
        }

        //Delete the earlier entry
        context.getContentResolver().delete(
                EntriesTable.CONTENT_URI.buildUpon().appendPath("1").build(),
                null, null
        );

        //Query the categories table
        Cursor cursor = context.getContentResolver().query(
                CategoriesTable.CONTENT_URI,
                Category.projection,
                CategoriesTable.COL_CATEGORY_NAME + " = ?",
                new String[]{categoryName}, null
        );
        cursor.moveToFirst();
        Category category = Category.fromCursor(cursor);

        //Check that the date matches the later entry, and that the frequency/totals are correct
        assertEquals(dates[1], category.date);
        assertEquals(1, category.frequency);
        assertEquals(100, category.total);
    }

}