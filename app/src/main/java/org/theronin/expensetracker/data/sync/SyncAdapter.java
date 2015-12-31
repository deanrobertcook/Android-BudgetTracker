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

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
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

        if (shouldFullSyncWithBackend()) {
            fetchAllFromBackend();
        }
    }

    private void pushEntries() {
        Timber.d("pushEntries");
        List<Entry> allEntries = entryDataSource.query();

        EntrySaver entrySaver = new EntrySaver(entryDataSource, remoteSync);
        EntityPushCoordinator<Entry> pushCoordinator = new EntityPushCoordinator<>(entrySaver);
        pushCoordinator.syncEntries(allEntries);
    }

    private boolean shouldFullSyncWithBackend() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(getContext().getString(R.string.pref_newly_created_database), false);
    }

    private void fetchAllFromBackend() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(EntryView.VIEW_NAME);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    Timber.d(objects.size() + " objects pulled down, saving now");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Timber.d("On the thread");
                            List<Entry> entries = new ArrayList<>();
                            for (ParseObject object : objects) {
                                Entry entry = Entry.fromParseObject(object);
                                entries.add(entry);
                                Timber.d("Adding " + entry);
                            }
                            entryDataSource.bulkInsert(entries);
                        }
                    }).start();

                } else {
                    Timber.d("Something went wrong fetching entries");
                    e.printStackTrace();
                }
            }
        });
        setShouldNotFullSyncWithBackend();
    }

    private void setShouldNotFullSyncWithBackend() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.edit().putBoolean(getContext().getString(R.string.pref_newly_created_database), false).apply();
    }
}
