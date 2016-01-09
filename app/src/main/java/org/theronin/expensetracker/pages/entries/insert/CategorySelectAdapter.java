package org.theronin.expensetracker.pages.entries.insert;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang.WordUtils;
import org.theronin.expensetracker.BuildConfig;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.comparators.CategoryAlphabeticalComparator;
import org.theronin.expensetracker.comparators.CategoryFrequencyComparator;
import org.theronin.expensetracker.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategorySelectAdapter extends RecyclerView.Adapter<CategorySelectAdapter.ViewHolder> {

    private final int MIN_CATEGORIES_FOR_FREQUENCY_SORT = 20;

    private List<Comparator<Category>> comparators;
    private int[] sortSizes;

    private final int VIEW_TYPE_NORMAL = 0;
    private final int VIEW_TYPE_WITH_BORDER = 1;

    private List<Category> categories;
    private CategorySelectedListener listener;

    public CategorySelectAdapter(CategorySelectedListener listener) {
        categories = new ArrayList<>();
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        this.categories = categories;

        initialiseSortingBlocks();
        sortCategories(comparators, sortSizes);
    }

    private void initialiseSortingBlocks() {
        comparators = new ArrayList<>();
        if (categories.size() > MIN_CATEGORIES_FOR_FREQUENCY_SORT) {
            comparators.add(new CategoryFrequencyComparator());
            comparators.add(new CategoryAlphabeticalComparator());

            sortSizes = new int[]{
                    (int) (categories.size() * 0.2),
                    categories.size()};
        } else {
            comparators.add(new CategoryAlphabeticalComparator());
            sortSizes = new int[]{categories.size()};
        }
    }

    /**
     * This method takes in a list of comparators, and a list of sizes, where each size specifies
     * how many items in the backing list that it's corresponding comparator will sort. For each
     * different comparator, a sub-list is created from the complete backing list, and then the
     * sub-lists for all of the different comparators are combined in order to provide us with
     * a multiple-sorted collection of items.
     * <p/>
     * Note, each sub list does NOT remove elements from the backing collection, so that there
     * can be duplicates across different sub-lists.
     *
     * @param comparators the comparators to be applied to the backing list
     * @param sizes       the number of elements in the backing list that each comparator should sort,
     *                    where the size value at index i corresponds to some comparator in comparators
     *                    at index i.
     */
    private void sortCategories(List<Comparator<Category>> comparators, int[] sizes) {
        if (comparators.size() != sizes.length) {
            throw new RuntimeException("The number of comparators and sizes of sublists must match");
        }
        if (categories.size() == 0) {
            return;
        }
        List<Category> finalSortedList = new ArrayList<>();

        for (int i = 0; i < comparators.size(); i++) {
            finalSortedList.addAll(generateSortedSublist(comparators.get(i), sizes[i]));
        }
        categories = finalSortedList;
    }

    private List<Category> generateSortedSublist(Comparator<Category> comparator, int size) {
        Collections.sort(categories, comparator);
        return new ArrayList<>(categories.subList(0, size));
    }

    @Override
    public int getItemViewType(int position) {
        int borderIndex = 0;
        for (int sortSize : sortSizes) {
            borderIndex += sortSize;
            if (position == borderIndex - 1 && position != categories.size() - 1) {
                return VIEW_TYPE_WITH_BORDER;
            }
        }
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item__category_select, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Category category = categories.get(position);

        switch (getItemViewType(position)) {
            case VIEW_TYPE_NORMAL:
                break;
            case VIEW_TYPE_WITH_BORDER:
                holder.separator.setVisibility(View.VISIBLE);
                break;
        }

        String text = WordUtils.capitalize(category.name);
        if (BuildConfig.DEBUG) {
            text += String.format(" (%d)", category.frequency);
        }
        ((TextView) holder.categoryNameTextView).setText(text);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCategorySelected(category.name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View separator;
        public final View categoryNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            separator = itemView.findViewById(R.id.v__separator);
            categoryNameTextView = itemView.findViewById(R.id.tv__category_name);
        }
    }

    public interface CategorySelectedListener {
        void onCategorySelected(String categoryName);
    }
}
