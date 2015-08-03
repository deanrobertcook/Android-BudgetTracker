package com.theronin.budgettracker.pages.categories;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.utils.CursorRecyclerViewAdapter;
import com.theronin.budgettracker.utils.MoneyUtils;

public class CategoriesAdapter extends CursorRecyclerViewAdapter<CategoriesAdapter.ViewHolder> {

    public CategoriesAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__category, parent, false);

        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
//        Category category = categories.get(position);
        Category category = null;
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
