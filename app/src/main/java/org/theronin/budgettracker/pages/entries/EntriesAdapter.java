package org.theronin.budgettracker.pages.entries;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.utils.CurrencySettings;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;
import org.theronin.budgettracker.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> implements
        CurrencySettings.Listener {

    private Context context;

    private CurrencySettings currencySettings;

    private final OnItemClickListener itemClickListener;
    private List<Entry> entries = new ArrayList<>();

    public EntriesAdapter(Context context, OnItemClickListener itemClickListener) {
        this.context = context;
        this.currencySettings = new CurrencySettings(context, this);
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

        if (position == 0) {
            ViewUtils.addPadding(context, viewHolder.itemView, 0, 8, 0, 0);
        }

        viewHolder.currentCurrencySymbolTextView.setText(boundEntry.currency.symbol);
        viewHolder.currentCurrencyCodeTextView.setText(boundEntry.currency.code);

        viewHolder.currentCurrencyAmount.setText(
                MoneyUtils.convertCentsToDisplayAmount(boundEntry.amount));

        if (boundEntry.getDirectExchangeRate() > 0
                && !boundEntry.currency.code.equals(currencySettings.getHomeCurrency().code)) {
            viewHolder.homeCurrencyAmount.setVisibility(View.VISIBLE);

            long homeAmount =
                    (long) ((double) boundEntry.amount * boundEntry.getDirectExchangeRate());

            String homeDisplayString = currencySettings.getHomeCurrency().symbol +
                    MoneyUtils.convertCentsToDisplayAmount(homeAmount);

            viewHolder.homeCurrencyAmount.setText(homeDisplayString);
        } else {
            viewHolder.homeCurrencyAmount.setVisibility(View.GONE);
        }

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

    @Override
    public void onHomeCurrencyChanged(Currency homeCurrency) {
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
