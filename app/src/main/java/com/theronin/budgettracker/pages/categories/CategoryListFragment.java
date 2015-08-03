package com.theronin.budgettracker.pages.categories;

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

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetContract;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.CategoryStore;

import java.util.List;

public class CategoryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        CategoryStore.Observer {

    private CategoriesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__category_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id
                .recycler_view__category_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CategoriesAdapter(getActivity(), null);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onCategoriesLoaded(List<Category> categories) {
        if (adapter != null) {

        }
    }

    //////////////////////////////
    // Loader Callbacks
    //////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                BudgetContract.CategoriesTable.CONTENT_URI,
                Category.projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}
