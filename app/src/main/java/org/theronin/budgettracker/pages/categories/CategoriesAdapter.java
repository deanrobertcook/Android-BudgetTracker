package org.theronin.budgettracker.pages.categories;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.utils.CursorRecyclerViewAdapter;
import org.theronin.budgettracker.utils.MoneyUtils;

public class CategoriesAdapter extends CursorRecyclerViewAdapter<CategoriesAdapter.ViewHolder> {

    private static final String TAG = CategoriesAdapter.class.getName();

    public CategoriesAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__category, parent, false);

        return new ViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        Category category = Category.fromCursor(cursor);

        viewHolder.nameTextView.setText(category.name);
        viewHolder.currencySymbolTotalView.setText(MoneyUtils.getCurrencySymbol());
        viewHolder.totalTextView.setText(
                MoneyUtils.convertCentsToDisplayAmount(category.total));
        viewHolder.currencySymbolMonthlyView.setText(MoneyUtils.getCurrencySymbol());
        viewHolder.monthlyTextView.setText(
                MoneyUtils.convertCentsToDisplayAmount(category.getMonthlyAverage()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView currencySymbolTotalView;
        public TextView currencySymbolMonthlyView;
        public TextView nameTextView;
        public TextView totalTextView;
        public TextView monthlyTextView;

        public ViewHolder(View listItemView) {
            super(listItemView);
            nameTextView = (TextView) listItemView.findViewById(R.id.tv__name_column);
            currencySymbolTotalView = (TextView) listItemView.findViewById(R.id
                    .tv__currency_symbol_total);
            totalTextView = (TextView) listItemView.findViewById(R.id.tv__total_column);
            currencySymbolMonthlyView = (TextView) listItemView.findViewById(R.id
                    .tv__currency_symbol_monthly);
            monthlyTextView = (TextView) listItemView.findViewById(R.id.tv__monthly_column);
        }
    }
}
