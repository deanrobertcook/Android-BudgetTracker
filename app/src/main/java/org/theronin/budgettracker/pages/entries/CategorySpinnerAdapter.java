package org.theronin.budgettracker.pages.entries;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.comparators.CategoryAlphabeticalComparator;
import org.theronin.budgettracker.comparators.CategoryFrequencyComparator;
import org.theronin.budgettracker.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class CategorySpinnerAdapter extends ArrayAdapter<String> {

    private List<Category> categories;

    private List<Comparator<Category>> comparators;
    private int[] sortSizes;

    private final int FREQUENCY_SORT_SIZE = 10;

    private final int VIEW_TYPE_NORMAL = 0;
    private final int VIEW_TYPE_WITH_BORDER = 1;

    public CategorySpinnerAdapter(Context context) {
        super(context, R.layout.list_item__add_entry__category_spinner);
    }

    public void addAll(List<Category> categories) {
        this.categories = categories;

        initialiseSortingBlocks();
        sortCategories(comparators, sortSizes);

        super.clear();
        super.addAll(getCategoryNames());
        super.notifyDataSetChanged();
    }

    private void initialiseSortingBlocks() {
        comparators = new ArrayList<>();
        if (categories.size() > FREQUENCY_SORT_SIZE * 2) {
            comparators.add(new CategoryFrequencyComparator());
            comparators.add(new CategoryAlphabeticalComparator());

            sortSizes = new int[]{
                    FREQUENCY_SORT_SIZE,
                    categories.size()};
        } else {
            comparators.add(new CategoryAlphabeticalComparator());
            sortSizes = new int[]{categories.size()};
        }
    }

    public Category getCategory(int position) {
        return categories.get(position);
    }

    public int getPosition(Category category) {
        return super.getPosition(category.name);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        int borderIndex = 0;
        for (int i = 0; i < sortSizes.length; i++) {
            borderIndex += sortSizes[i];
            if (position == borderIndex - 1
                    && position != categories.size() - 1) {
                return VIEW_TYPE_WITH_BORDER;
            }
        }
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View itemView = null;
        String categoryName = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        switch (getItemViewType(position)) {
            case VIEW_TYPE_NORMAL:
                itemView = inflater.inflate(
                        R.layout.list_item__add_entry__category_spinner,
                        parent, false);
                ((TextView) itemView).setText(categoryName);
                break;
            case VIEW_TYPE_WITH_BORDER:
                itemView = inflater.inflate(
                        R.layout.list_item__add_entry__category_spinner_with_border,
                        parent, false);
                ((TextView) itemView.findViewById(R.id.tv__add_category__spinner_drop_down)).setText(categoryName);
                break;
        }
        return itemView;
    }

    /**
     * This method takes in a list of comparators, and a list of sizes, where each size specifies
     * how many items in the backing list that it's corresponding comparator will sort. For each
     * different comparator, a sub-list is created from the complete backing list, and then the
     * sub-lists for all of the different comparators are combined in order to provide us with
     * a multiple-sorted collection of items.
     *
     * Note, each sub list does NOT remove elements from the backing collection, so that there
     * can be duplicates across different sub-lists.
     * @param comparators the comparators to be applied to the backing list
     * @param sizes the number of elements in the backing list that each comparator should sort,
     *              where the size value at index i corresponds to some comparator in comparators
     *              at index i.
     */
    private void sortCategories(List<Comparator<Category>> comparators, int[] sizes) {
        if (comparators.size() != sizes.length) {
            throw new RuntimeException("The number of comparators and sizes of sublists must " +
                    "match");
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

    private List<String> getCategoryNames() {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.name);
        }
        return categoryNames;
    }

    @Override
    public void clear() {
        super.clear();
        super.notifyDataSetChanged();
    }

    @Override
    public void sort(Comparator<? super String> comparator) {
        //prevent sorting of the parent class
    }
}
