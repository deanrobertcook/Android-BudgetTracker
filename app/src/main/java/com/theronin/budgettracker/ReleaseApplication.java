package com.theronin.budgettracker;

import android.app.Application;

import com.theronin.budgettracker.model.CategoryStore;
import com.theronin.budgettracker.model.EntryStore;

public class ReleaseApplication extends Application {

    private CategoryStore categoryStore;
    private EntryStore entryStore;

    @Override
    public void onCreate() {
        super.onCreate();
        categoryStore = new CategoryStore(this);
        entryStore = new EntryStore(this);

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
