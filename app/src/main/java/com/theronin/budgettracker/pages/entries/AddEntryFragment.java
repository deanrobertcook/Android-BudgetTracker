package com.theronin.budgettracker.pages.entries;

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

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetContract;
import com.theronin.budgettracker.data.BudgetContract.EntriesTable;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.pages.reusable.DatePickerFragment;
import com.theronin.budgettracker.utils.DateUtils;
import com.theronin.budgettracker.utils.MoneyUtils;

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
    private Date currentSelectedDate;

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
        categorySpinnerAdapter = new CategorySpinnerAdapter(getActivity(),
                android.R.layout.simple_spinner_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categorySpinnerAdapter);

        dateTextView = (TextView) rootView.findViewById(R.id.tv__entry_date);
        setDateTextView(new Date());
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

    private void setDateTextView(Date date) {
        this.currentSelectedDate = date;
        dateTextView.setText(DateUtils.getDisplayFormattedDate(date));
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
        String dateEnteredVal = DateUtils.getStorageFormattedDate(currentSelectedDate);

        ContentValues values = new ContentValues();
        values.put(EntriesTable.COL_CATEGORY_ID, lastSelectedCategory.id);
        values.put(EntriesTable.COL_DATE_ENTERED, dateEnteredVal);
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
    public void onDateSelected(Date date) {
        setDateTextView(date);
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
                BudgetContract.CategoriesTable.CONTENT_URI,
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
        private final int FREQUENCY_SORT_SIZE = 10;

        public CategorySpinnerAdapter(Context context, int resource) {
            super(context, resource);
            comparators = new ArrayList<>();
            comparators.add(new FrequencyComparator());
            comparators.add(new AlphabeticalComparator());
        }

        public void addAll(List<Category> categories) {
            this.categories = categories;
            int[] sortSizes = {FREQUENCY_SORT_SIZE, categories.size() - 1};
            sortCategories(comparators, sortSizes);
            super.clear();
            super.addAll(getCategoryNames());
            super.notifyDataSetChanged();
        }

        public Category getCategory(String categoryName) {
            int pos = super.getPosition(categoryName);
            return categories.get(pos);
        }

        public int getPosition(Category category) {
            return super.getPosition(category.name);
        }

        /**
         *
         */
        public void sortCategories(List<Comparator<Category>> comparators, int[] sizes) {
            if (comparators.size() != sizes.length) {
                throw new RuntimeException("The number of comparators and sizes of sublists must " +
                        "match");
            }

            List<Category> finalSortedList = new ArrayList<>();

            for (int i = 0; i < comparators.size(); i++) {
                finalSortedList.addAll(generateSortedSublist(comparators.get(i), sizes[i]));
            }
            categories = finalSortedList;

            super.clear();
            super.addAll(getCategoryNames());
            super.notifyDataSetChanged();
        }

        public List<Category> generateSortedSublist(Comparator<Category> comparator, int size) {
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

        private class FrequencyComparator implements Comparator<Category> {
            @Override
            public int compare(Category lhs, Category rhs) {
                return (int) (lhs.frequency - rhs.frequency);
            }
        }

        private class AlphabeticalComparator implements Comparator<Category> {
            @Override
            public int compare(Category lhs, Category rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        }
    }


}
