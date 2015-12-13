package org.theronin.budgettracker.pages.entries;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.utils.CurrencySettings;

import timber.log.Timber;

public class EntryDialogActivity extends AppCompatActivity
        implements View.OnClickListener, CurrencySettings.Listener {

    private TextView currencySymbolTextView;
    private TextView currencyCodeTextView;

    private CurrencySettings currencySettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__add_entry);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        toolbar.setTitle("Add Entry");
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currencySettings = new CurrencySettings(this, this);
        setupAmountView();

    }

    private void setupAmountView() {
        currencySymbolTextView = (TextView) findViewById(R.id.tv__add_entry_currency__symbol);
        currencyCodeTextView = (TextView) findViewById(R.id.tv__add_entry_currency__code);
        setCurrencyInformation();
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
                Timber.d("Save button clicked");
                break;
        }
    }

    @Override
    public void onHomeCurrencyChanged(Currency homeCurrency) {
        //do nothing
    }

    @Override
    public void onCurrentCurrencyChanged(Currency currentCurrency) {
        setCurrencyInformation();
    }
}
