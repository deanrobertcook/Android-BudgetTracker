package org.theronin.budgettracker.pages.entries;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.BudgetContract.EntryView;
import org.theronin.budgettracker.data.loader.EntryLoader;
import org.theronin.budgettracker.model.Entry;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class EntryListFragment extends Fragment implements
        View.OnClickListener,
        EntriesAdapter.OnItemClickListener,
        EntryOptionsDialogFragment.Container,
        LoaderManager.LoaderCallbacks<List<Entry>> {

    public static final String TAG = EntryListFragment.class.getName();

    private static final int ENTRY_LOADER_ID = 0;

    private static final String SORT_ORDER =
            EntryView.COL_DATE + " DESC, " + EntryView._ID + " DESC";

    private EntriesAdapter adapter;
    private Entry entrySelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(ENTRY_LOADER_ID, null, this);
        Timber.d("onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__entry_list, container, false);

        FloatingActionButton floatingActionButton =
                (FloatingActionButton) rootView.findViewById(R.id.fab__add_entry_button);
        floatingActionButton.setImageDrawable(
                new IconicsDrawable(getActivity())
                        .icon(GoogleMaterial.Icon.gmd_add)
                        .sizeDp(24)
                        .colorRes(R.color.md_white_1000)
        );
        floatingActionButton.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id
                .recycler_view__entry_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new EntriesAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onClick(View listItemView) {
        Timber.d("FAB clicked");
    }

    @Override
    public void onItemClicked(Entry entrySelected) {
        this.entrySelected = entrySelected;
        EntryOptionsDialogFragment fragment = EntryOptionsDialogFragment.newInstance(this);
        fragment.show(getFragmentManager(), "entryClickedDialog");
    }

    @Override
    public void onDeleteClicked() {
        if (entrySelected != null) {
            boolean wasDeleted = ((BudgetTrackerApplication) getActivity().getApplication())
                    .getDataSourceEntry().delete(entrySelected);
            if (wasDeleted) {
                Toast.makeText(getActivity(), "Entry deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
        entrySelected = null;
    }

    @Override
    public Loader<List<Entry>> onCreateLoader(int id, Bundle args) {
        return new EntryLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Entry>> loader, List<Entry> data) {
        adapter.setEntries(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Entry>> loader) {
        adapter.setEntries(new ArrayList<Entry>());
    }
}
