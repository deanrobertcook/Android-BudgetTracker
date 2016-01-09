package org.theronin.expensetracker.pages.entries;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.theronin.expensetracker.dagger.InjectedFragment;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.loader.EntryLoader;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.pages.main.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;

public class EntryListFragment extends InjectedFragment implements
        View.OnClickListener,
        EntriesAdapter.SelectionListener,
        LoaderManager.LoaderCallbacks<List<Entry>>,
        DeleteSelectionDialogFragment.Container {

    public static final String TAG = EntryListFragment.class.getName();

    private static final int ENTRY_LOADER_ID = 0;

    @Inject AbsDataSource<Entry> entryDataSource;

    private EntriesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(ENTRY_LOADER_ID, null, this);
        Timber.d("onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__entry_list, container, false);

        FloatingActionButton floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fab__add_entry_button);
        floatingActionButton.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view__entry_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new EntriesAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onClick(View button) {
        switch (button.getId()) {
            case R.id.fab__add_entry_button:
                Timber.d("FAB clicked!");
                Intent intent = new Intent(getActivity(), EntryDialogActivity.class);
                getActivity().startActivity(intent);
        }
    }

    @Override
    public Loader<List<Entry>> onCreateLoader(int id, Bundle args) {
        return new EntryLoader(getActivity(), this);
    }

    @Override
    public void onLoadFinished(Loader<List<Entry>> loader, List<Entry> data) {
        adapter.setEntries(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Entry>> loader) {
        adapter.setEntries(new ArrayList<Entry>());
    }

    @Override
    public void onPause() {
        adapter.exitSelectMode();
        super.onPause();
    }

    @Override
    public void onEnterSelectMode() {
        Timber.d("onEnterSelectMode");
        ((MainActivity) getActivity()).setSelectMode(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onExitSelectMode() {
        Timber.d("onExitSelectMode");
        ((MainActivity) getActivity()).setSelectMode(false);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onItemSelected(int count, String amount) {
        Timber.d(count + " items selected");
        String display = count == 1 ? (count + " entry: " + amount) : (count + " entries: " + amount);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(display);
    }

    @Override
    public void deleteSelection() {
        Timber.d("deleteSelection");
        int count = adapter.getSelection().size();
        DeleteSelectionDialogFragment dialogFragment = DeleteSelectionDialogFragment.newInstance(this, count);
        dialogFragment.show(getFragmentManager(), DeleteSelectionDialogFragment.TAG);
    }

    @Override
    public void onDeleteSelectionConfirmed() {
        Timber.d("onDeleteSelectionConfirmed");
        Set<Entry> selectedEntries = adapter.getSelection();

        //TODO abstract away the mark as deleted functionality
        ((DataSourceEntry) entryDataSource).bulkMarkAsDeleted(selectedEntries);

        Toast.makeText(getActivity(), selectedEntries.size() + " entries deleted.", Toast.LENGTH_SHORT).show();
        adapter.exitSelectMode();
    }

    @Override
    public void cancelSelection() {
        Timber.d("cancelSelection");
        adapter.exitSelectMode();
    }


}
