package com.theronin.budgettracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.theronin.budgettracker.model.Category;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BudgetProviderTest {

    private Context context = InstrumentationRegistry.getTargetContext();
    private SQLiteDatabase db;

    @Before
    public void clearDatabase() {
        context.deleteDatabase(BudgetDbHelper.DATABASE_NAME);
        db = new BudgetDbHelper(context).getWritableDatabase();
    }

    @Test
    public void addCategory() {
        Category category = new Category("cashews");

        ContentValues values = new ContentValues();
        values.put(CategoriesTable.COL_CATEGORY_NAME, category.name);

        context.getContentResolver().insert(
                CategoriesTable.CONTENT_URI,
                values);

        Cursor cursor = db.query(CategoriesTable.TABLE_NAME,
                new String[] {CategoriesTable.COL_CATEGORY_NAME},
                CategoriesTable.COL_CATEGORY_NAME + " = ?",
                new String[] {category.name},
                null, null, null);

        assertTrue(cursor.getCount() == 1);

        cursor.moveToFirst();
        assertEquals(category.name, cursor.getString(0));
    }

}