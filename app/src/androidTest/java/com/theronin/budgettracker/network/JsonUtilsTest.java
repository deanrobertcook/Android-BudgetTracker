package com.theronin.budgettracker.network;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.gson.Gson;
import com.theronin.budgettracker.DatabaseDevUtils;
import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.data.BudgetDbHelper;
import com.theronin.budgettracker.model.Category;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class JsonUtilsTest {
    private static final String TAG = JsonUtilsTest.class.getName();
    private Context context;
    private BudgetDbHelper dbHelper;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        dbHelper = new BudgetDbHelper(context);

        BudgetDbHelper.dropTables(dbHelper.getWritableDatabase());
        BudgetDbHelper.createTables(dbHelper.getWritableDatabase());
    }

    @Test
    public void convertCategoriesToJson() {
        DatabaseDevUtils.fillDatabaseUsingContentProvider(
                context,
                DatabaseDevUtils.SOME_CATEGORIES,
                200, 1000);

        Cursor categoriesCursor = context.getContentResolver().query(
                CategoriesTable.CONTENT_URI,
                Category.projection,
                null, null, null);

        List<Category> categories = new ArrayList<>();
        while (categoriesCursor.moveToNext()) {
            categories.add(Category.fromCursor(categoriesCursor));
        }

        Gson gson = new Gson();
        String json = gson.toJson(categories);
        Log.d(TAG, json);
    }
}
