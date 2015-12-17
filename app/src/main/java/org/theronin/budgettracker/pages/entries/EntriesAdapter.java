package org.theronin.budgettracker.pages.entries;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang.WordUtils;
import org.theronin.budgettracker.R;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.utils.CurrencySettings;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;
import org.theronin.budgettracker.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {

    private Context context;

    private CurrencySettings currencySettings;

    private SelectionManager selectionManager;

    private List<Entry> entries = new ArrayList<>();

    public EntriesAdapter(Context context, SelectionListener selectionListener) {
        this.context = context;
        this.selectionManager = new SelectionManager(selectionListener);
        this.currencySettings = new CurrencySettings(context, null);
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

        viewHolder.itemView.setTag(R.id.entry_id, boundEntry.id);
        viewHolder.currentCurrencySymbolTextView.setText(boundEntry.currency.symbol);
        viewHolder.currentCurrencyCodeTextView.setText(boundEntry.currency.code);

        viewHolder.currentCurrencyAmount.setText(
                MoneyUtils.convertCentsToDisplayAmount(boundEntry.amount));

        if (!boundEntry.currency.code.equals(currencySettings.getHomeCurrency().code)) {
            viewHolder.homeCurrencyAmount.setVisibility(View.VISIBLE);

            StringBuilder sb = new StringBuilder();
            sb.append(currencySettings.getHomeCurrency().symbol);

            if (boundEntry.getDirectExchangeRate() > 0) {
                long homeAmount = Math.round(
                        (double) boundEntry.amount * boundEntry.getDirectExchangeRate());
                sb.append(MoneyUtils.convertCentsToDisplayAmount(homeAmount));
            } else {
                sb.append("??.??");
            }
            viewHolder.homeCurrencyAmount.setText(sb.toString());


        } else {
            viewHolder.homeCurrencyAmount.setVisibility(View.GONE);
        }
        viewHolder.categoryTextView.setText(WordUtils.capitalize(boundEntry.category.name));
        viewHolder.dateTextView.setText(DateUtils.getDisplayFormattedDate(boundEntry.utcDate));

        selectionManager.listenToItemView(viewHolder.itemView);
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

        public ViewHolder(View view) {
            super(view);;

            currentCurrencySymbolTextView = (TextView) itemView.findViewById(R.id.tv__widget_amount_display__current_currency__symbol);
            currentCurrencyCodeTextView = (TextView) itemView.findViewById(R.id.tv__widget_amount_display__current_currency__code);

            currentCurrencyAmount = (TextView) itemView.findViewById(R.id.tv__widget_amount_display__current_currency__amount);
            homeCurrencyAmount = (TextView) itemView.findViewById(R.id.tv__widget_amount_display__home_currency__amount);

            categoryTextView = (TextView) itemView.findViewById(R.id.tv__list_item__entry__category);
            dateTextView = (TextView) itemView.findViewById(R.id.tv__list_item__entry__date);
        }
    }

    public Set<Entry> getSelection() {
        return selectionManager.selectedEntries;
    }

    public void exitSelectMode() {
        selectionManager.exitSelectMode();
    }

    public interface SelectionListener {
        void onEnterSelectMode();
        void onExitSelectMode();
        void onItemSelected(int count);
        void onDeleteSelectionClicked();
        void onCancelSelectionClicked();
    }

    private class SelectionManager implements View.OnClickListener, View.OnLongClickListener {

        private boolean selectMode;

        /**
         * Keep a reference to (eventually) all of the recycler views item Views, so we can wipe
         * the selected state of them when we exit selectMode
        **/
        private Set<View> itemViews;

        private Set<Entry> selectedEntries;
        private SelectionListener listener;

        public SelectionManager(SelectionListener listener) {
            itemViews = new HashSet<>();
            selectedEntries = new HashSet<>();
            this.listener = listener;
        }

        public void listenToItemView(View itemView) {
            itemViews.add(itemView);

            if (selectMode) {
                //save a redraw if not in select mode
                resetViewSelectionState(itemView);
            }

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void resetViewSelectionState(View itemView) {
            Entry entry = findEntryFromTag(itemView);
            if (selectedEntries.contains(entry)) {
                setViewSelected(itemView);
            } else {
                setViewUnselected(itemView);
            }
        }

        private void setViewSelected(View itemView) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.primary_light));
        }

        private void setViewUnselected(View itemView) {
            TypedArray a = context.getTheme().obtainStyledAttributes(R.style.AppTheme, new int[]{R.attr.selectableItemBackground});
            int attributeResourceId = a.getResourceId(0, 0);
            a.recycle();
            itemView.setBackground(context.getResources().getDrawable(attributeResourceId, context.getTheme()));
        }

        public void exitSelectMode() {
            selectMode = false;
            for (View itemView : itemViews) {
                setViewUnselected(itemView);
            }
            selectedEntries = new HashSet<>();
            listener.onExitSelectMode();
        }

        @Override
        public void onClick(View itemView) {
            if (selectMode) {
                Entry entry = findEntryFromTag(itemView);
                if (selectedEntries.contains(entry)) {
                    setViewUnselected(itemView);
                    selectedEntries.remove(entry);
                    if (selectedEntries.isEmpty()) {
                        exitSelectMode();
                    }
                } else {
                    setViewSelected(itemView);
                    selectedEntries.add(entry);
                }
                listener.onItemSelected(selectedEntries.size());
            }
        }
        
        private Entry findEntryFromTag(View view) {
            long entryId = (long) view.getTag(R.id.entry_id);
            for (Entry entry : entries) {
                if (entry.id == entryId) {
                    return entry;
                }
            }
            throw new IllegalStateException("The Entry should exist within the adapters data set");
        }

        @Override
        public boolean onLongClick(View v) {
            if (!selectMode) {
                selectMode = true;
            }
            listener.onEnterSelectMode();
            v.callOnClick();
            return true;
        }
    }
}
