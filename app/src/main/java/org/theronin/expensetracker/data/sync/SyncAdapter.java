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
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject AbsDataSource<Entry> entryDataSource;

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

        pushChanges();

        if (shouldFullSyncWithBackend()) {
            fetchAllFromBackend();
        }
    }

    private void pushChanges() {
        Timber.d("pushChanges");
        List<Entry> allEntries = entryDataSource.query();
        List<Entry> entriesToAdd = new ArrayList<>();
        List<Entry> entriesToDeleteRemote = new ArrayList<>();
        List<Entry> entriesToDeleteLocal = new ArrayList<>();
        for (final Entry entry : allEntries) {
            switch (entry.getSyncState()) {
                //TODO separate new from update operation
                case NEW:
                case UPDATED:
                    entriesToAdd.add(entry);
                    break;
                case MARKED_AS_DELETED:
                    entriesToDeleteRemote.add(entry);
                    break;
                case DELETE_SYNCED:
                    entriesToDeleteLocal.add(entry);
                    break;
                case SYNCED:
                    //do nothing
                    break;
            }
        }

        saveEntriesOnBackend(entriesToAdd);
        deleteEntriesOnBackend(entriesToDeleteRemote);

        entryDataSource.bulkDelete(entriesToDeleteLocal);
    }

    private void saveEntriesOnBackend(final List<Entry> entries) {
        Timber.d("Remote saving " + entries.size() + " entries");
        final Map<Long, ParseObject> objects = new HashMap<>();
        for (Entry entry : entries) {
            ParseObject object = entry.toParseObject();
            objects.put(entry.getId(), object);
        }

        try {
            ParseObject.saveAll(new ArrayList<>(objects.values()));
            Timber.d("Remote saving entries succeeded");
            for (Entry entry : entries) {
                entry.setGlobalId(objects.get(entry.getId()).getObjectId());
                entry.setSyncState(SyncState.SYNCED);
            }
            entryDataSource.bulkUpdate(entries);
        } catch (ParseException e) {
            Timber.d("Remote saving entries failed: ");
            e.printStackTrace();
        }
    }

    private void deleteEntriesOnBackend(final List<Entry> entries) {
        Timber.d("Remote deleting: " + entries.size() + " entries");
        List<Entry> notYetSynced = new ArrayList<>();
        final Map<Entry, ParseObject> toRemoveFromBackend = new HashMap<>();

        for (Entry entry : entries) {
            if (!entry.hasGlobalId()) {
                entry.setSyncState(SyncState.DELETE_SYNCED);
                notYetSynced.add(entry);
            } else {
                toRemoveFromBackend.put(entry, entry.toParseObject());
            }
        }
        entryDataSource.bulkUpdate(notYetSynced);

        try {
            ParseObject.deleteAll(new ArrayList<>(toRemoveFromBackend.values()));
            Timber.d("Remote deleting entries successful");
            for (Entry entry : toRemoveFromBackend.keySet()) {
                entry.setSyncState(SyncState.DELETE_SYNCED);
            }
            entryDataSource.bulkUpdate(toRemoveFromBackend.keySet());
        } catch (ParseException e) {
            Timber.d("Remote deleting entries failed: ");
            e.printStackTrace();
        }
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
