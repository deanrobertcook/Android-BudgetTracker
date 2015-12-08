package org.theronin.budgettracker.pages.categories;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.BudgetContract.CategoryView;
import org.theronin.budgettracker.data.loader.DataLoader;
import org.theronin.budgettracker.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment implements
        LoaderCallbacks<List<Category>> {

    private static final int CATEGORY_LOADER_ID = 0;
    private static final String SORT_ORDER =
            CategoryView.COL_TOTAL_AMOUNT + " DESC, " + CategoryView.COL_CATEGORY_NAME + " ASC";
    private CategoriesAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__category_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id
                .recycler_view__category_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CategoriesAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public Loader<List<Category>> onCreateLoader(int id, Bundle args) {
        return new DataLoader.CategoryLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Category>> loader, List<Category> data) {
        adapter.setCategories(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Category>> loader) {
        adapter.setCategories(new ArrayList<Category>());
    }
}
