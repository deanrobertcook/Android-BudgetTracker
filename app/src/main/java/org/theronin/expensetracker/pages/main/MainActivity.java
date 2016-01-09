package org.theronin.expensetracker.pages.main;

import android.content.Intent;
import android.os.Bundle;
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
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.ParseUser;

import org.theronin.expensetracker.BuildConfig;
import org.theronin.expensetracker.PlayGroundActivity;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.dagger.InjectedActivity;
import org.theronin.expensetracker.data.Contract;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.backend.SyncState;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.pages.categories.CategoryListFragment;
import org.theronin.expensetracker.pages.entries.list.EntriesAdapter.SelectionListener;
import org.theronin.expensetracker.pages.entries.list.EntryListFragment;
import org.theronin.expensetracker.pages.launch.LaunchActivity;
import org.theronin.expensetracker.pages.settings.SettingsActivity;
import org.theronin.expensetracker.task.FileBackupAgent;
import org.theronin.expensetracker.utils.SyncUtils;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;


public class MainActivity extends InjectedActivity implements
        FileBackupAgent.Listener,
        Drawer.OnDrawerItemClickListener {

    private static final String TAG = MainActivity.class.getName();

    @Inject AbsDataSource<Entry> entryDataSource;

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

        if (!isUserSessionValid()) {
            finish();
            return;
        }
        SyncUtils.requestSync(this);
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
        ProfileDrawerItem profile = new ProfileDrawerItem()
                .withEmail(ParseUser.getCurrentUser().getEmail());

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(profile)
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

        if (BuildConfig.DEBUG) {
            drawerItems = new PrimaryDrawerItem[MainPage.values().length + 1];
            drawerItems[drawerItems.length - 1] = new PrimaryDrawerItem()
                    .withName("PlayGround")
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            Intent intent = new Intent(MainActivity.this, PlayGroundActivity.class);
                            startActivity(intent);
                            return true;
                        }
                    });
        }

        for (int i = 0; i < MainPage.values().length; i++) {
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
        Timber.d("onStart");
        if (!isUserSessionValid()) {
            finish();
            return;
        }
        super.onStart();
    }

    private boolean isUserSessionValid() {
        if (ParseUser.getCurrentUser() == null) {
            Timber.d("User session has expired");
            Intent signInIntent = new Intent(this, LaunchActivity.class);
            startActivity(signInIntent);
            return false;
        }
        return true;
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
    public void onEntriesRestored(List<Entry> entries) {
        if (entries.isEmpty()) {
            Toast.makeText(this, "There were no entries to back up. Make sure permissions are set", Toast.LENGTH_SHORT).show();
        }
        entryDataSource.bulkInsert(entries);
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        //The AccountHeader in the navigation view also has a position
        MainPage page = MainPage.valueOf(position - 1);
        return setPage(page);
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
