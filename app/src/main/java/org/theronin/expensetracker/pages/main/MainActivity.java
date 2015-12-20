package org.theronin.expensetracker.pages.main;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.ParseUser;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.DataSourceCategory;
import org.theronin.expensetracker.data.loader.EntryLoader;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.pages.categories.CategoryDialogFragment;
import org.theronin.expensetracker.pages.categories.CategoryListFragment;
import org.theronin.expensetracker.pages.entries.EntriesAdapter.SelectionListener;
import org.theronin.expensetracker.pages.entries.EntryListFragment;
import org.theronin.expensetracker.pages.settings.SettingsActivity;
import org.theronin.expensetracker.task.FileBackupAgent;

import java.util.List;

import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Entry>>,
        FileBackupAgent.Listener,
        Drawer.OnDrawerItemClickListener,
        CategoryDialogFragment.Container {

    private static final String TAG = MainActivity.class.getName();

    private static final int ENTRY_LOADER_ID = 0;
    private Toolbar toolbar;

    private Drawer navDrawer;

    private boolean selectMode;

    private final static String CURRENT_PAGE_KEY = "CURRENT_PAGE";
    private MainPage currentPage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__basic);

        toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        //TODO figure out why having this here prevents "BudgetTracker" from appearing...
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        navDrawer = buildNavigationDrawer();

        currentPage = findCurrentPage(savedInstanceState);
        setPage(currentPage);
    }

    private MainPage findCurrentPage(Bundle savedInstanceState) {
        MainPage defaultPage = MainPage.ENTRIES;
        if (savedInstanceState != null) {
            int currentPageId = savedInstanceState.getInt(CURRENT_PAGE_KEY, -1);
            if (currentPageId > -1) {
                return MainPage.valueOf(currentPageId);
            }
        }
        return defaultPage;
    }

    private boolean setPage(MainPage page) {
        if (page == null) {
            return false;
        }
        currentPage = page;
        navDrawer.setSelection(page.id);

        switch (page) {
            case ENTRIES:
                Timber.d("Creating Entries List");
                EntryListFragment entryListFragment = (EntryListFragment)
                        getFragmentManager().findFragmentByTag(EntryListFragment.TAG);

                if (entryListFragment == null) {
                    entryListFragment = new EntryListFragment();
                    //only set the title if the fragment is null
                    //TODO this feels dirty
                    toolbar.setTitle(page.title);
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.fl__main_content,
                                entryListFragment,
                                EntryListFragment.TAG)
                        .commit();
                return true;

            case CATEGORIES:
                Timber.d("Creating Categories List");
                CategoryListFragment categoryListFragment = (CategoryListFragment)
                        getFragmentManager().findFragmentByTag(CategoryListFragment.TAG);

                if (categoryListFragment == null) {
                    categoryListFragment = new CategoryListFragment();
                    //only set the title if the fragment is null
                    //TODO this feels dirty
                    toolbar.setTitle(page.title);
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.fl__main_content,
                                categoryListFragment,
                                CategoryListFragment.TAG)
                        .commit();
                return true;
            default:
                return false;
        }
    }

    private Drawer buildNavigationDrawer() {
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(getDrawable(R.color.primary))
                .build();

        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(buildPrimaryDrawerItems())
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        return drawer;
    }

    private PrimaryDrawerItem[] buildPrimaryDrawerItems() {
        PrimaryDrawerItem[] drawerItems = new PrimaryDrawerItem[MainPage.values().length];
        for (int i = 0; i < drawerItems.length; i++) {
            MainPage page = MainPage.values()[i];
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
    protected void onStart() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Timber.d("Current user: " + currentUser.getEmail());

        } else {
            Timber.d("No user logged in");
        }
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_PAGE_KEY, currentPage.id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (selectMode) {
            getMenuInflater().inflate(R.menu.menu_select_mode, menu);
        } else {
            toolbar.setTitle(currentPage.title);
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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

            /**
             * We can count on their being a SelectionListener ONLY if these menu items exist
             */
            case R.id.action_delete_selection:
                deleteSelection();
                return true;
            case R.id.action_cancel_selection:
                cancelSelection();
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

    private void deleteSelection() {
        selectionListener().deleteSelection();
    }

    private SelectionListener selectionListener() {
        return (SelectionListener) getFragmentManager().findFragmentById(R.id.fl__main_content);
    }

    private void cancelSelection() {
        selectionListener().cancelSelection();
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
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
        ((CustomApplication) getApplication()).getDataSourceEntry().bulkInsert(entries);
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        //The AccountHeader in the navigation view also has a position
        MainPage page = MainPage.valueOf(position - 1);
        return setPage(page);
    }

    @Override
    public void onCategoryCreated(String categoryName) {
        if (categoryName != null && categoryName.length() > 0) {
            DataSourceCategory dataSource = ((CustomApplication) getApplication()).getDataSourceCategory();
            long id = dataSource.insert(new Category(sanitiseCategoryName(categoryName)));
            if (id == -1) {
                Toast.makeText(this, R.string.duplicate_category_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.category_creation_success, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.empty_category_name_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String sanitiseCategoryName(String categoryName) {
        categoryName = categoryName.toLowerCase();
        categoryName = categoryName.trim();
        return categoryName;
    }

    @Override
    public void onBackPressed() {
        if (selectMode) {
            selectionListener().cancelSelection();
        } else {
            super.onBackPressed();
        }
    }
}
