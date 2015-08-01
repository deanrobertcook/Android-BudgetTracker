package com.theronin.budgettracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.theronin.budgettracker.data.BudgetContract;
import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.data.BudgetDbHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CategoryStoreTest {
    public static final String TAG = CategoryStoreTest.class.getName();
    private Context context = InstrumentationRegistry.getTargetContext();

    BudgetDbHelper dbHelper;

    @Before
    public void clearDatabase() {
        Log.d(TAG, "Clearing database");
        dbHelper = new BudgetDbHelper(context);

        //For some reason, using context.deleteDatabase() spoils the database for subsequent tests
        //instead, it's better to just drop the tables and recreate everything
        dbHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + BudgetContract
                .EntriesTable.TABLE_NAME);
        dbHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + CategoriesTable
                .TABLE_NAME);
        dbHelper.onCreate(dbHelper.getWritableDatabase());

    }

    @After
    public void cleanUp() {
        Log.d(TAG, "cleaning up");
        dbHelper.close();
        dbHelper = null;
    }

    @Test
    public void testFetchCategories() {
        //create some categories
        final ArrayList<Category> expectedCategories = new ArrayList<>();
        expectedCategories.add(new Category("cashews", null));
        expectedCategories.add(new Category("bananas", null));
        expectedCategories.add(new Category("apples", null));
        expectedCategories.add(new Category("coffee", null));

        //insert them into the database
        for (Category category : expectedCategories) {
            ContentValues values = new ContentValues();
            values.put(CategoriesTable.COL_CATEGORY_NAME, category.name);

            context.getContentResolver().insert(
                    CategoriesTable.CONTENT_URI, values);
        }


        //make an observer which tests the list
        CategoryStore.Observer testObserver = new CategoryStore.Observer() {
            @Override
            public void onCategoriesLoaded(List<Category> resultCategories) {

                assertEquals("The number of fetched categories is wrong",
                        expectedCategories.size(), resultCategories.size());

                ArrayList<String> expectedNames = new ArrayList<>();
                ArrayList<String> resultNames = new ArrayList<>();
                for (int i = 0; i < resultCategories.size(); i++) {
                    expectedNames.add(expectedCategories.get(i).name);
                    resultNames.add(resultCategories.get(i).name);
                }

                assertEquals("The names of the resulting categories don't match", expectedNames, resultNames);

            }
        };
        //TODO fix this test

//        //register the observer to a new store
//        CategoryStore testStore = new CategoryStore((BudgetTrackerApplication) context);
//        testStore.addObserver(testObserver);
//
//        //trigger the fetch
//        testStore.fetchCategories();
    }

}
