package org.theronin.budgettracker.pages.entries;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import org.theronin.budgettracker.model.Entry;

public class EntryListFragment extends Fragment implements
        View.OnClickListener,
        EntriesAdapter.OnItemClickListener,
        EntryOptionsDialogFragment.Container,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ENTRY_LOADER_ID = 0;

    private EntriesAdapter adapter;
    private Entry entrySelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(ENTRY_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__entry_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id
                .recycler_view__entry_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new EntriesAdapter(getActivity(), null, this);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onClick(View listItemView) {
        TextView categoryText = (TextView) listItemView.findViewById(R.id.tv__category_column);
        Toast.makeText(getActivity(), categoryText.getText(), Toast.LENGTH_SHORT).show();
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
            int numDeleted = getActivity().getContentResolver().delete(
                    EntriesTable.CONTENT_URI.buildUpon().appendPath(
                            Long.toString(entrySelected.id)).build(),
                    null, null);
            if (numDeleted == 1) {
                Toast.makeText(getActivity(), "Entry deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
        entrySelected = null;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                EntriesTable.CONTENT_URI,
                Entry.projection,
                null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}
