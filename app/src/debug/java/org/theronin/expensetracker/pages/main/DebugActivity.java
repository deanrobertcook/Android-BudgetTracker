package org.theronin.expensetracker.pages.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.Contract;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.user.UserManager;
import org.theronin.expensetracker.task.FileBackupAgent;

import java.util.List;

public class DebugActivity extends MainActivity implements
        FileBackupAgent.Listener {


    private AbsDataSource<Entry> entryDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entryDataSource = DataSourceEntry.newInstance(this,
                DbHelper.getInstance(this, UserManager.getUser(this).getId()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!super.onCreateOptionsMenu(menu)) {
            getMenuInflater().inflate(R.menu.menu_debug, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_backup:
                backupEntries();
                break;
            case R.id.action_restore:
                restoreEntries();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void backupEntries() {
        //TODO tidy this up
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FileBackupAgent().backupEntries(entryDataSource
                        .query(Contract.EntryView.COL_SYNC_STATUS + " NOT IN (?)", new String[]{SyncState.deleteStateSelection()}, null));
            }
        }).start();
    }

    private void restoreEntries() {
        new FileBackupAgent().restoreEntriesFromBackup(this);
    }

    @Override
    public void onEntriesRestored(List<Entry> entries) {
        if (entries.isEmpty()) {
            Toast.makeText(this, "There were no entries to back up. Make sure permissions are set", Toast.LENGTH_SHORT).show();
        }
        entryDataSource.bulkInsert(entries);
    }

}
