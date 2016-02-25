package org.theronin.expensetracker.pages.main;

import android.view.Menu;
import android.view.MenuItem;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.Contract;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.task.FileBackupAgent;

public class DebugActivity extends MainActivity {

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
}
