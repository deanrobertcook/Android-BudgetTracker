package com.theronin.budgettracker.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.theronin.budgettracker.DatabaseDevUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DatabaseUpgradeTest {

    @Test
    public void version1To2() {
        //Only the Categories Table has changed

        Context context = InstrumentationRegistry.getContext();

        SQLiteDatabase database = SQLiteDatabase.create(null);
        database.execSQL(BudgetContractV1.CategoriesTable.SQL_CREATE_CATEGORIES_TABLE);

        DatabaseDevUtils.fillDatabaseWithDummyData(
                database, new String[]{"cat1", "cat2"}, 10, 1000);

        Cursor oldData = database.query(
                BudgetContractV1.CategoriesTable.TABLE_NAME,
                new String[]{
                        BudgetContractV1.CategoriesTable.COL_CATEGORY_NAME,
                        BudgetContractV1.CategoriesTable.COL_DATE_CREATED
                }, null, null, null, null, null
        );

        int count = oldData.getCount();
        oldData.close();


        BudgetDbHelper dbHelper = new BudgetDbHelper(context);
        dbHelper.onUpgrade(database, 1, 2);

        Cursor newData = database.query(
                BudgetContractV2.CategoriesTable.TABLE_NAME,
                new String[]{
                        BudgetContractV2.CategoriesTable.COL_CATEGORY_NAME,
                        BudgetContractV2.CategoriesTable.COL_FIRST_ENTRY_DATE,
                        BudgetContractV2.CategoriesTable.COL_ENTRY_FREQUENCY,
                        BudgetContractV2.CategoriesTable.COL_TOTAL_AMOUNT
                }, null, null, null, null, null
        );

        int newCount = newData.getCount();
        newData.close();

        assertEquals(count, newCount);


    }


}
