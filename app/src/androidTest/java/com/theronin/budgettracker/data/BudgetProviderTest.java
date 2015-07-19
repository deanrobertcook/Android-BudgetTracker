package com.theronin.budgettracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.theronin.budgettracker.data.BudgetContract.EntriesTable;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.Entry;
import com.theronin.budgettracker.utils.DateUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BudgetProviderTest {

    private static final String TAG = BudgetProviderTest.class.getName();
    private static final String TEST_ITEM_ID = "1";

    private Context context = InstrumentationRegistry.getTargetContext();
    private SQLiteDatabase db;

    @Before
    public void clearDatabase() {
        assertTrue(context.deleteDatabase(BudgetDbHelper.DATABASE_NAME));
        db = new BudgetDbHelper(context).getWritableDatabase();
    }

    @Test
    public void getCategoryDirectoryType() {
        String expectedType = CategoriesTable.CONTENT_TYPE;
        Uri categoryDirUri = CategoriesTable.CONTENT_URI;
        String matchedType = context.getContentResolver().getType(categoryDirUri);
        assertEquals("Categories dir type is incorrect", expectedType, matchedType);
    }

    @Test
    public void getCategoryItemType() {
        String expectedType = CategoriesTable.CONTENT_ITEM_TYPE;
        Uri categoryItemUri = CategoriesTable.CONTENT_URI.buildUpon().appendPath(TEST_ITEM_ID).build();
        String matchedType = context.getContentResolver().getType(categoryItemUri);
        assertEquals("Category item type is incorrect", expectedType, matchedType);
    }

    @Test
    public void getEntriesDirectoryType() {
        String expectedType = EntriesTable.CONTENT_TYPE;
        Uri entriesDirUri = EntriesTable.CONTENT_URI;
        String matchedType = context.getContentResolver().getType(entriesDirUri);
        assertEquals("Entries dir type is incorrect", expectedType, matchedType);
    }

    @Test
    public void getEntryItemType() {
        String expectedType = EntriesTable.CONTENT_ITEM_TYPE;
        Uri entryItemUri = EntriesTable.CONTENT_URI.buildUpon().appendPath(TEST_ITEM_ID).build();
        String matchedType = context.getContentResolver().getType(entryItemUri);
        assertEquals("Entry item type is incorrect", expectedType, matchedType);
    }

    @Test
    public void addCategory() {
        //Create a new category
        Category category = new Category("cashews", null);
        //And a ContentValues with the name data (date should be done automatically)
        ContentValues values = new ContentValues();
        values.put(CategoriesTable.COL_CATEGORY_NAME, category.name);


        //Add to the content provider
        context.getContentResolver().insert(
                CategoriesTable.CONTENT_URI,
                values);

        //Query the database directly to check
        Cursor cursor = db.query(
                CategoriesTable.TABLE_NAME,
                new String[]{
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_DATE_CREATED},
                CategoriesTable.COL_CATEGORY_NAME + " = ?",
                new String[]{category.name},
                null, null, null);

        //There should only be one result for a given name
        assertTrue(cursor.getCount() == 1);

        cursor.moveToFirst();
        //Check the name matches and that the date of insertion is saved in the db
        assertEquals(category.name, cursor.getString(0));
        assertEquals(DateUtils.getCurrentDateString(), cursor.getString(1));
    }

    @Test
    public void addEntry() {
        //Create a new category
        Entry entry = new Entry();
        //And a ContentValues with the name data (date should be done automatically)
        ContentValues values = new ContentValues();
        values.put(CategoriesTable.COL_CATEGORY_NAME, category.name);


        //Add to the content provider
        context.getContentResolver().insert(
                CategoriesTable.CONTENT_URI,
                values);

        //Query the database directly to check
        Cursor cursor = db.query(
                CategoriesTable.TABLE_NAME,
                new String[]{
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_DATE_CREATED},
                CategoriesTable.COL_CATEGORY_NAME + " = ?",
                new String[]{category.name},
                null, null, null);

        //There should only be one result for a given name
        assertTrue(cursor.getCount() == 1);

        cursor.moveToFirst();
        //Check the name matches and that the date of insertion is saved in the db
        assertEquals(category.name, cursor.getString(0));
        assertEquals(DateUtils.getCurrentDateString(), cursor.getString(1));
    }

}