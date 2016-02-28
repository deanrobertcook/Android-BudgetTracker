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

    private final InjectedComponent injectedComponent;
    @Inject AbsDataSource<Entry> entryDataSource;
    @Inject EntryRemoteSync remoteSync;

    public SyncAdapter(Context context, InjectedComponent injectedComponent, boolean autoInitialize) {
        this(context, injectedComponent, autoInitialize, false);
    }

    public SyncAdapter(Context context,
                       InjectedComponent injectedComponent,
                       boolean autoInitialize,
                       boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.injectedComponent = injectedComponent;
        Timber.d("SyncAdapter created");
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {

        try {
            injectedComponent.inject(this);
        } catch (IllegalStateException e) {
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
