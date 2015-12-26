package org.theronin.expensetracker.data.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.model.Entry;

import java.util.List;

import timber.log.Timber;

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

            switch (entry.syncState) {
                //TODO separate new from update operation
                case NEW:
                case UPDATED:
                    saveEntryOnBackend(entry);
                    break;
                case DELETED:
                    deleteEntryOnBackend(entry);
                    break;
                case SYNCED:
                    //do nothing
                    break;
            }
        }
    }

    private void saveEntryOnBackend(final Entry entry) {
        Timber.d("Syncing entry: " + entry);
        final ParseObject parseObject = new ParseObject(EntryView.VIEW_NAME);
        parseObject.put("amount", entry.amount);
        parseObject.put("category", entry.category.name);
        parseObject.put("currency", entry.currency.code);
        parseObject.put("date", entry.utcDate);
        parseObject.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Entry updatedEntry = new Entry(
                            entry.id,
                            parseObject.getObjectId(),
                            SyncState.SYNCED,
                            entry.utcDate,
                            entry.amount,
                            entry.category,
                            entry.currency
                    );
                    app.getDataSourceEntry().update(updatedEntry);
                } else {
                    Timber.d("Saving entry failed: ");
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteEntryOnBackend(final Entry entry) {
        if (entry.globalId == null || entry.globalId.length() == 0) {
            throw new IllegalStateException("The global ID must exist for it to be deleted");
        }
        ParseObject.createWithoutData(EntryView.VIEW_NAME, entry.globalId).deleteEventually(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    app.getDataSourceEntry().delete(entry);
                } else {
                    Timber.d("Deleting entry failed: ");
                    e.printStackTrace();
                }
            }
        });
    }
}
