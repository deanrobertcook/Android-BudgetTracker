package org.theronin.budgettracker.pages.categories;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.utils.CursorRecyclerViewAdapter;
import org.theronin.budgettracker.utils.MoneyUtils;

public class CategoriesAdapter extends CursorRecyclerViewAdapter<CategoriesAdapter.ViewHolder>
        implements OnSharedPreferenceChangeListener {

    private static final String TAG = CategoriesAdapter.class.getName();

    private Currency homeCurrency;

    private Context context;

    private SharedPreferences defaultPreferences;

    public CategoriesAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultPreferences.registerOnSharedPreferenceChangeListener(this);
        homeCurrency = MoneyUtils.getHomeCurrency(context, defaultPreferences);
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

        viewHolder.currencySymbolTotalView.setText(homeCurrency.symbol);
        viewHolder.currencyCodeTotalView.setText(homeCurrency.code);

        viewHolder.totalTextView.setText(MoneyUtils.convertCentsToDisplayAmount(category.total));

        viewHolder.currencySymbolMonthlyView.setText(homeCurrency.symbol);
        viewHolder.currencyCodeMonthlyView.setText(homeCurrency.code);

        viewHolder.monthlyTextView.setText(MoneyUtils.convertCentsToDisplayAmount(category
                .getMonthlyAverage()));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        homeCurrency = MoneyUtils.getHomeCurrency(context, defaultPreferences);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView currencySymbolTotalView;
        public TextView currencyCodeTotalView;
        public TextView currencySymbolMonthlyView;
        public TextView currencyCodeMonthlyView;
        public TextView nameTextView;
        public TextView totalTextView;
        public TextView monthlyTextView;

        public ViewHolder(View listItemView) {
            super(listItemView);
            nameTextView = (TextView) listItemView.findViewById(R.id.tv__name_column);

            View currencyTotalView = listItemView.findViewById(R.id.ll__currency_total);
            currencySymbolTotalView = (TextView) currencyTotalView.findViewById(R.id
                    .tv__list_item__currency__symbol);
            currencyCodeTotalView = (TextView) currencyTotalView.findViewById(R.id
                    .tv__list_item__currency__code);

            totalTextView = (TextView) listItemView.findViewById(R.id.tv__total_column);

            View currencyMonthlyView = listItemView.findViewById(R.id.ll__currency_monthly);
            currencySymbolMonthlyView = (TextView) currencyMonthlyView.findViewById(R.id
                    .tv__list_item__currency__symbol);
            currencyCodeMonthlyView = (TextView) currencyMonthlyView.findViewById(R.id
                    .tv__list_item__currency__code);

            monthlyTextView = (TextView) listItemView.findViewById(R.id.tv__monthly_column);
        }
    }
}
