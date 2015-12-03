package org.theronin.budgettracker.pages.main;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.BudgetContract.EntriesView;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.pages.categories.CategoriesActivity;
import org.theronin.budgettracker.pages.entries.EntriesActivity;
import org.theronin.budgettracker.task.ExchangeRateDownloadAgent;
import org.theronin.budgettracker.task.FileBackupAgent;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements
        MainMenuFragment.Listener,
        LoaderManager.LoaderCallbacks<Cursor>,
        FileBackupAgent.Listener, ExchangeRateDownloadAgent.Listener {

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
        switch (id) {
            case ENTRY_LOADER_ID:
                return new CursorLoader(
                        this,
                        EntriesView.CONTENT_URI,
                        EntriesView.PROJECTION,
                        null, null, null
                );
            default:
                throw new RuntimeException("Unrecognised loader id");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ENTRY_LOADER_ID:
                handleLoadedEntries(data);
                break;
            default:
                throw new RuntimeException("Unrecognised loader id");
        }
    }

    private void handleLoadedEntries(Cursor data) {
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
        ContentValues[] valuesArray = new ContentValues[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            valuesArray[i] = entries.get(i).toValues();
        }
        getContentResolver().bulkInsert(EntriesView.CONTENT_URI, valuesArray);
    }

    @Override
    public void onExchangeRatesDownloaded(List<ExchangeRate> rates) {
        for (ExchangeRate rate: rates) {
            Log.d("EXCHANGE RATES", rate.toString());
        }
    }
}
