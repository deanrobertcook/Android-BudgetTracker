package com.theronin.budgettracker.pages.categories;

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

public class CategoryListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle 
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__category_list, container, false);


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view__category_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        CategoriesAdapter adapter = new CategoriesAdapter(dummyData());
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    private List<String[]> dummyData () {
        ArrayList<String[]> dummyData = new ArrayList<>();
        dummyData.add(new String[]{"bananas", "200.00", "30.00"});
        dummyData.add(new String[]{"apples", "200.00", "30.00"});
        dummyData.add(new String[]{"olives", "200.00", "30.00"});
        dummyData.add(new String[]{"beer", "200.00", "30.00"});
        dummyData.add(new String[]{"bread", "200.00", "30.00"});
        dummyData.add(new String[]{"milk", "200.00", "30.00"});
        dummyData.add(new String[]{"cashews", "200.00", "30.00"});
        return dummyData;
    }
}
