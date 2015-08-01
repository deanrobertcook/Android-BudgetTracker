package com.theronin.budgettracker.pages.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.theronin.budgettracker.BudgetTrackerApplication;
import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.EntryStore;
import com.theronin.budgettracker.pages.categories.CategoriesActivity;
import com.theronin.budgettracker.pages.entries.AddEntryFragment;
import com.theronin.budgettracker.pages.entries.EntriesActivity;


public class MainActivity extends AppCompatActivity implements
        MainMenuFragment.Listener,
        AddEntryFragment.Container {

    private static final String TAG = MainActivity.class.getName();
    private BudgetTrackerApplication application;
    private AddEntryFragment addEntryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main);
        application = (BudgetTrackerApplication) getApplication();

        addEntryFragment = (AddEntryFragment)
                getFragmentManager().findFragmentById(R.id.fragment__add_entry);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        application.getCategoryStore().addObserver(addEntryFragment);
        application.getCategoryStore().notifyObservers();
    }

    @Override
    protected void onPause() {
        application.getCategoryStore().removeObserver(addEntryFragment);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        addEntryFragment = null;
        application = null;
        super.onDestroy();
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
    public EntryStore getEntryStore() {
        return application.getEntryStore();
    }
}
