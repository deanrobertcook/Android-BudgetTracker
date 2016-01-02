package org.theronin.expensetracker.data.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject AbsDataSource<Entry> entryDataSource;
    @Inject RemoteSync remoteSync;

    //TODO fix this ugly hack to prevent the SyncAdapter from running in tests
    private boolean execute;

    public SyncAdapter(Context context, InjectedComponent injectedComponent, boolean autoInitialize) {
        this(context, autoInitialize, false);
        try {
            injectedComponent.inject(this);
            execute = true;
        } catch (IllegalStateException e) {
            execute = false;
        }

    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Timber.d("SyncAdapter created");
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {
        if (!execute) {
            return;
        }

        pushEntries();
        pullEntries();

    }

    private void pushEntries() {
        Timber.d("pushEntries");
        List<Entry> allEntries = entryDataSource.query();
        PushCoordinator<Entry> pushCoordinator = new PushCoordinator<>(entryDataSource, remoteSync);
        pushCoordinator.syncEntries(allEntries);
    }

    private void pullEntries() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean firstSync = pref.getBoolean("FIRST_SYNC", true);
        long lastChecked = pref.getLong("SYNC_CHECK", -1);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("entry");
        query.setLimit(1000);

        if (firstSync) {
            query.whereEqualTo("isDeleted", false);
        }

        if (lastChecked > -1) {
            Date date = new Date(lastChecked);
            query.whereGreaterThan("updatedAt", date);
        }
        final long requestTime = System.currentTimeMillis();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    List<Entry> toInsert = new ArrayList<>();
                    List<Entry> toDelete = new ArrayList<>();
                    for (ParseObject object : objects) {
                        Entry entry = Entry.fromParseObject(object);
                        if (entry.getSyncState() == SyncState.DELETE_SYNCED) {
                            Timber.i("Deleting " + entry);
                            toDelete.add(entry);
                        } else {
                            toInsert.add(entry);
                            Timber.i("Adding " + entry);
                        }
                    }
                    entryDataSource.bulkInsert(toInsert);
                    entryDataSource.bulkDelete(toDelete);

                    pref.edit()
                            .putLong("SYNC_CHECK", requestTime)
                            .putBoolean("FIRST_SYNC", false)
                            .apply();
                } else {
                    Timber.d("Something went wrong fetching entries");
                    e.printStackTrace();
                }
            }
        });
    }
}
