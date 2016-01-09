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
import org.theronin.expensetracker.data.backend.PushCoordinator;
import org.theronin.expensetracker.data.backend.RemoteSync;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;

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

    //TODO this should be moved into a class in the backend package
    //TODO The issue was making this available for all entity types
    private void pullEntries() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        long lastSync = pref.getLong("SYNC_CHECK", -1);
        final long syncTime = System.currentTimeMillis();

        RemoteSync.PullResult callback = new RemoteSync.PullResult() {
            @Override
            public void addEntries(List<Entry> entries) {
                entryDataSource.bulkInsert(entries);
            }

            @Override
            public void deleteEntries(List<Entry> entries) {
                entryDataSource.bulkDelete(entries);
            }

            @Override
            public void onComplete() {
                pref.edit().putLong("SYNC_CHECK", syncTime).apply();
            }

            @Override
            public void onFail(Exception e) {
                Timber.i("Pull failed");
                e.printStackTrace();
            }
        };
        remoteSync.findEntries(lastSync, callback);
    }
}
