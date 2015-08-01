package com.theronin.budgettracker.pages.categories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.List;

public class CategoriesAdapter  extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private List<Category> categories;

    public CategoriesAdapter() {
        this.categories = new ArrayList<>();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__category, parent, false);

        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Category category = categories.get(position);
        viewHolder.nameTextView.setText(category.name);
        viewHolder.currencySymbolTotalView.setText(MoneyUtils.getCurrencySymbol());
        viewHolder.totalTextView.setText(MoneyUtils.convertToDollars(category.total));
        viewHolder.currencySymbolMonthlyView.setText(MoneyUtils.getCurrencySymbol());
        viewHolder.monthlyTextView.setText(MoneyUtils.convertToDollars(category.getMonthlyAverage()));
    }

    @Override
    public int getItemCount() {
        return categories.size();
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
            currencySymbolTotalView = (TextView) listItemView.findViewById(R.id.tv__currency_symbol_total);
            totalTextView = (TextView) listItemView.findViewById(R.id.tv__total_column);
            currencySymbolMonthlyView = (TextView) listItemView.findViewById(R.id.tv__currency_symbol_monthly);
            monthlyTextView = (TextView) listItemView.findViewById(R.id.tv__monthly_column);
        }
    }
}
