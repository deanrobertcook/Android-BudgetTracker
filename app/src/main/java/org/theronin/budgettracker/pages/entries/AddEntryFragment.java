package org.theronin.budgettracker.pages.entries;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.loader.CategoryLoader;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.pages.reusable.DatePickerFragment;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddEntryFragment extends Fragment implements DatePickerFragment.Container,
        OnEditorActionListener,
        TextWatcher,
        LoaderCallbacks<List<Category>>,
        OnSharedPreferenceChangeListener {

    private static final String TAG = AddEntryFragment.class.getName();

    private static final int CATEGORY_LOADER_ID = 0;

    private SharedPreferences preferences;

    private TextView currencyCodeTextView;
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

        View currencyDisplayLayout = rootView.findViewById(R.id.ll__currency_layout);
        currencyCodeTextView = (TextView) currencyDisplayLayout.findViewById(
                R.id.tv__list_item__currency__code);
        currencySymbolTextView = (TextView) currencyDisplayLayout.findViewById(
                R.id.tv__list_item__currency__symbol);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);
        updateCurrencyDisplay(preferences);

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
        categorySpinnerAdapter = new CategorySpinnerAdapter(getActivity());
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

    private void updateCurrencyDisplay(SharedPreferences sharedPreferences) {
        Currency currentCurrency = MoneyUtils.getCurrentCurrency(getActivity(), sharedPreferences);
        currencyCodeTextView.setText(currentCurrency.code);
        currencySymbolTextView.setText(currentCurrency.symbol);
    }

    @Override
    public void onStop() {
        //Not attached to activity when settings are changed - causes NPEs without unregistering
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateCurrencyDisplay(sharedPreferences);
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

        lastSelectedCategory = categorySpinnerAdapter.getCategory(categorySpinner
                .getSelectedItemPosition());

        Entry entry = new Entry(
                currentSelectedUtcTime,
                amount,
                lastSelectedCategory,
                new Currency(currencyCodeTextView.getText().toString())
        );

        long id = ((BudgetTrackerApplication) getActivity().getApplication())
                .getDataSourceEntry().insert(entry);

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
    public Loader<List<Category>> onCreateLoader(int id, Bundle args) {
        return new CategoryLoader(getActivity(), false);
    }

    @Override
    public void onLoadFinished(Loader<List<Category>> loader, List<Category> data) {
        updateCategories(data);
    }

    private void updateCategories(List<Category> categories) {
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
    public void onLoaderReset(Loader<List<Category>> loader) {
        updateCategories(new ArrayList<Category>());
    }
}
