package org.theronin.expensetracker.data.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.model.Entry;

import java.util.List;

import timber.log.Timber;

import static org.theronin.expensetracker.data.Contract.EntryTable.SYNC_STATUS_DELETE;
import static org.theronin.expensetracker.data.Contract.EntryTable.SYNC_STATUS_NEW;
import static org.theronin.expensetracker.data.Contract.EntryTable.SYNC_STATUS_SYNCED;
import static org.theronin.expensetracker.data.Contract.EntryTable.SYNC_STATUS_UPDATE;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private CustomApplication app;

    public SyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Timber.d("SyncAdapter created");
        this.app = (CustomApplication) context;
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {
        List<Entry> entryList = app.getDataSourceEntry().query();
        for (final Entry entry : entryList) {

            switch (entry.syncStatus) {
                //TODO separate new from update operation
                case SYNC_STATUS_NEW:
                case SYNC_STATUS_UPDATE:
                    syncEntry(entry);
                    break;
                case SYNC_STATUS_DELETE:
                    break;
                case SYNC_STATUS_SYNCED:
                    //do nothing
                    break;
            }
        }
    }

    private void syncEntry(final Entry entry) {
        Timber.d("Syncing entry: " + entry);
        final ParseObject parseEntry = new ParseObject("entry");
        parseEntry.put("amount", entry.amount);
        parseEntry.put("category", entry.category.name);
        parseEntry.put("currency", entry.currency.code);
        parseEntry.put("date", entry.utcDate);
        parseEntry.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    syncSuccess(parseEntry, entry);
                } else {
                    syncFail(parseEntry, entry);
                }
            }
        });
    }

    private void syncSuccess(ParseObject object, Entry entry) {
        Entry updatedEntry = new Entry(
                entry.id,
                object.getObjectId(),
                SYNC_STATUS_SYNCED,
                entry.utcDate,
                entry.amount,
                entry.category,
                entry.currency
        );
        app.getDataSourceEntry().update(updatedEntry);
    }

    private void syncFail(ParseObject object, Entry entry) {
        Timber.d("Sync failed for: " + entry);
    }
}
