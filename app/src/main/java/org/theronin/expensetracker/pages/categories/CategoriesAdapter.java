package org.theronin.expensetracker.pages.categories;

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
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.comparators.CategoryTotalComparator;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.utils.DateUtils;
import org.theronin.expensetracker.utils.MoneyUtils;
import org.theronin.expensetracker.view.AmountDisplayLayout;

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
            throw new IllegalArgumentException("Categories cannot be null, specify an empty list instead");
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
    public void onBindViewHolder(ViewHolder vH, int position) {
        Category category = categories.get(position);

        vH.totalDisplay.setCurrency(homeCurrency);
        vH.totalDisplay.setAmount(category.getTotal());
        vH.nameTextView.setText(WordUtils.capitalize(category.name));

        vH.dateSinceTextView.setText(String.format(context.getString(R.string.date_since), DateUtils.getDisplayFormattedDate(category.utcFirstEntryDate)));
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

        public AmountDisplayLayout totalDisplay;
        public TextView nameTextView;
        public TextView dateSinceTextView;

        public ViewHolder(View listItemView) {
            super(listItemView);
            totalDisplay = (AmountDisplayLayout) listItemView.findViewById(R.id.amount_display_total);
            nameTextView = (TextView) listItemView.findViewById(R.id.tv__name_column);
            dateSinceTextView = (TextView) listItemView.findViewById(R.id.tv__date_since);

        }
    }
}
