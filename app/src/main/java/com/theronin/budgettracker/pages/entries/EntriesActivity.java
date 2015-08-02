package com.theronin.budgettracker.pages.entries;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.theronin.budgettracker.BudgetTrackerApplication;
import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.EntryStore;

public class EntriesActivity extends FragmentActivity implements
        AddEntryFragment.Container,
        EntryListFragment.Container {

    private BudgetTrackerApplication application;
    private EntryListFragment entryListFragment;
    private AddEntryFragment addEntryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__entries);

        application = (BudgetTrackerApplication) getApplication();

        entryListFragment = (EntryListFragment) getFragmentManager()
                .findFragmentById(R.id.fragment__entry_list);

        addEntryFragment = (AddEntryFragment) getFragmentManager()
                .findFragmentById(R.id.fragment__add_entry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        application.getEntryStore().addObserver(entryListFragment);
        application.getEntryStore().notifyObservers();

        application.getCategoryStore().addObserver(addEntryFragment);
        application.getCategoryStore().notifyObservers();
    }


    @Override
    protected void onPause() {
        application.getEntryStore().removeObserver(entryListFragment);
        application.getCategoryStore().removeObserver(addEntryFragment);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        entryListFragment = null;
        addEntryFragment = null;

        application = null;

        super.onDestroy();
    }

    @Override
    public EntryStore getEntryStore() {
        return application.getEntryStore();
    }
}
