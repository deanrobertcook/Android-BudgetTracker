package org.theronin.budgettracker.pages.main;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
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

        buildNavigationDrawer();

    }

    private void buildNavigationDrawer() {
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(getDrawable(R.color.primary))
                .build();

        new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(accountHeader)
                .addDrawerItems(buildPrimaryDrawerItems())
                .build();
    }

    private PrimaryDrawerItem[] buildPrimaryDrawerItems() {
        PrimaryDrawerItem[] drawerItems = new PrimaryDrawerItem[Page.values().length];
        for (int i = 0; i < drawerItems.length; i++) {
            Page page = Page.values()[i];
            drawerItems[i] = new PrimaryDrawerItem()
                    .withName(page.title)
                    .withIdentifier(page.ordinal())
                    .withIcon(page.unselectedIconResId)
                    .withSelectedIcon(page.selectedIconResId)
                    .withOnDrawerItemClickListener(this);
        }
        return drawerItems;
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
        Page page = Page.valueOf(position - 1);
        if (page == null) {
            return false;
        }
        switch (page) {
            case ENTRIES:
                toolbar.setTitle(page.title);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fl__main_content, new EntryListFragment())
                        .commit();
                return true;
            case CATEGORIES:
                toolbar.setTitle(page.title);
                if (categoryListFragment == null) {
                    categoryListFragment = new CategoryListFragment();
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.fl__main_content, new CategoryListFragment())
                        .commit();
                return true;
            default:
                return false;
        }
    }


}
