package com.theronin.budgettracker.pages.entries;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.theronin.budgettracker.R;

import java.util.ArrayList;
import java.util.List;

public class EntryListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__entry_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view__entry_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        EntriesAdapter adapter = new EntriesAdapter(dummyData());
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    private List<String[]> dummyData () {
        ArrayList<String[]> dummyData = new ArrayList<>();
        dummyData.add(new String[]{"3.00", "bananas", "2015/06/07"});
        dummyData.add(new String[]{"1.00", "apples", "2015/06/06"});
        dummyData.add(new String[]{"2.00", "cashews", "2015/06/05"});
        dummyData.add(new String[]{"5.00", "bananas", "2015/06/04"});
        dummyData.add(new String[]{"2.00", "apples", "2015/06/03"});
        dummyData.add(new String[]{"1.00", "olives", "2015/06/02"});
        dummyData.add(new String[]{"2.00", "peaches", "2015/06/01"});
        return dummyData;
    }
}
