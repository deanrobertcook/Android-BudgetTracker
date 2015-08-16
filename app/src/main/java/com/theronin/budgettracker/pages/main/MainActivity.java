package com.theronin.budgettracker.pages.main;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetContract;
import com.theronin.budgettracker.model.Entry;
import com.theronin.budgettracker.file.FileWriterTask;
import com.theronin.budgettracker.pages.categories.CategoriesActivity;
import com.theronin.budgettracker.pages.entries.EntriesActivity;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements
        MainMenuFragment.Listener,
        LoaderManager.LoaderCallbacks<Cursor> {

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
        }

        return super.onOptionsItemSelected(item);
    }

    private void backupEntries() {
        getLoaderManager().initLoader(ENTRY_LOADER_ID, null, this);
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

        new FileWriterTask().execute(entries);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }
}
