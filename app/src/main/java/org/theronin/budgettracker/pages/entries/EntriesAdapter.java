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

        viewHolder.currentCurrencySymbolTextView.setText(boundEntry.currency.symbol);
        viewHolder.currentCurrencyCodeTextView.setText(boundEntry.currency.code);

        viewHolder.currentCurrencyAmount.setText(
                MoneyUtils.convertCentsToDisplayAmount(boundEntry.amount));
//        viewHolder.homeCurrencyAmount.setText("todo");

        viewHolder.categoryTextView.setText(boundEntry.category.name);
        viewHolder.dateTextView.setText(DateUtils.getDisplayFormattedDate(boundEntry.utcDate));

        viewHolder.moreOptionsView.setOnClickListener(new View.OnClickListener() {
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

        public final TextView currentCurrencySymbolTextView;
        public final TextView currentCurrencyCodeTextView;

        public final TextView currentCurrencyAmount;
        public final TextView homeCurrencyAmount;

        public final TextView categoryTextView;
        public final TextView dateTextView;

        public final View moreOptionsView;

        public ViewHolder(View view) {
            super(view);;

            currentCurrencySymbolTextView = (TextView) itemView.findViewById(R.id.tv__list_item__entry__current_currency__symbol);
            currentCurrencyCodeTextView = (TextView) itemView.findViewById(R.id.tv__list_item__entry__current_currency__code);

            currentCurrencyAmount = (TextView) itemView.findViewById(R.id.tv__list_item__entry__current_currency__amount);
            homeCurrencyAmount = (TextView) itemView.findViewById(R.id.tv__list_item__entry__home_currency__amount);

            categoryTextView = (TextView) itemView.findViewById(R.id.tv__list_item__entry__category);
            dateTextView = (TextView) itemView.findViewById(R.id.tv__list_item__entry__date);

            moreOptionsView = itemView.findViewById(R.id.iv__list_item__entry__more_options);
        }
    }

    interface OnItemClickListener {
        void onItemClicked(Entry entrySelected);
    }
}
