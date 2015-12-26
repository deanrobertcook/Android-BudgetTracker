package org.theronin.expensetracker.data.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.parse.ParseObject;
import com.parse.ParseUser;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.model.Entry;

import java.util.List;

import timber.log.Timber;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private CustomApplication app;

    public SyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Timber.d("SyncAdapter created");
        this.app = (CustomApplication) context;
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {

        Timber.d("Syncing data with server!!! Extras?: " + extras.getString("TEST", "Nothing there..."));
        List<Entry> entryList = app.getDataSourceEntry().query();

        for (Entry entry : entryList) {
            ParseObject parseEntry = new ParseObject("entry");
            parseEntry.put("amount", entry.amount);
            parseEntry.put("category", entry.category.name);
            parseEntry.put("currency", entry.currency.code);
            parseEntry.put("date", entry.utcDate);
            parseEntry.saveInBackground();
        }

        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            Timber.d("Syncing data for user: " + user.getEmail());
        } else {
            Timber.d("No user logged in");
        }
    }
}
