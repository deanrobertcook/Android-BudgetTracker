package org.theronin.budgettracker.pages.entries;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.utils.CursorRecyclerViewAdapter;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;

public class EntriesAdapter extends CursorRecyclerViewAdapter<EntriesAdapter.ViewHolder> {

    private final OnItemClickListener itemClickListener;

    public EntriesAdapter(Context context, Cursor cursor, OnItemClickListener itemClickListener) {
        super(context, cursor);
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__entry, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        final Entry boundEntry = Entry.fromCursor(cursor);

        viewHolder.currencySymbolTextView.setText(boundEntry.currencyEntered);
        viewHolder.amountTextView.setText(
                MoneyUtils.convertCentsToDisplayAmount(boundEntry.amount));
        viewHolder.categoryTextView.setText(boundEntry.categoryName);
        viewHolder.dateTextView.setText(DateUtils.getDisplayFormattedDate(boundEntry.utcDateEntered));

        viewHolder.listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClicked(boundEntry);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View listItemView;
        public final TextView currencySymbolTextView;
        public final TextView amountTextView;
        public final TextView categoryTextView;
        public final TextView dateTextView;

        public ViewHolder(View view) {
            super(view);
            this.listItemView = view;
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
