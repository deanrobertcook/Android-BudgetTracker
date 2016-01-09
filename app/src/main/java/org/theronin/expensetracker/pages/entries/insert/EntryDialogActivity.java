package org.theronin.expensetracker.pages.entries.insert;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.dagger.InjectedActivity;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.pages.reusable.DatePickerFragment;
import org.theronin.expensetracker.utils.CurrencySettings;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class EntryDialogActivity extends InjectedActivity
        implements View.OnClickListener,
        CurrencySettings.Listener,
        DatePickerFragment.Container, InsertRowViewHolder.RowClickListener {

    private static final String TAG = EntryDialogActivity.class.getName();

    @Inject AbsDataSource<Entry> entryDataSource;

    private CurrencySettings currencySettings;

    private TextView currencySymbolTextView;
    private TextView currencyCodeTextView;

    private TextView dateTextView;
    private long currentSelectedUtcTime;

    private LinearLayout inputRowsLayout;
    private List<InsertRowViewHolder> inputRows;

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
        InsertRowViewHolder viewHolder = createInputRow(inputRowsLayout);
        inputRows.add(viewHolder);
        inputRowsLayout.addView(viewHolder.rowView);
        viewHolder.rowView.requestFocus();
    }

    private InsertRowViewHolder createInputRow(ViewGroup parent) {
        View inputView = getLayoutInflater().inflate(R.layout.list_item__insert_entry_row, parent, false);
        InsertRowViewHolder viewHolder = new InsertRowViewHolder(inputView, this, inputRows.size());

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

    @Override
    public void onRowClearButtonClicked(int rowIndex) {
        InsertRowViewHolder holder = inputRows.get(rowIndex);
        inputRows.remove(holder);
        inputRowsLayout.removeView(holder.rowView);

        if (inputRows.isEmpty()) {
            addInputRow();
            return;
        }

        for (int i = 0; i < inputRows.size(); i++) {
            holder = inputRows.get(i);
            holder.resetRowIndex(i);
        }
    }

    @Override
    public void onRowSelectCategoryFieldClicked(int rowIndex) {
        Intent intent = new Intent(EntryDialogActivity.this, CategorySelectActivity.class);
        startActivityForResult(intent, rowIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String categorySelectedName = data.getExtras().getString(CategorySelectActivity.CATEGORY_NAME_KEY);
            inputRows.get(requestCode).setCategory(new Category(categorySelectedName));
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
        boolean anEntryHasNoAmount = false;
        boolean anEntryHasNoCategory = false;
        List<Entry> entriesToInsert = new ArrayList<>();

        for (InsertRowViewHolder viewHolder : inputRows) {
            long amount = viewHolder.moneyEditText.getAmount();
            if (amount == 0) {
                anEntryHasNoAmount = true;
                break;
            }

            if (viewHolder.getCategory() == null) {
                anEntryHasNoCategory = true;
                break;
            }

            Entry entry = new Entry(
                    currentSelectedUtcTime,
                    amount,
                    viewHolder.getCategory(),
                    new Currency(currencySettings.getCurrentCurrency().code)
            );

            entriesToInsert.add(entry);
        }

        if (anEntryHasNoAmount) {
            Toast.makeText(this, "Make sure all rows have an amount greater than 0", Toast.LENGTH_SHORT).show();
        } else if (anEntryHasNoCategory) {
            Toast.makeText(this, "Make sure all rows have a selected category", Toast.LENGTH_SHORT).show();
        } else {
            entryDataSource.bulkInsert(entriesToInsert);
            Toast.makeText(this, "All G", Toast.LENGTH_SHORT).show();
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            finish();
        }
    }

    @Override
    public void onDateSelected(long utcTime) {
        setDateTextView(utcTime);
    }
}
