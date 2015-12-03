package org.theronin.budgettracker.task;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.theronin.budgettracker.DatabaseDevUtils;
import org.theronin.budgettracker.data.BudgetContract.CategoriesView;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import com.theronin.budgettracker.data.BudgetDbHelper;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
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

        DatabaseDevUtils.resetDatabase(dbHelper.getWritableDatabase());
    }

    @Test
    public void convertCategoriesToJson() {
        DatabaseDevUtils.fillDatabaseUsingContentProvider(
                context,
                DatabaseDevUtils.SOME_CATEGORIES,
                200, 1000);

        Cursor categoriesCursor = context.getContentResolver().query(
                CategoriesView.CONTENT_URI,
                Category.projection,
                null, null, null);

        List<Category> categories = new ArrayList<>();
        while (categoriesCursor.moveToNext()) {
            categories.add(Category.fromCursor(categoriesCursor));
        }

        Gson gson = new Gson();
        String json = gson.toJson(categories);
        Log.d(TAG, json);

        Cursor entriesCursor = context.getContentResolver().query(
                EntriesTable.CONTENT_URI,
                Entry.projection,
                null, null, null
        );

        List<Entry> entries = new ArrayList<>();
        while (entriesCursor.moveToNext()) {
            entries.add(Entry.fromCursor(entriesCursor));
        }

        json = gson.toJson(entries);
        Log.d(TAG, json);

        Type entryArrayType = new TypeToken<ArrayList<Entry>>(){}.getType();
        ArrayList<Entry> entriesParsed = gson.fromJson(json, entryArrayType);
        for (Entry entry : entriesParsed) {
            Log.d(TAG, entry.id + ", " + entry.category + ", " + entry.utcDateEntered + ", " + entry.amount);
        }
    }
}
