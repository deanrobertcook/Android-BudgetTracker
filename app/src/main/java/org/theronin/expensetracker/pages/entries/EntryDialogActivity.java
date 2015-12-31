package org.theronin.expensetracker.pages.entries;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.theronin.expensetracker.dagger.InjectedActivity;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.loader.CategoryLoader;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.pages.reusable.DatePickerFragment;
import org.theronin.expensetracker.utils.CurrencySettings;
import org.theronin.expensetracker.utils.DateUtils;
import org.theronin.expensetracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class EntryDialogActivity extends InjectedActivity
        implements View.OnClickListener,
        CurrencySettings.Listener,
        TextWatcher,
        TextView.OnEditorActionListener,
        DatePickerFragment.Container,
        LoaderManager.LoaderCallbacks<List<Category>> {

    private static final String TAG = EntryDialogActivity.class.getName();
    private static final int CATEGORY_LOADER_ID = 0;

    @Inject AbsDataSource<Entry> entryDataSource;

    private CurrencySettings currencySettings;
    private TextView currencySymbolTextView;
    private TextView currencyCodeTextView;

    private EditText amountEditText;
    private long currentAmount;

    private Spinner categorySpinner;
    private CategorySpinnerAdapter categorySpinnerAdapter;
    private Category lastSelectedCategory;

    private TextView dateTextView;
    private long currentSelectedUtcTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__entry_dialog);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        toolbar.setTitle("Add Entry");
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);

        currencySettings = new CurrencySettings(this, this);
        currencySymbolTextView = (TextView) findViewById(R.id.tv__add_entry_currency__symbol);
        currencyCodeTextView = (TextView) findViewById(R.id.tv__add_entry_currency__code);
        setCurrencyInformation();

        amountEditText = (EditText) findViewById(R.id.et__add_entry_amount);
        setAmountEditText(0);
        amountEditText.setOnEditorActionListener(this);
        amountEditText.addTextChangedListener(this);

        categorySpinner = (Spinner) findViewById(R.id.spn__add_entry_category);
        categorySpinnerAdapter = new CategorySpinnerAdapter(this);
        categorySpinner.setAdapter(categorySpinnerAdapter);

        dateTextView = (TextView) findViewById(R.id.tv__add_entry_date);
        setDateTextView(new Date().getTime());
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSpinnerClicked();
            }
        });

    }

    private void setCurrencyInformation() {
        currencySymbolTextView.setText(currencySettings.getCurrentCurrency().symbol);
        currencyCodeTextView.setText(currencySettings.getCurrentCurrency().code);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_entry, menu);

        View saveButton = menu.findItem(R.id.action_save).getActionView();
        saveButton.setOnClickListener(this);

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_save:
                passInputToStore();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); //emulate back rather than up
                return true;
        }
        return false;
    }

    @Override
    public void onHomeCurrencyChanged(Currency homeCurrency) {
        //do nothing
    }

    @Override
    public void onCurrentCurrencyChanged(Currency currentCurrency) {
        setCurrencyInformation();
    }

    private void setAmountEditText(long amount) {
        amountEditText.removeTextChangedListener(this);

        currentAmount = amount;

        amountEditText.setText(currentAmountText());
        amountEditText.setSelection(currentAmountText().length());

        amountEditText.addTextChangedListener(this);
    }

    private String currentAmountText() {
        return MoneyUtils.getDisplay(this, currentAmount);
    }

    private long getAmountEditText() {
        String displayAmount = amountEditText.getText().toString();
        return MoneyUtils.getCents(displayAmount);
    }

    private void setDateTextView(long utcTime) {
        currentSelectedUtcTime = utcTime;
        dateTextView.setText(DateUtils.getDisplayFormattedDate(currentSelectedUtcTime));
    }

    private void dateSpinnerClicked() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setContainer(this);
        datePickerFragment.show(getFragmentManager(), TAG);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence input, int start, int before, int count) {
        if (!input.toString().equals(currentAmountText()) && input.length() != 0) {
            String cleanString = input.toString().replaceAll("[,.]", "");
            long cents = Long.parseLong(cleanString);
            setAmountEditText(cents);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

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

    private void passInputToStore() {
        long amount = getAmountEditText();
        if (amount == 0) {
            Toast.makeText(this, "Please provide an amount for the entry", Toast
                    .LENGTH_SHORT).show();
            return;
        }

        if (categorySpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please create a category first", Toast
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

        long id = entryDataSource.insert(entry);

        if (id == -1) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "All G", Toast.LENGTH_SHORT).show();
            setAmountEditText(0);

            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService
                        (Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            finish();
        }

    }

    @Override
    public void onDateSelected(long utcTime) {
        setDateTextView(utcTime);
    }

    @Override
    public Loader<List<Category>> onCreateLoader(int id, Bundle args) {
        return new CategoryLoader(this, this, false);
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
