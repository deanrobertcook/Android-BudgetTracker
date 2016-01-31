package org.theronin.expensetracker.data.loader;

import android.content.Context;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class EntryLoader extends DataLoader<Entry> {

    @Inject AbsDataSource<Entry> entryDataSource;

    public EntryLoader(Context context, InjectedComponent component) {
        super(context, component);
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
