package org.theronin.expensetracker;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.theronin.expensetracker.data.source.DataSourceCategory;
import org.theronin.expensetracker.data.source.DataSourceCurrency;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DataSourceExchangeRate;
import org.theronin.expensetracker.data.source.DbHelper;

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

        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public String getDatabaseName() {
        if (ParseUser.getCurrentUser() == null) {
            throw new IllegalStateException("The current user is null, there is no database " +
                    "that can be created or retrieved");
        }
        return ParseUser.getCurrentUser().getObjectId();
    }

    public void setDatabase() {
        DbHelper dbHelper = DbHelper.getInstance(getApplicationContext(), getDatabaseName());

        dataSourceCurrency = new DataSourceCurrency(this, dbHelper);
        dataSourceExchangeRate = new DataSourceExchangeRate(this, dbHelper);
        dataSourceCategory = new DataSourceCategory(this, dbHelper);
        dataSourceEntry = new DataSourceEntry(this, dbHelper, dataSourceCategory, dataSourceCurrency);
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
