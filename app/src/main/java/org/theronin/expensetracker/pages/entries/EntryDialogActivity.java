package org.theronin.expensetracker.pages.entries;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.dagger.InjectedActivity;
import org.theronin.expensetracker.data.loader.CategoryLoader;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.pages.reusable.DatePickerFragment;
import org.theronin.expensetracker.utils.CurrencySettings;
import org.theronin.expensetracker.utils.DateUtils;
import org.theronin.expensetracker.view.MoneyEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class EntryDialogActivity extends InjectedActivity
        implements View.OnClickListener,
        CurrencySettings.Listener,
        DatePickerFragment.Container,
        LoaderManager.LoaderCallbacks<List<Category>> {

    private static final String TAG = EntryDialogActivity.class.getName();
    private static final int CATEGORY_LOADER_ID = 0;

    @Inject AbsDataSource<Entry> entryDataSource;

    private CurrencySettings currencySettings;

    private TextView currencySymbolTextView;
    private TextView currencyCodeTextView;

    private TextView dateTextView;
    private long currentSelectedUtcTime;

    private LinearLayout inputRowsLayout;
    private List<ViewHolder> inputRows;

    private CategorySpinnerAdapter categorySpinnerAdapter;

    private View addEntryRowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__entry_dialog);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        toolbar.setTitle("Add Entries");
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);

        currencySettings = new CurrencySettings(this, this);
        currencySymbolTextView = (TextView) findViewById(R.id.currency__symbol);
        currencyCodeTextView = (TextView) findViewById(R.id.currency__code);
        setCurrentCurrency(currencySettings.getCurrentCurrency());

        dateTextView = (TextView) findViewById(R.id.tv__add_entry_date);
        setDateTextView(new Date().getTime());
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSpinnerClicked();
            }
        });

        categorySpinnerAdapter = new CategorySpinnerAdapter(this);
        inputRowsLayout = (LinearLayout) findViewById(R.id.lv__input_rows);
        inputRows = new ArrayList<>();
        addInputRow();

        addEntryRowButton = findViewById(R.id.add_entry_row_button);
        addEntryRowButton.setClickable(true);
        addEntryRowButton.setOnClickListener(this);
    }

    private void setCurrentCurrency(Currency currency) {
        currencySymbolTextView.setText(currency.symbol);
        currencyCodeTextView.setText(currency.code);
    }

    private void addInputRow() {
        ViewHolder viewHolder = createInputRow(inputRowsLayout);
        inputRows.add(viewHolder);
        inputRowsLayout.addView(viewHolder.inputView);
        viewHolder.inputView.requestFocus();
    }

    private ViewHolder createInputRow(ViewGroup parent) {
        View inputView = getLayoutInflater().inflate(R.layout.list_item__insert_entry_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(inputView);

        viewHolder.moneyEditText.setAmount(0);
        viewHolder.moneyEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addInputRow();
                    handled = true;
                }
                return handled;
            }
        });

        return viewHolder;
    }

    private class ViewHolder implements View.OnClickListener {
        public final View inputView;
        public final View clearButton;
        public final MoneyEditText moneyEditText;

        public final Spinner categorySpinner;
        public Category lastSelectedCategory;

        public ViewHolder(View inputView) {
            this.inputView = inputView;
            clearButton = inputView.findViewById(R.id.clear_row);
            if (inputRows.isEmpty()) { //the first one
                clearButton.setVisibility(View.INVISIBLE);
            } else {
                clearButton.setOnClickListener(this);
            }
            moneyEditText = (MoneyEditText) inputView.findViewById(R.id.amount_edit_layout);
            categorySpinner = (Spinner) inputView.findViewById(R.id.spn__add_entry_category);
            categorySpinner.setAdapter(categorySpinnerAdapter);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.clear_row:
                    inputRows.remove(this);
                    inputRowsLayout.removeView(inputView);
            }
        }
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
            case R.id.add_entry_row_button:
                addInputRow();
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
        setCurrentCurrency(currentCurrency);
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

    private void passInputToStore() {
        boolean error = false;
        List<Entry> entriesToInsert = new ArrayList<>();

        for (ViewHolder viewHolder : inputRows) {
            long amount = viewHolder.moneyEditText.getAmount();
            if (amount == 0) {
                error = true;
                break;
            }

            if (viewHolder.categorySpinner.getSelectedItem() == null) {
                Toast.makeText(this, "Please create a category first", Toast
                        .LENGTH_SHORT).show();
                return;
            }

            viewHolder.lastSelectedCategory = categorySpinnerAdapter.getCategory(
                    viewHolder.categorySpinner.getSelectedItemPosition());

            Entry entry = new Entry(
                    currentSelectedUtcTime,
                    amount,
                    viewHolder.lastSelectedCategory,
                    new Currency(currencySettings.getCurrentCurrency().code)
            );

            entriesToInsert.add(entry);
        }

        if (error) {
            Toast.makeText(this, "Make sure all rows have an amount greater than 0", Toast.LENGTH_SHORT).show();
        } else {
            entryDataSource.bulkInsert(entriesToInsert);
            Toast.makeText(this, "All G", Toast.LENGTH_SHORT).show();
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
        for (ViewHolder viewHolder : inputRows) {
            categorySpinnerAdapter.addAll(categories);

            if (viewHolder.lastSelectedCategory != null) {
                int lastSelectedCategoryNewPosition =
                        categorySpinnerAdapter.getPosition(viewHolder.lastSelectedCategory);
                viewHolder.categorySpinner.setSelection(lastSelectedCategoryNewPosition);
                //If you don't set this back to null, then there are cases where the
                //lastSelectedCategory gets out of date.
                viewHolder.lastSelectedCategory = null;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Category>> loader) {
        updateCategories(new ArrayList<Category>());
    }
}
