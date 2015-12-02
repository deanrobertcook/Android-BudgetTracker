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
import org.theronin.budgettracker.data.BudgetContract.CategoriesView;
import org.theronin.budgettracker.data.BudgetContract.CurrenciesTable;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.CurrencyWrapper;
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

    private static final int CURRENCY_SYMBOL_LOADER_ID = 0;
    private static final int CATEGORY_LOADER_ID = 1;

    private Spinner currencySpinner;
    private CurrencySymbolSpinnerAdapter currencySymbolSpinnerAdapter;
    private CurrencyWrapper lastSelectedCurrency;

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
        getLoaderManager().initLoader(CURRENCY_SYMBOL_LOADER_ID, null, this);
        getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__add_entry, container, false);

        currencySpinner = (Spinner) rootView.findViewById(R.id.spn__currency_symbol);
        currencySymbolSpinnerAdapter = new CurrencySymbolSpinnerAdapter();
        currencySpinner.setAdapter(currencySymbolSpinnerAdapter);

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

        if (categorySpinner.getSelectedItem() == null) {
            Toast.makeText(getActivity(), "Please create a category first", Toast
                    .LENGTH_SHORT).show();
            return;
        }
        //TODO, change this to use getSelectedItemPosition instead!
        lastSelectedCategory = categorySpinnerAdapter.getCategory(categorySpinner.getSelectedItem().toString());
        lastSelectedCurrency = currencySymbolSpinnerAdapter.getCurrency(currencySpinner
                .getSelectedItemPosition());

        ContentValues values = new ContentValues();
        values.put(EntriesTable.COL_CATEGORY_ID, lastSelectedCategory.id);
        values.put(EntriesTable.COL_DATE_ENTERED, currentSelectedUtcTime);
        values.put(EntriesTable.COL_CURRENCY_CODE, lastSelectedCurrency.code);
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
        this.currencySpinner = null;
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
        switch (id) {
            case CURRENCY_SYMBOL_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        CurrenciesTable.CONTENT_URI,
                        CurrenciesTable.PROJECTION,
                        null, null, CurrenciesTable.COL_CODE + " ASC"
                );
            case CATEGORY_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        CategoriesView.CONTENT_URI,
                        Category.projection,
                        null, null, null
                );
            default:
                throw new RuntimeException("Unrecognised loader id");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case CURRENCY_SYMBOL_LOADER_ID:
                updateCurrencySymbols(data);
                break;
            case CATEGORY_LOADER_ID:
                updateCategories(data);
                break;
            default:
                throw new RuntimeException("Unrecognised loader id");
        }
    }

    private void updateCurrencySymbols(Cursor data) {
        List<CurrencyWrapper> currencies = new ArrayList<>();
        while (data.moveToNext()) {
            currencies.add(CurrencyWrapper.fromCursor(data));
        }

        currencySymbolSpinnerAdapter.addAll(currencies);

        if (lastSelectedCurrency != null) {
            int lastSelectedCurrencyNewPosition = currencySymbolSpinnerAdapter.getPosition
                    (lastSelectedCurrency);
            currencySpinner.setSelection(lastSelectedCurrencyNewPosition);
            //If you don't set this back to null, then there are cases where the
            //lastSelectedCurrency gets out of date.
            lastSelectedCurrency = null;
        }
    }

    private void updateCategories(Cursor data) {
        List<Category> categories = new ArrayList<>();
        while (data.moveToNext()) {
            categories.add(Category.fromCursor(data));
        }
        categorySpinnerAdapter.addAll(categories);


        if (lastSelectedCategory != null) {
            int lastSelectedCategoryNewPosition =
                    categorySpinnerAdapter.getPosition(lastSelectedCategory);
            categorySpinner.setSelection(lastSelectedCategoryNewPosition);
            //If you don't set this back to null, then there are cases where the
            //lastSelectedCategory gets out of date.
            lastSelectedCategory = null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case CURRENCY_SYMBOL_LOADER_ID:
                currencySymbolSpinnerAdapter.clear();
                break;
            case CATEGORY_LOADER_ID:
                categorySpinnerAdapter.clear();
                break;
            default:
                throw new RuntimeException("Unrecognised loader id");
        }
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

    private class CurrencySymbolSpinnerAdapter extends ArrayAdapter<String> {

        List<CurrencyWrapper> currencies;

        public CurrencySymbolSpinnerAdapter() {
            super(getActivity(), android.R.layout.simple_spinner_item);
        }

        public void addAll(List<CurrencyWrapper> currencies) {
            this.currencies = currencies;

            super.clear();
            super.addAll(getSymbols());
            super.notifyDataSetChanged();
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_item_add_entry_currency_symbol_spinner,
                        parent, false);
            }
            TextView symbolTextView = (TextView) convertView.findViewById(
                    R.id.tv__add_entry__currency_spinner__drop_down__symbol);
            TextView codeTextView = (TextView) convertView.findViewById(
                    R.id.tv__add_entry__currency_spinner__drop_down__code);

            CurrencyWrapper currency = getCurrency(position);
            symbolTextView.setText(currency.symbol);
            codeTextView.setText("(" + currency.code + ")");

            if (position == currencySpinner.getSelectedItemPosition()) {
                convertView.setBackgroundColor(getResources().getColor(R.color.primary_light));
            }

            return convertView;
        }

        private List<String> getSymbols() {
            List<String> symbols = new ArrayList<>();
            for (CurrencyWrapper currency : currencies) {
                symbols.add(currency.symbol);
            }
            return symbols;
        }

        public CurrencyWrapper getCurrency(int position) {
            return currencies.get(position);
        }

        public int getPosition(CurrencyWrapper currency) {
            return currencies.indexOf(currency);
        }
    }
}
