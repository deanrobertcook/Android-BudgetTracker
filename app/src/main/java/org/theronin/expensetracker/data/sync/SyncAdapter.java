package org.theronin.expensetracker.data.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.backend.entry.EntryRemoteSync;
import org.theronin.expensetracker.data.backend.entry.EntrySyncCoordinator;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String PREF_LAST_SYNC_CHECK_KEY = "SYNC_CHECK";

    @Inject AbsDataSource<Entry> entryDataSource;
    @Inject EntryRemoteSync remoteSync;

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
        EntrySyncCoordinator entrySyncCoordinator = new EntrySyncCoordinator(entryDataSource, remoteSync);
        entrySyncCoordinator.syncEntries(allEntries);
    }

    private void pullEntries() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        long lastSync = pref.getLong(PREF_LAST_SYNC_CHECK_KEY, -1);
        final long syncTime = System.currentTimeMillis();
        new EntrySyncCoordinator(entryDataSource, remoteSync).findEntries(lastSync);
        pref.edit().putLong(PREF_LAST_SYNC_CHECK_KEY, syncTime).apply();

    }
}
