package com.theronin.budgettracker.pages.main;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetContract;
import com.theronin.budgettracker.file.FileBackupAgent;
import com.theronin.budgettracker.model.Entry;
import com.theronin.budgettracker.pages.categories.CategoriesActivity;
import com.theronin.budgettracker.pages.entries.EntriesActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements
        MainMenuFragment.Listener,
        LoaderManager.LoaderCallbacks<Cursor>,
        FileBackupAgent.Listener {

    private static final String TAG = MainActivity.class.getName();
    private static final int ENTRY_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_backup:
                backupEntries();
                return true;
            case R.id.action_restore:
                restoreEntries();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void backupEntries() {
        getLoaderManager().initLoader(ENTRY_LOADER_ID, null, this);
    }

    private void restoreEntries() {
        new FileBackupAgent().restoreEntriesFromBackup(this);
    }

    @Override
    public void onEntriesMenuItemClicked() {
        Intent intent = new Intent(this, EntriesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCategoriesMenuItemClicked() {
        Intent intent = new Intent(this, CategoriesActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                BudgetContract.EntriesTable.CONTENT_URI,
                Entry.projection,
                null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<Entry> entries = new ArrayList<>();
        while (data.moveToNext()) {
            Entry entry = Entry.fromCursor(data);
            entries.add(entry);
        }

        new FileBackupAgent().backupEntries(entries);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }

    @Override
    public void onEntriesRestored(List<Entry> entries) {
        for (Entry entry : entries) {
            Log.d(TAG, entry.amount + ", " + entry.categoryName + ", " + entry.dateEntered);
        }
    }
}
