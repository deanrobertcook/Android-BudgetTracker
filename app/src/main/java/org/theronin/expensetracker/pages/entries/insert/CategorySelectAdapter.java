package org.theronin.expensetracker.pages.entries.insert;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.comparators.CategoryAlphabeticalComparator;
import org.theronin.expensetracker.comparators.CategoryFrequencyComparator;
import org.theronin.expensetracker.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategorySelectAdapter extends RecyclerView.Adapter<CategorySelectAdapter.ViewHolder> {

    private static final int MIN_CATEGORIES_FOR_FREQUENCY_SORT = 20;
    private final Context context;

    private List<Comparator<Category>> comparators;
    private int[] sortSizes;

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_WITH_SEPARATOR_FREQUENCY = 1;
    private static final int VIEW_TYPE_WITH_SEPARATOR_ALPHABETICAL = 2;

    private List<Category> categories;
    private CategorySelectedListener listener;
    private boolean moreButtonsVisible = true;
    private int selectedPosition = -1;

    public CategorySelectAdapter(Context context, CategorySelectedListener listener) {
        this.context = context;
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
        notifyDataSetChanged();
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
        if (position == 0) {
            return VIEW_TYPE_WITH_SEPARATOR_FREQUENCY;
        }
        int borderIndex = 0;
        for (int sortSize : sortSizes) {
            borderIndex += sortSize;
            if (position == borderIndex && position != categories.size() - 1) {
                return VIEW_TYPE_WITH_SEPARATOR_ALPHABETICAL;
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
        final Category category = categories.get(position);

        vh.setCategory(category);
        vh.setSeparator(getItemViewType(position));

        vh.categoryNameView.setOnClickListener(vh);
        vh.moreButton.setOnClickListener(vh);

        vh.categoryNameView.setText(category.getDisplayNameWithFrequency());
        vh.setMoreOptionsVisible(moreButtonsVisible);
        vh.setHighlighted(position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setMoreButtonsVisible(boolean moreButtonsVisible) {
        this.moreButtonsVisible = moreButtonsVisible;
        notifyDataSetChanged();
    }

    public void setCategoryHighlighted(Category category) {
        if (category == null) {
            selectedPosition = -1;
            return;
        }
        selectedPosition = categories.indexOf(category);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView separator;
        public final TextView categoryNameView;
        public final View moreButton;
        private Category category;

        public ViewHolder(View itemView) {
            super(itemView);
            this.separator = (TextView) itemView.findViewById(R.id.separator);
            this.categoryNameView = (TextView) itemView.findViewById(R.id.category_name);
            this.moreButton = itemView.findViewById(R.id.more);
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public void setHighlighted(boolean highlighted) {
            if (highlighted) {
                categoryNameView.setBackgroundColor(context.getResources().getColor(R.color.primary_light));
            } else {
                categoryNameView.setBackground(context.getResources().
                        getDrawable(R.drawable.list_item_selector, context.getTheme()));
            }
        }

        public void setMoreOptionsVisible(boolean visible) {
            if (visible) {
                moreButton.setVisibility(View.VISIBLE);
            } else {
                moreButton.setVisibility(View.GONE);
            }
        }

        public void setSeparator(int viewType) {
            switch (viewType) {
                case VIEW_TYPE_WITH_SEPARATOR_FREQUENCY:
                    separator.setVisibility(View.VISIBLE);
                    separator.setText(context.getString(R.string.category_separator_frequency));
                    break;
                case VIEW_TYPE_WITH_SEPARATOR_ALPHABETICAL:
                    separator.setVisibility(View.VISIBLE);
                    separator.setText(context.getString(R.string.category_separator_alphabetical));
                    break;
                default:
                    separator.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.category_name:
                    listener.onCategorySelected(category);
                    break;
                case R.id.more:
                    listener.onMoreButtonSelected(category);
                    break;
            }
        }
    }

    public interface CategorySelectedListener {
        void onCategorySelected(Category category);

        void onMoreButtonSelected(Category category);
    }
}
