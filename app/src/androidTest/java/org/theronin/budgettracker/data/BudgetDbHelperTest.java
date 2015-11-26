package org.theronin.budgettracker.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BudgetDbHelperTest {

    private static final int SQLITE_MASTER_TABLE_NAME_COLUMN = 0;
    private static final int PRAGMA_TABLE_NAME_COLUMN = 1;

    private Context context = InstrumentationRegistry.getTargetContext();
    private SQLiteDatabase db;

    @Before
    public void clearDatabase() {
        context.deleteDatabase(BudgetDbHelper.DATABASE_NAME);
        db = new BudgetDbHelper(context).getWritableDatabase();
    }

    @Test
    public void testCreateDatabase() {
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(BudgetContract.EntriesTable.TABLE_NAME);
        tableNameHashSet.add(BudgetContract.CategoriesTable.TABLE_NAME);

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        cursor.moveToFirst();

        do {
            tableNameHashSet.remove(cursor.getString(SQLITE_MASTER_TABLE_NAME_COLUMN));
        } while (cursor.moveToNext());

        assertTrue("Some expected tables haven't been created", tableNameHashSet.isEmpty());
    }

    @Test
    public void testCategoriesTableColumns() {
        HashSet<String> categoriesColumnNames = new HashSet<>();
        categoriesColumnNames.add(BudgetContract.CategoriesTable.COL_CATEGORY_NAME);
        categoriesColumnNames.add(BudgetContract.CategoriesTable.COL_FIRST_ENTRY_DATE);

        Cursor cursor = db.rawQuery("PRAGMA table_info (" +
                BudgetContract.CategoriesTable.TABLE_NAME + ")", null);

        cursor.moveToFirst();

        do {
            categoriesColumnNames.remove(cursor.getString(PRAGMA_TABLE_NAME_COLUMN));
        } while (cursor.moveToNext());

        assertTrue("Some expected columns haven't been created", categoriesColumnNames.isEmpty());
    }

    @Test
    public void testEntriesTableColumns() {
        HashSet<String> entriesColumnNames = new HashSet<>();
        entriesColumnNames.add(BudgetContract.EntriesTable.COL_CATEGORY_ID);
        entriesColumnNames.add(BudgetContract.EntriesTable.COL_DATE_ENTERED);
        entriesColumnNames.add(BudgetContract.EntriesTable.COL_AMOUNT_CENTS);

        Cursor cursor = db.rawQuery("PRAGMA table_info (" +
                BudgetContract.EntriesTable.TABLE_NAME + ")", null);

        cursor.moveToFirst();

        do {
            entriesColumnNames.remove(cursor.getString(PRAGMA_TABLE_NAME_COLUMN));
        } while (cursor.moveToNext());

        assertTrue("Some expected columns haven't been created", entriesColumnNames.isEmpty());
    }

}