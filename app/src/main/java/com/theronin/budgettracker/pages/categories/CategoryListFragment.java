package com.theronin.budgettracker.pages.categories;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.CategoryStore;

import java.util.List;

public class CategoryListFragment extends Fragment implements CategoryStore.Observer {

    private CategoriesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle 
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__category_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view__category_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CategoriesAdapter();
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onCategoriesLoaded(List<Category> categories) {
        if (adapter != null) {
            adapter.setCategories(categories);
        }
    }
}
