package org.theronin.expensetracker.data.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.backend.entry.EntryRemoteSync;
import org.theronin.expensetracker.data.backend.entry.EntrySyncCoordinator;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.user.UserManager;
import org.theronin.expensetracker.utils.Prefs;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

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

        if (!UserManager.getUser(getContext()).canSync()) {
            return;
        }

        pushEntries();
        pullEntries();

    }

    private void pushEntries() {
        Timber.d("pushEntries");
        List<Entry> allEntries = entryDataSource.query();
        EntrySyncCoordinator entrySyncCoordinator = new EntrySyncCoordinator(
                UserManager.getUser(getContext()), entryDataSource, remoteSync);
        entrySyncCoordinator.syncEntries(allEntries);
    }

    private void pullEntries() {
        new EntrySyncCoordinator(UserManager.getUser(getContext()),
                entryDataSource, remoteSync).findEntries(Prefs.getLastSyncTime(getContext()));
        Prefs.setLastSyncTime(System.currentTimeMillis(), getContext());
    }
}
