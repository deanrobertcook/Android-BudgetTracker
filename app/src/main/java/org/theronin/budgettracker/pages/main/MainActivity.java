package org.theronin.budgettracker.pages.main;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.loader.EntryLoader;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.pages.categories.CategoriesActivity;
import org.theronin.budgettracker.pages.entries.EntriesActivity;
import org.theronin.budgettracker.pages.settings.SettingsActivity;
import org.theronin.budgettracker.task.ExchangeRateDownloadAgent;
import org.theronin.budgettracker.task.FileBackupAgent;

import java.util.List;


public class MainActivity extends FragmentActivity implements
        MainMenuFragment.Listener,
        LoaderManager.LoaderCallbacks<List<Entry>>,
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
                showSettings();
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

    private void showSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
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
    public Loader<List<Entry>> onCreateLoader(int id, Bundle args) {
        return new EntryLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Entry>> loader, List<Entry> data) {
        new FileBackupAgent().backupEntries(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Entry>> loader) {
        //do nothing
    }

    @Override
    public void onEntriesRestored(List<Entry> entries) {
        ((BudgetTrackerApplication) getApplication()).getDataSourceEntry().bulkInsert(entries);
    }

    @Override
    public void onExchangeRatesDownloaded(List<ExchangeRate> rates) {
        for (ExchangeRate rate: rates) {
            Log.d("EXCHANGE RATES", rate.toString());
        }
    }
}
