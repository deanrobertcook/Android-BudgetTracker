package org.theronin.budgettracker.pages.categories;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang.WordUtils;
import org.theronin.budgettracker.R;
import org.theronin.budgettracker.comparators.CategoryTotalComparator;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder>
        implements OnSharedPreferenceChangeListener {

    private static final String TAG = CategoriesAdapter.class.getName();

    private List<Category> categories = new ArrayList<>();

    private Currency homeCurrency;

    private Context context;

    private SharedPreferences defaultPreferences;

    public CategoriesAdapter(Context context) {
        this.context = context;
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultPreferences.registerOnSharedPreferenceChangeListener(this);
        homeCurrency = MoneyUtils.getHomeCurrency(context, defaultPreferences);
    }

    public void setCategories(List<Category> categories) {
        if (categories == null) {
            throw new IllegalArgumentException("Categories cannot be null, specify an empty list " +
                    "instead");
        }
        this.categories = categories;
        Collections.sort(this.categories, new CategoryTotalComparator());
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__category, parent, false);

        return new ViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Category category = categories.get(position);

        viewHolder.nameTextView.setText(WordUtils.capitalize(category.name));

        viewHolder.currencySymbolTotalView.setText(homeCurrency.symbol);
        viewHolder.currencyCodeTotalView.setText(homeCurrency.code);

        viewHolder.totalTextView.setText(createAmountDisplay(category.getMissingEntries(),
                category.getTotal()));

        viewHolder.dateSinceTextView.setText(
                String.format(context.getString(R.string.date_since),
                        DateUtils.getDisplayFormattedDate(category.utcFirstEntryDate)));
    }

    private String createAmountDisplay(int missingEntries, long amount) {
        StringBuilder sb = new StringBuilder();
        if (missingEntries > 0) {
            sb.append("~");
        }
        sb.append(MoneyUtils.getDisplayCompact(amount));
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        homeCurrency = MoneyUtils.getHomeCurrency(context, defaultPreferences);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView currencySymbolTotalView;
        public TextView currencyCodeTotalView;
        public TextView nameTextView;
        public TextView totalTextView;
        public TextView dateSinceTextView;

        public ViewHolder(View listItemView) {
            super(listItemView);
            nameTextView = (TextView) listItemView.findViewById(R.id.tv__name_column);

            View currencyTotalView = listItemView.findViewById(R.id.amount_display_total);
            currencySymbolTotalView = (TextView) currencyTotalView.findViewById(R.id
                    .tv__widget_amount_display__current_currency__symbol);
            currencyCodeTotalView = (TextView) currencyTotalView.findViewById(R.id
                    .tv__widget_amount_display__current_currency__code);
            totalTextView = (TextView) currencyTotalView.findViewById(R.id
                    .tv__widget_amount_display__current_currency__amount);

            dateSinceTextView = (TextView) listItemView.findViewById(R.id.tv__date_since);

        }
    }
}
