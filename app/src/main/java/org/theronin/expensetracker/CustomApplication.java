package org.theronin.expensetracker;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

import org.theronin.expensetracker.data.DataSourceCategory;
import org.theronin.expensetracker.data.DataSourceCurrency;
import org.theronin.expensetracker.data.DataSourceEntry;
import org.theronin.expensetracker.data.DataSourceExchangeRate;

import timber.log.Timber;

public class CustomApplication extends Application {

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
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this);

        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    public void setDatabase() {
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
