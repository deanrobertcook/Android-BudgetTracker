package org.theronin.expensetracker.data.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import org.theronin.expensetracker.data.backend.entry.EntryRemoteSync;
import org.theronin.expensetracker.data.backend.entry.EntrySyncCoordinator;
import org.theronin.expensetracker.data.backend.entry.ParseEntryRemoteSync;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceCategory;
import org.theronin.expensetracker.data.source.DataSourceCurrency;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.user.UserManager;
import org.theronin.expensetracker.utils.Prefs;

import java.util.List;

import timber.log.Timber;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final EntryRemoteSync remoteSync;
    private final AbsDataSource<Entry> entryDataSource;

    public SyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.remoteSync = new ParseEntryRemoteSync();

        DbHelper helper = DbHelper.getInstance(getContext(), UserManager.getUser(getContext()).getId());
        this.entryDataSource = new DataSourceEntry(
                getContext(),
                helper,
                new DataSourceCategory(getContext(), helper),
                new DataSourceCurrency(getContext(), helper)
        );
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {
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
