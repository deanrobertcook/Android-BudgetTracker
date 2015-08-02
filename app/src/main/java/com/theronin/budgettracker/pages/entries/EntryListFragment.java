package com.theronin.budgettracker.pages.entries;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.Entry;
import com.theronin.budgettracker.model.EntryStore;

import java.util.List;

public class EntryListFragment extends Fragment implements EntryStore.Observer,
        View.OnClickListener,
        EntriesAdapter.OnItemClickListener {

    private EntriesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__entry_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view__entry_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new EntriesAdapter(this);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onEntriesLoaded(List<Entry> entries) {
        adapter.setEntries(entries);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View listItemView) {
        TextView categoryText = (TextView) listItemView.findViewById(R.id.tv__category_column);
        Toast.makeText(getActivity(), categoryText.getText(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClicked(Entry entrySelected) {
        Toast.makeText(getActivity(), "Entry: " + entrySelected.amount + ", " + entrySelected.categoryName, Toast.LENGTH_SHORT).show();
    }
}
