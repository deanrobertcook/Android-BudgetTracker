package org.theronin.budgettracker;

import android.app.Application;

import org.theronin.budgettracker.data.DataSourceCategory;
import org.theronin.budgettracker.data.DataSourceCurrency;
import org.theronin.budgettracker.data.DataSourceEntry;
import org.theronin.budgettracker.data.DataSourceExchangeRate;

import timber.log.Timber;

public class BudgetTrackerApplication extends Application {

    private DataSourceEntry dataSourceEntry;
    private DataSourceCategory dataSourceCategory;
    private DataSourceCurrency dataSourceCurrency;
    private DataSourceExchangeRate dataSourceExchangeRate;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        dataSourceEntry = new DataSourceEntry(this);
        dataSourceCategory = new DataSourceCategory(this);
        dataSourceCurrency = new DataSourceCurrency(this);
        dataSourceExchangeRate = new DataSourceExchangeRate(this);
    }

    public DataSourceEntry getDataSourceEntry() {
        return dataSourceEntry;
    }

    public DataSourceCategory getDataSourceCategory() {
        return dataSourceCategory;
    }

    public DataSourceCurrency getDataSourceCurrency() {
        return dataSourceCurrency;
    }

    public DataSourceExchangeRate getDataSourceExchangeRate() {
        return dataSourceExchangeRate;
    }
}
