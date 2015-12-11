package org.theronin.budgettracker.pages.main;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.loader.EntryLoader;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.pages.categories.CategoryListFragment;
import org.theronin.budgettracker.pages.entries.EntryListFragment;
import org.theronin.budgettracker.pages.settings.SettingsActivity;
import org.theronin.budgettracker.task.FileBackupAgent;

import java.util.List;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Entry>>,
        FileBackupAgent.Listener,
        Drawer.OnDrawerItemClickListener {

    private static final String TAG = MainActivity.class.getName();

    private static final int ENTRY_LOADER_ID = 0;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    private EntryListFragment entryListFragment;
    private CategoryListFragment categoryListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main);

        toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        toolbar.setTitle("Entries");
        setSupportActionBar(toolbar);

        entryListFragment = new EntryListFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fl__main_content, entryListFragment)
                .commit();

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(getDrawable(R.color.primary))
                .build();

        PrimaryDrawerItem item1 = new PrimaryDrawerItem()
                .withName("Entries")
                .withIdentifier(0)
                .withIcon(getDrawable(R.drawable.ic_entry_unselected))
                .withOnDrawerItemClickListener(this);

        PrimaryDrawerItem item2 = new PrimaryDrawerItem()
                .withName("Categories")
                .withIdentifier(1)
                .withIcon(getDrawable(R.drawable.ic_category_unselected))
                .withOnDrawerItemClickListener(this);

        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(accountHeader)
                .addDrawerItems(item1, item2)
                .build();
        drawerLayout = drawer.getDrawerLayout();

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
    public Loader<List<Entry>> onCreateLoader(int id, Bundle args) {
        return new EntryLoader(this, null, null, null);
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
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        switch (drawerItem.getIdentifier()) {
            case 0:
                toolbar.setTitle("Entries");
                getFragmentManager().beginTransaction()
                        .replace(R.id.fl__main_content, new EntryListFragment())
                        .commit();
                return true;
            case 1:
                toolbar.setTitle("Categories");
                if (categoryListFragment == null) {
                    categoryListFragment = new CategoryListFragment();
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.fl__main_content, new CategoryListFragment())
                        .commit();
                return true;
        }
        return false;
    }


}
