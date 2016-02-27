package org.theronin.expensetracker.pages.entries.insert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import org.theronin.expensetracker.utils.DateUtils;
import org.theronin.expensetracker.utils.TrackingUtils;
import org.theronin.expensetracker.utils.TransitionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static org.theronin.expensetracker.utils.Prefs.getCurrentCurrency;

public class EntryDialogActivity extends InjectedActivity
        implements View.OnClickListener,
        DatePickerFragment.Container, InsertRowViewHolder.RowClickListener {

    private static final String TAG = EntryDialogActivity.class.getName();

    @Inject AbsDataSource<Entry> entryDataSource;

    private TextView currencySymbolTextView;
    private TextView currencyCodeTextView;

    private TextView dateTextView;
    private long currentSelectedUtcTime;

    private LinearLayout inputRowsLayout;
    private List<InsertRowViewHolder> inputRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__entry_dialog);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        toolbar.setTitle(getString(R.string.add_entry_dialog_title));
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currencySymbolTextView = (TextView) findViewById(R.id.currency__symbol);
        currencyCodeTextView = (TextView) findViewById(R.id.currency__code);
        setCurrentCurrency(getCurrentCurrency(this));

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

        findViewById(R.id.add_entry_row_button).setOnClickListener(this);
    }

    private void setCurrentCurrency(Currency currency) {
        currencySymbolTextView.setText(currency.symbol);
        currencyCodeTextView.setText(currency.code);
    }

    private void addInputRow() {
        TrackingUtils.extraInputRowCreated();
        InsertRowViewHolder viewHolder = new InsertRowViewHolder(inputRowsLayout, this, inputRows.size());
        inputRows.add(viewHolder);
        inputRowsLayout.addView(viewHolder.rowView);
        viewHolder.rowView.requestFocus();
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
        TrackingUtils.categoryFieldClicked();
        Intent intent = new Intent(EntryDialogActivity.this, CategorySelectActivity.class);
        startActivityForResult(intent, rowIndex, TransitionUtils.getLeftTransitionAnimation(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            inputRows.get(requestCode).setCategory(
                    (Category) data.getExtras().getSerializable(CategorySelectActivity.CATEGORY_KEY));
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
                onCancelPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        onCancelPressed();
    }

    private void onCancelPressed() {
        if (inputProvided()) {
            showCancelConfirmationDialog();
        } else {
            finish();
        }
    }

    private boolean inputProvided() {
        boolean amountEntered = false;
        boolean categoryEntered = false;

        for (InsertRowViewHolder viewHolder : inputRows) {
            if (viewHolder.moneyEditText.getAmount() > 0) {
                amountEntered = true;
            }
            if (viewHolder.getCategory() != null) {
                categoryEntered = true;
            }
        }
        return amountEntered || categoryEntered;
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.entry_confirm_discard_dialog__message)
                .setPositiveButton(R.string.entry_confirm_discard_dialog__positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.entry_confirm_discard_dialog__negative_button, null)
                .show();
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
                    new Currency(getCurrentCurrency(this).code)
            );

            TrackingUtils.entryCreated(entry);
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
