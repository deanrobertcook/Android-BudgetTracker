package com.theronin.budgettracker;

import android.app.Application;

import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.data.BudgetContract.EntriesTable;
import com.theronin.budgettracker.data.BudgetDbHelper;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.CategoryStore;
import com.theronin.budgettracker.model.EntryStore;

import java.util.ArrayList;

import timber.log.Timber;

public class BudgetTrackerApplication extends Application {

    private CategoryStore categoryStore;
    private EntryStore entryStore;

    @Override
    public void onCreate() {
        super.onCreate();
        categoryStore = new CategoryStore(this);
        entryStore = new EntryStore(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            clearDatabase();
            fillDatabaseWithDummyData();
        }

        categoryStore.fetchCategories();
        entryStore.fetchEntries();


    }

    public CategoryStore getCategoryStore() {
        return categoryStore;
    }

    public EntryStore getEntryStore() {
        return entryStore;
    }

    private void clearDatabase() {
        BudgetDbHelper dbHelper = new BudgetDbHelper(this);

        //For some reason, using context.deleteDatabase() spoils the database for subsequent tests
        //instead, it's better to just drop the tables and recreate everything
        dbHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + EntriesTable.TABLE_NAME);
        dbHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " +
                CategoriesTable.TABLE_NAME);
        dbHelper.onCreate(dbHelper.getWritableDatabase());
    }

    private void fillDatabaseWithDummyData() {

        ArrayList<Category> categories = new ArrayList<>();
        categories.add(new Category("cashews"));
        categories.add(new Category("bananas"));
        categories.add(new Category("apples"));
        categories.add(new Category("coffee"));
        categories.add(new Category("tea"));

        for (Category category : categories) {
            try {
                categoryStore.addCategory(category);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
