package org.theronin.expensetracker.pages.entries;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.view.AmountDisplayLayout;

public class EntryInputAdapter extends RecyclerView.Adapter<EntryInputAdapter.ViewHolder> {

    private CategorySpinnerAdapter spinnerAdapter;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public final AmountDisplayLayout amountLayout;

        public final Spinner categorySpinner;
        public Category lastSelectedCategory;

        public ViewHolder(View inputView) {
            super(inputView);

            amountLayout = (AmountDisplayLayout) inputView.findViewById(R.id.amount_edit_layout);
            categorySpinner = (Spinner) inputView.findViewById(R.id.spn__add_entry_category);
            categorySpinner.setAdapter(spinnerAdapter);
        }
    }
}
