package com.theronin.budgettracker.pages.entries;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.Entry;
import com.theronin.budgettracker.utils.DateUtils;
import com.theronin.budgettracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {

    private final OnItemClickListener itemClickListener;
    private List<Entry> entries;

    public EntriesAdapter(OnItemClickListener itemClickListener) {
        this(itemClickListener, new ArrayList<Entry>());
    }

    public EntriesAdapter(OnItemClickListener itemClickListener, List<Entry> entries) {
        this.itemClickListener = itemClickListener;
        this.entries = entries;
        sortEntriesByDate();
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
        sortEntriesByDate();
    }

    private void sortEntriesByDate() {
        Collections.sort(entries, new Comparator<Entry>() {
            @Override
            public int compare(Entry lhs, Entry rhs) {
                return rhs.dateEntered.compareTo(lhs.dateEntered);
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__entry, parent, false);
        final ViewHolder viewHolder = new ViewHolder(listItemView);

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Entry entrySelected = new Entry(
                        viewHolder.categoryTextView.getText().toString(),
                        viewHolder.dateTextView.getText().toString(),
                        MoneyUtils.convertToCents(viewHolder.amountTextView.getText().toString())
                );
                itemClickListener.onItemClicked(entrySelected);
            }
        });


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.currencySymbolTextView.setText(MoneyUtils.getCurrencySymbol());
        viewHolder.amountTextView.setText(MoneyUtils.convertToDollars(entries.get(position).amount));
        viewHolder.categoryTextView.setText(entries.get(position).categoryName);
        viewHolder.dateTextView.setText(DateUtils.getDisplayFormattedDate(entries.get(position).dateEntered));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView currencySymbolTextView;
        public final TextView amountTextView;
        public final TextView categoryTextView;
        public final TextView dateTextView;

        public ViewHolder(View listItemView) {
            super(listItemView);
            currencySymbolTextView = (TextView) listItemView.findViewById(R.id.tv__currency_symbol);
            amountTextView = (TextView) listItemView.findViewById(R.id.tv__cost_column);
            categoryTextView = (TextView) listItemView.findViewById(R.id.tv__category_column);
            dateTextView = (TextView) listItemView.findViewById(R.id.tv__date_column);
        }
    }

    interface OnItemClickListener {
        void onItemClicked(Entry entrySelected);
    }
}
