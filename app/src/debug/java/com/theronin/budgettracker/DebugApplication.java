package com.theronin.budgettracker;

import com.theronin.budgettracker.data.BudgetDbHelper;

import timber.log.Timber;

public class DebugApplication extends ReleaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
        BudgetDbHelper dbHelper = new BudgetDbHelper(this);
        DatabaseDevUtils.clearDatabase(dbHelper);
        DatabaseDevUtils.fillDatabaseWithDummyData(
                dbHelper,
                new String[] {"cashews", "bananas", "apples", "coffee", "tea",
                "burritos", "chinese", "climbing", "bagels", "cups", "paper",
                "pens", "books", "toilet paper", "tissues", "ear phones", "rulers",
                "irons", "pencils", "spoons", "plane tickets"},
                200, 10000);
        dbHelper.close();
    }
}
