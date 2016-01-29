package org.theronin.expensetracker;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.source.DbHelper;

import dagger.ObjectGraph;
import timber.log.Timber;

public class CustomApplication extends Application implements InjectedComponent {

    private ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

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
        DbHelper dbHelper = DbHelper.getInstance(getApplicationContext(), getDatabaseName());
        graph = ObjectGraph.create(new AppModule(this, dbHelper));
    }

    @Override
    public void inject(Object object) {
        if (graph == null) {
            throw new IllegalStateException("The Object graph has not been established yet. This" +
                    " means the database is also not ready");
        }
        graph.inject(object);
    }

    private String getDatabaseName() {
        if (ParseUser.getCurrentUser() == null) {
            throw new IllegalStateException("The current user is null, there is no database " +
                    "that can be created or retrieved");
        }
        return ParseUser.getCurrentUser().getObjectId();
    }
}
