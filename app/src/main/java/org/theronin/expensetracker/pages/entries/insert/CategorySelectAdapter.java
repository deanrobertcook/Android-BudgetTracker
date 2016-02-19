package org.theronin.expensetracker.pages.entries.insert;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.comparators.CategoryAlphabeticalComparator;
import org.theronin.expensetracker.comparators.CategoryFrequencyComparator;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.view.SelectCategorySwipeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

public class CategorySelectAdapter extends RecyclerView.Adapter<CategorySelectAdapter.ViewHolder> implements SelectCategorySwipeView.Listener {

    private static final int MIN_CATEGORIES_FOR_FREQUENCY_SORT = 20;
    private final RecyclerView recyclerView;

    private List<Comparator<Category>> comparators;
    private int[] sortSizes;

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_WITH_SEPARATOR = 1;

    private List<Category> categories;
    private CategorySelectedListener listener;

    public CategorySelectAdapter(CategorySelectedListener listener, RecyclerView recyclerView) {
        categories = new ArrayList<>();
        this.recyclerView = recyclerView;
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
                return VIEW_TYPE_WITH_SEPARATOR;
            }
        }
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item__category_select, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        vh.setSeparatorVisible(getItemViewType(position) == VIEW_TYPE_WITH_SEPARATOR);
        vh.swipeView.reset();
        vh.swipeView.setCategory(categories.get(position));
        vh.swipeView.setListener(this);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public void onCategorySelected(Category category) {
        listener.onCategorySelected(category);
    }

    @Override
    public void onEditClicked(Category category) {
        Timber.v("onEditClicked %s", category.name);
    }

    @Override
    public void onMergeClicked(Category category) {
        Timber.v("onMergeClicked %s", category.name);
    }

    @Override
    public void onDeleteClicked(Category category) {
        Timber.v("onDeleteClicked %s", category.name);
    }

    @Override
    public void onOptionsExpanding() {
        Timber.v("onOptionsExpanding");
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            if (child instanceof ViewGroup) {
                ((SelectCategorySwipeView) child.findViewById(R.id.swipe_view)).closeOptions(false);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final SelectCategorySwipeView swipeView;
        public final View separator;

        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeView = (SelectCategorySwipeView) itemView.findViewById(R.id.swipe_view);
            this.separator = itemView.findViewById(R.id.separator);
        }

        public void setSeparatorVisible(boolean visible) {
            if (visible) {
                separator.setVisibility(View.VISIBLE);
            } else {
                separator.setVisibility(View.GONE);
            }
        }
    }

    public interface CategorySelectedListener {
        void onCategorySelected(Category categoryName);
    }
}
