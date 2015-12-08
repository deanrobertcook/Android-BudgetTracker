package org.theronin.budgettracker.pages.entries;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {

    private final OnItemClickListener itemClickListener;
    private List<Entry> entries = new ArrayList<>();

    public EntriesAdapter(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setEntries(List<Entry> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("Entries cannot be set to null, use an empty list " +
                    "instead");
        }
        this.entries = entries;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__entry, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Entry boundEntry = entries.get(position);

        viewHolder.currencySymbolTextView.setText(boundEntry.currency.symbol);
        viewHolder.currencyCodeTextView.setText(boundEntry.currency.code);
        viewHolder.amountTextView.setText(
                MoneyUtils.convertCentsToDisplayAmount(boundEntry.amount));
        viewHolder.categoryTextView.setText(boundEntry.category.name);
        viewHolder.dateTextView.setText(DateUtils.getDisplayFormattedDate(boundEntry.utcDateEntered));

        viewHolder.listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClicked(boundEntry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View listItemView;
        public final TextView currencySymbolTextView;
        public final TextView currencyCodeTextView;
        public final TextView amountTextView;
        public final TextView categoryTextView;
        public final TextView dateTextView;

        public ViewHolder(View view) {
            super(view);
            this.listItemView = view;
            View currencyDisplayView = listItemView.findViewById(R.id.ll__currency_layout);
            currencySymbolTextView = (TextView) currencyDisplayView.findViewById(R.id.tv__list_item__currency__symbol);
            currencyCodeTextView = (TextView) currencyDisplayView.findViewById(R.id.tv__list_item__currency__code);
            amountTextView = (TextView) listItemView.findViewById(R.id.tv__amount_column);
            categoryTextView = (TextView) listItemView.findViewById(R.id.tv__category_column);
            dateTextView = (TextView) listItemView.findViewById(R.id.tv__date_column);
        }
    }

    interface OnItemClickListener {
        void onItemClicked(Entry entrySelected);
    }
}
