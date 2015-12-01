package org.theronin.budgettracker.pages.entries;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.comparators.CategoryAlphabeticalComparator;
import org.theronin.budgettracker.comparators.CategoryFrequencyComparator;
import org.theronin.budgettracker.data.BudgetContract;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.pages.reusable.DatePickerFragment;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AddEntryFragment extends Fragment implements DatePickerFragment.Container,
        TextView.OnEditorActionListener,
        TextWatcher,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AddEntryFragment.class.getName();
    private static final int CATEGORY_LOADER_ID = 0;

    private TextView currencySymbolTextView;
    private Button confirmEntryButton;

    private Spinner categorySpinner;
    private CategorySpinnerAdapter categorySpinnerAdapter;
    private Category lastSelectedCategory;

    private TextView dateTextView;
    private long currentSelectedUtcTime;

    private EditText amountEditText;
    private String currentAmountText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__add_entry, container, false);

        currencySymbolTextView = (TextView) rootView.findViewById(R.id.tv__currency_symbol);
        currencySymbolTextView.setText(MoneyUtils.getCurrencySymbol());

        confirmEntryButton = (Button) rootView.findViewById(R.id.btn__add_entry_confirm);
        confirmEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passInputToStore();
            }
        });

        amountEditText = (EditText) rootView.findViewById(R.id.et__entry_amount);
        setAmountEditText(0);
        amountEditText.setOnEditorActionListener(this);
        amountEditText.addTextChangedListener(this);

        categorySpinner = (Spinner) rootView.findViewById(R.id.spn__entry_category);
        categorySpinnerAdapter = new CategorySpinnerAdapter();
        categorySpinner.setAdapter(categorySpinnerAdapter);

        dateTextView = (TextView) rootView.findViewById(R.id.tv__entry_date);
        setDateTextView(new Date().getTime());
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSpinnerClicked();
            }
        });

        return rootView;
    }

    private void setAmountEditText(long amount) {
        amountEditText.removeTextChangedListener(this);

        currentAmountText = MoneyUtils.convertCentsToDisplayAmount(amount);

        amountEditText.setText(currentAmountText);
        amountEditText.setSelection(currentAmountText.length());

        amountEditText.addTextChangedListener(this);
    }

    private long getAmountEditText() {
        String displayAmount = amountEditText.getText().toString();
        return MoneyUtils.convertDisplayAmountToCents(displayAmount);
    }

    private void setDateTextView(long utcTime) {
        this.currentSelectedUtcTime = utcTime;
        dateTextView.setText(DateUtils.getDisplayFormattedDate(currentSelectedUtcTime));
    }

    private void dateSpinnerClicked() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setContainer(this);
        datePickerFragment.show(getFragmentManager(), TAG);
    }

    private void passInputToStore() {
        long amount = getAmountEditText();
        if (amount == 0) {
            Toast.makeText(getActivity(), "Please provide an amount for the entry", Toast
                    .LENGTH_SHORT).show();
            return;
        }

        lastSelectedCategory = categorySpinnerAdapter.getCategory(categorySpinner.getSelectedItem().toString());

        ContentValues values = new ContentValues();
        values.put(EntriesTable.COL_CATEGORY_ID, lastSelectedCategory.id);
        values.put(EntriesTable.COL_DATE_ENTERED, currentSelectedUtcTime);
        values.put(EntriesTable.COL_AMOUNT_CENTS, amount);

        Uri uri = getActivity().getContentResolver().insert(
                EntriesTable.CONTENT_URI, values);

        long id = Long.parseLong(uri.getLastPathSegment());

        if (id == -1) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "All G", Toast.LENGTH_SHORT).show();
            setAmountEditText(0);

            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService
                        (Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        this.currencySymbolTextView = null;
        this.confirmEntryButton = null;
        this.categorySpinner = null;

        super.onDestroyView();
    }

    @Override
    public void onDateSelected(long utcTime) {
        setDateTextView(utcTime);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            passInputToStore();
            handled = true;
        }
        return handled;
    }

    //////////////////////////////////////////////////////
    // EditText watcher methods
    //////////////////////////////////////////////////////

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence input, int start, int before, int count) {
        if (!input.toString().equals(currentAmountText) && input.length() != 0) {
            String cleanString = input.toString().replaceAll("[,.]", "");
            long cents = Long.parseLong(cleanString);
            setAmountEditText(cents);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                BudgetContract.CategoriesView.CONTENT_URI,
                Category.projection,
                null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<Category> categories = new ArrayList<>();
        while (data.moveToNext()) {
            categories.add(Category.fromCursor(data));
        }
        categorySpinnerAdapter.addAll(categories);


        if (lastSelectedCategory != null) {
            int lastSelectedCategoryNewPosition = categorySpinnerAdapter.getPosition
                    (lastSelectedCategory);
            categorySpinner.setSelection(lastSelectedCategoryNewPosition);
            //If you don't set this back to null, then there are cases where the
            // lastSelectedCategory
            //gets out of date.
            lastSelectedCategory = null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        categorySpinnerAdapter.clear();
    }

    private class CategorySpinnerAdapter extends ArrayAdapter<String> {

        private List<Category> categories;

        private List<Comparator<Category>> comparators;
        private int[] sortSizes;

        private final int FREQUENCY_SORT_SIZE = 10;

        private final int VIEW_TYPE_NORMAL = 0;
        private final int VIEW_TYPE_WITH_BORDER = 1;

        public CategorySpinnerAdapter() {
            super(getActivity(), android.R.layout.simple_spinner_item);
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
                        categories.size() - 1};
            } else {
                comparators.add(new CategoryAlphabeticalComparator());
                sortSizes = new int[]{categories.size() - 1};
            }
        }

        public Category getCategory(String categoryName) {
            int pos = super.getPosition(categoryName);
            return categories.get(pos);
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
                            android.R.layout.simple_dropdown_item_1line,
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
}
