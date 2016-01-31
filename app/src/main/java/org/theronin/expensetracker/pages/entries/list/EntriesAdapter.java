package org.theronin.expensetracker.pages.entries.list;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import org.apache.commons.lang.WordUtils;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.utils.DateUtils;
import org.theronin.expensetracker.utils.MoneyUtils;
import org.theronin.expensetracker.utils.MoneyUtils.EntryCondition;
import org.theronin.expensetracker.utils.MoneyUtils.EntrySum;
import org.theronin.expensetracker.utils.Prefs;
import org.theronin.expensetracker.view.AmountView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.theronin.expensetracker.utils.DateUtils.sameDay;
import static org.theronin.expensetracker.utils.DateUtils.sameMonth;
import static org.theronin.expensetracker.utils.MoneyUtils.calculateTotals;
import static org.theronin.expensetracker.utils.Prefs.getHomeCurrency;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {

    private Context context;

    private SelectionManager selectionManager;

    private List<Entry> entries = new ArrayList<>();

    public EntriesAdapter(Context context, SelectionListener selectionListener) {
        this.context = context;
        this.selectionManager = new SelectionManager(selectionListener);
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

        if (shouldDisplaySummaryRow(position)) {
            vH.displaySummaryRow(DateUtils.getMonth(entry.utcDate), getMonthSummary(position), getHomeCurrency(context));
        } else {
            vH.hideInflatedSummaryRow();
        }

        if (shouldDisplayDateBorder(position)) {
            vH.displayDateBorder(DateUtils.getDisplayFormattedDate(entry.utcDate), getFormattedDayTotal(entry.utcDate));
        } else {
            vH.hideInflatedDateBorder();
        }

        setCurrentAmount(vH, entry);

        vH.categoryTextView.setText(WordUtils.capitalize(entry.category.name));

        setHomeAmount(vH, entry);

        selectionManager.listenToItemView(vH.contentLayout);
    }

    private boolean shouldDisplaySummaryRow(int position) {
        if (position == 0) {
            return true;
        }
        return !sameMonth(entries.get(position).utcDate, entries.get(position - 1).utcDate);
    }

    private long getMonthSummary(int position) {
        Entry startEntry = entries.get(position);
        long total = 0;
        int i = position;
        while (i < entries.size() && sameMonth(startEntry.utcDate, entries.get(i).utcDate)) {
            total += entries.get(i).getHomeAmount();
            i++;
        }
        return total;
    }

    private void setCurrentAmount(ViewHolder vH, Entry entry) {
        vH.currentDisplay.setCurrency(entry.currency);
        vH.currentDisplay.setAmount(entry.amount);
    }

    private void setHomeAmount(ViewHolder vH, Entry entry) {
        if (!entry.currency.code.equals(getHomeCurrency(context).code)) {
            vH.homeDisplay.setVisibility(View.VISIBLE);
            vH.homeDisplay.setCurrency(getHomeCurrency(context));
            vH.homeDisplay.setAmount(entry.getHomeAmount());
        } else {
            vH.homeDisplay.setVisibility(View.INVISIBLE);
        }
    }

    private String getFormattedDayTotal(final long utcDate) {
        EntrySum entrySum = calculateTotals(entries, new EntryCondition() {
            @Override
            public boolean check(Entry entry) {
                return sameDay(entry.utcDate, utcDate);
            }
        });
        return (entrySum.missingEntries > 0 ? "~" : "") +
                getHomeCurrency(context).symbol +
                MoneyUtils.getDisplayCompact(context, entrySum.amount);
    }

    private boolean shouldDisplayDateBorder(int position) {
        if (position == 0) {
            return true;
        }
        return !sameDay(entries.get(position).utcDate, entries.get(position - 1).utcDate);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final ViewStub summaryRowStub;

        private View summaryRowLayout;
        private TextView summaryTitleTextView;
        private AmountView summaryDisplay;
        private View limitSeparator;
        private AmountView summaryLimit;

        public final ViewStub borderStub;
        private View borderLayout;
        private TextView borderDateTextView;
        private TextView borderTotalTextView;

        public final View contentLayout;
        public final AmountView currentDisplay;
        public final TextView categoryTextView;
        public final AmountView homeDisplay;

        public ViewHolder(View view) {
            super(view);

            summaryRowStub = (ViewStub) itemView.findViewById(R.id.stub__summary_row_layout);

            borderStub = (ViewStub) itemView.findViewById(R.id.stub__date_border_layout);

            contentLayout = itemView.findViewById(R.id.ll__list_item__content_layout);
            currentDisplay = (AmountView) contentLayout.findViewById(R.id.amount_display_current);
            categoryTextView = (TextView) contentLayout.findViewById(R.id.tv__list_item__entry__category);
            homeDisplay = (AmountView) contentLayout.findViewById(R.id.amount_display_home);
        }

        public void displaySummaryRow(String month, long monthTotal, Currency currency) {
            inflateSummaryRow();
            summaryRowLayout.setVisibility(View.VISIBLE);
            summaryTitleTextView.setText(month + ": ");
            summaryDisplay.setAmount(monthTotal, false);
            summaryDisplay.setCurrency(currency);

            long limit = Prefs.getMonthlyLimit(EntriesAdapter.this.context);
            if (limit == 0) {
                limitSeparator.setVisibility(View.GONE);
                summaryLimit.setVisibility(View.GONE);
            } else {
                summaryLimit.setCurrency(currency);
                summaryLimit.setAmount(limit, false);
            }
        }

        private void inflateSummaryRow() {
            if (summaryRowLayout == null) {
                summaryRowLayout = summaryRowStub.inflate();
                summaryTitleTextView = (TextView) summaryRowLayout.findViewById(R.id.tv__summary_row_title);
                summaryDisplay = (AmountView) summaryRowLayout.findViewById(R.id.adl__summary_row_amount);
                limitSeparator = summaryRowLayout.findViewById(R.id.separator);
                summaryLimit = (AmountView) summaryRowLayout.findViewById(R.id.adl__summary_monthly_limit);
            }
        }

        public void hideInflatedSummaryRow() {
            if (summaryRowLayout == null) {
                return;
            }
            summaryRowLayout.setVisibility(View.GONE);
        }

        public void displayDateBorder(String formattedDate, String formattedTotal) {
            inflateDateBorder();
            borderLayout.setVisibility(View.VISIBLE);
            borderDateTextView.setText(formattedDate);
            borderTotalTextView.setText(formattedTotal);
        }

        private void inflateDateBorder() {
            if (borderLayout == null) {
                borderLayout = borderStub.inflate();
                borderDateTextView = (TextView) borderLayout.findViewById(R.id.tv__boundary_date);
                borderTotalTextView = (TextView) borderLayout.findViewById(R.id.tv__boundary_total);
            }
        }

        public void hideInflatedDateBorder() {
            if (borderLayout == null) {
                return;
            }
            borderLayout.setVisibility(View.GONE);
        }
    }

    public List<Entry> getSelection() {
        return new ArrayList<>(selectionManager.selectedEntries);
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
                    getHomeCurrency(context).symbol +
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
