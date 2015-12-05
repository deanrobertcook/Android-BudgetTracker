package org.theronin.budgettracker;

import android.app.Application;

import timber.log.Timber;

public class BudgetTrackerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
