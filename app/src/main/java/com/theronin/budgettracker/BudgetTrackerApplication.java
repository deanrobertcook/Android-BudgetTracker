package com.theronin.budgettracker;

import android.app.Application;

import com.theronin.budgettracker.data.BudgetDbHelper;
import com.theronin.budgettracker.model.CategoryStore;
import com.theronin.budgettracker.model.EntryStore;

import timber.log.Timber;

public class BudgetTrackerApplication extends Application {

    private CategoryStore categoryStore;
    private EntryStore entryStore;

    @Override
    public void onCreate() {
        super.onCreate();
        categoryStore = new CategoryStore(this);
        entryStore = new EntryStore(this);

        /**
         * If a DEV version, fill the app with some dummy data for checking
         */
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            BudgetDbHelper dbHelper = new BudgetDbHelper(this);
            DatabaseDevUtils.clearDatabase(dbHelper);
            DatabaseDevUtils.fillDatabaseWithDummyData(
                    dbHelper,
                    new String[] {"cashews", "bananas", "apples", "coffee", "tea"},
                    200, 10000);
            dbHelper.close();
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

}
