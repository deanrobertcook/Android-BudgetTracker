package org.theronin.expensetracker.pages.entries.list;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang.WordUtils;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.utils.CurrencySettings;
import org.theronin.expensetracker.utils.DateUtils;
import org.theronin.expensetracker.utils.MoneyUtils;
import org.theronin.expensetracker.utils.MoneyUtils.EntryCondition;
import org.theronin.expensetracker.utils.MoneyUtils.EntrySum;
import org.theronin.expensetracker.view.AmountDisplayLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.theronin.expensetracker.utils.DateUtils.sameDay;
import static org.theronin.expensetracker.utils.MoneyUtils.calculateTotals;

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
            throw new IllegalArgumentException("Entries cannot be set to null, use an empty list instead");
        }
        this.entries = entries;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__entry, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder vH, int position) {
        final Entry entry = entries.get(position);

        vH.contentLayout.setTag(R.id.entry_id, entry.getId());

        String formattedDate = DateUtils.getDisplayFormattedDate(entry.utcDate);
        String formattedDayTotal = getFormattedDayTotal(entry.utcDate);
        displayDateBorder(vH, position, formattedDate, formattedDayTotal);

        vH.currentDisplay.setCurrency(entry.currency);
        vH.currentDisplay.setAmount(entry.amount);

        vH.categoryTextView.setText(WordUtils.capitalize(entry.category.name));

        if (!entry.currency.code.equals(currencySettings.getHomeCurrency().code)) {
            vH.homeDisplay.setVisibility(View.VISIBLE);
            vH.homeDisplay.setCurrency(currencySettings.getHomeCurrency());
            vH.homeDisplay.setAmount(entry.getHomeAmount());
        } else {
            vH.homeDisplay.setVisibility(View.INVISIBLE);
        }

        selectionManager.listenToItemView(vH.contentLayout);
    }

    private String getFormattedDayTotal(final long utcDate) {
        EntrySum entrySum = calculateTotals(entries, new EntryCondition() {
            @Override
            public boolean check(Entry entry) {
                return sameDay(entry.utcDate, utcDate);
            }
        });
        return (entrySum.missingEntries > 0 ? "~" : "") +
                currencySettings.getHomeCurrency().symbol +
                MoneyUtils.getDisplayCompact(context, entrySum.amount);
    }

    private void displayDateBorder(ViewHolder viewHolder, int position, String formattedDate, String formattedTotal) {
        if (shouldDisplayDateBorder(position)) {
            viewHolder.borderLayout.setVisibility(View.VISIBLE);

            viewHolder.borderDateTextView.setText(formattedDate);
            viewHolder.borderTotalTextView.setText(formattedTotal);
        } else {
            viewHolder.borderLayout.setVisibility(View.GONE);
        }
    }

    private boolean shouldDisplayDateBorder(int position) {
        if (position == 0) {
            return true;
        }

        Entry currentEntry = entries.get(position);
        Entry lastEntry = entries.get(position - 1);

        return !sameDay(currentEntry.utcDate, lastEntry.utcDate);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View borderLayout;
        public final TextView borderDateTextView;
        private final TextView borderTotalTextView;

        public final View contentLayout;
        public final AmountDisplayLayout currentDisplay;
        public final TextView categoryTextView;
        public final AmountDisplayLayout homeDisplay;

        public ViewHolder(View view) {
            super(view);

            borderLayout = itemView.findViewById(R.id.ll__border);
            borderDateTextView = (TextView) itemView.findViewById(R.id.tv__boundary_date);
            borderTotalTextView = (TextView) itemView.findViewById(R.id.tv__boundary_total);

            contentLayout = itemView.findViewById(R.id.ll__list_item__content_layout);
            currentDisplay = (AmountDisplayLayout) contentLayout.findViewById(R.id.amount_display_current);
            categoryTextView = (TextView) contentLayout.findViewById(R.id.tv__list_item__entry__category);
            homeDisplay = (AmountDisplayLayout) contentLayout.findViewById(R.id.amount_display_home);
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
        void onItemSelected(int count, String amount);
        void deleteSelection();
        void cancelSelection();
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
                listener.onItemSelected(selectedEntries.size(), getEntrySumAmountDisplay(selectedEntries));
            }
        }

        private String getEntrySumAmountDisplay(Collection<Entry> entries) {
            EntrySum entrySum = calculateTotals(entries, null);
            return (entrySum.missingEntries > 0 ? "~" : "") +
                    currencySettings.getHomeCurrency().symbol +
                    MoneyUtils.getDisplayCompact(context, entrySum.amount);
        }
        
        private Entry findEntryFromTag(View view) {
            long entryId = (long) view.getTag(R.id.entry_id);
            for (Entry entry : entries) {
                if (entry.getId() == entryId) {
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
