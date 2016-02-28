package org.theronin.expensetracker.data.loader;

import android.content.Context;

import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.user.UserManager;

import java.util.List;

import timber.log.Timber;

public class EntryLoader extends DataLoader<Entry> {

    private AbsDataSource<Entry> entryDataSource;

    public EntryLoader(Context context) {
        super(context);
        entryDataSource = DataSourceEntry.newInstance(getContext(),
                DbHelper.getInstance(getContext(), UserManager.getUser(getContext()).getId()));
        setObservedDataSources(entryDataSource);
    }

    @Override
    public List<Entry> loadInBackground() {
        Timber.i("loadInBackground");
        List<Entry> entries = entryDataSource.query(
                EntryView.COL_SYNC_STATUS + " NOT IN (" + SyncState.deleteStateSelection() + ")", null,
                EntryView.COL_DATE + " DESC, " + EntryView._ID + " DESC");

        assignHomeAmountsToEntries(entries);

        Timber.i("Returning " + entries.size() + " entries");
        return entries;
    }
}
