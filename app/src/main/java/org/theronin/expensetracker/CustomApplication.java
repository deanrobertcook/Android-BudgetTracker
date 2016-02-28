package org.theronin.expensetracker;

import android.app.Application;

import com.localytics.android.LocalyticsActivityLifecycleCallbacks;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;

import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.model.user.UserManager;

import timber.log.Timber;

public class CustomApplication extends Application  {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        registerActivityLifecycleCallbacks(new LocalyticsActivityLifecycleCallbacks(this));
        setupParse();
    }

    private void setupParse() {
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

    public void setDatabase() {
        DbHelper.getInstance(getApplicationContext(), UserManager.getUser(getApplicationContext()).getId());
    }
}
