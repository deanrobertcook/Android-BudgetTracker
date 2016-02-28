package org.theronin.expensetracker.pages.entries.list;

import android.support.v7.widget.RecyclerView;

import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.utils.DateUtils;

public class MonthChangedListener extends RecyclerView.OnScrollListener {

    private final Callback callback;

    interface Callback {
        /**
         * Returns the last entry of the month at the instant that that Entry becomes the top-most visible
         * item in the list, when the entry before it was made in a different month.
         *
         * @param lastEntry
         */
        void onMonthChanged(Entry lastEntry);
    }

    public MonthChangedListener(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        EntriesAdapter adapter = (EntriesAdapter) recyclerView.getAdapter();
        int position = recyclerView.getChildAdapterPosition(recyclerView.findChildViewUnder(0, 0));

        Entry topVisibleEntry = adapter.getEntryAt(position);
        Entry lastEntry = dy > 0 ? adapter.getEntryAt(position - 1) : adapter.getEntryAt(position + 1);

        if (topVisibleEntry == null) {
            callback.onMonthChanged(null);
            return;
        }

        if (lastEntry == null || !DateUtils.sameMonth(topVisibleEntry.utcDate, lastEntry.utcDate)) {
            callback.onMonthChanged(topVisibleEntry);
        }
    }
}
